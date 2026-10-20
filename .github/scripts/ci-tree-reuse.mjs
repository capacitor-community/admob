// Reuse observations of a complete Git tree, never just a PR head commit.
export async function resolveReuse(github, context, tree, workflow) {
  const fallback = (reason) => ({ reuse: false, reason });
  if (context.eventName !== 'push' || !context.ref.startsWith('refs/heads/'))
    return fallback('Fresh validation requested');
  const repo = context.repo;
  try {
    const pulls = await github.paginate(github.rest.repos.listPullRequestsAssociatedWithCommit, {
      ...repo,
      commit_sha: context.sha,
      per_page: 100,
    });
    const matches = pulls.filter(
      (pr) =>
        pr.merged_at && pr.base.ref === context.ref.slice('refs/heads/'.length) && pr.merge_commit_sha === context.sha,
    );
    if (matches.length !== 1) return fallback('No unique merged PR');
    const pr = matches[0];
    const latestRun = async () => {
      const runs = await github.paginate(github.rest.actions.listWorkflowRuns, {
        ...repo,
        workflow_id: workflow,
        event: 'pull_request',
        per_page: 100,
      });
      return runs
        .filter(
          (run) =>
            run.pull_requests.some((pull) => pull.number === pr.number) ||
            (!run.pull_requests.length && run.head_sha === pr.head.sha),
        )
        .sort((a, b) => b.run_number - a.run_number || b.id - a.id)[0];
    };
    const run = await latestRun();
    const successful = (value) =>
      value &&
      value.event === 'pull_request' &&
      value.head_sha === pr.head.sha &&
      value.path.split('@')[0] === `.github/workflows/${workflow}` &&
      value.status === 'completed' &&
      value.conclusion === 'success';
    if (!successful(run)) return fallback('Latest PR CI has not succeeded');
    const artifacts = await github.paginate(github.rest.actions.listWorkflowRunArtifacts, {
      ...repo,
      run_id: run.id,
      per_page: 100,
    });
    const prefix = `ci-tree-pr-${pr.number}-`;
    const suffix = `-${run.run_attempt}`;
    // Reject multiple checkout claims even if only one matches the target tree.
    // Dependencies can upload artifacts, but cannot replace the final gate's
    // immutable evidence. Never pick a convenient claim out of conflicting ones.
    const evidence = artifacts.filter((artifact) => artifact.name.startsWith(prefix) && artifact.name.endsWith(suffix));
    if (evidence.length !== 1 || evidence[0].expired) return fallback('No unique checkout evidence');
    const identity = evidence[0].name.slice(prefix.length, -suffix.length);
    const match = /^([a-f0-9]{40})-([a-f0-9]{40})$/.exec(identity);
    if (!match || match[1] !== tree) return fallback('Invalid or different checkout tree');
    const sha = match[2];
    const { data: commit } = await github.rest.git.getCommit({
      ...repo,
      commit_sha: sha,
    });
    if (commit.tree.sha !== tree || commit.parents.length !== 2 || commit.parents[1].sha !== pr.head.sha)
      return fallback('Actual PR merge tree does not match');
    // A rerun or newer run starting during verification supersedes old success.
    const latest = await latestRun();
    if (!successful(latest) || latest.id !== run.id || latest.run_attempt !== run.run_attempt)
      return fallback('PR CI changed during verification');
    return { reuse: true, tree, run: run.id, url: run.html_url };
  } catch (error) {
    return fallback(`Cannot verify previous CI: ${error.message}`);
  }
}

export function requireResults(reuse, needs, required, executed = []) {
  if (needs['ci-reuse']?.result !== 'success') throw new Error('Tree verification did not succeed');
  for (const [name, job] of Object.entries(needs)) {
    if (name === 'ci-reuse') continue;
    if (reuse && !executed.includes(name)) {
      if (job.result !== 'skipped') throw new Error(`${name}: expected verified reuse`);
    } else if (job.result !== 'success' && !(job.result === 'skipped' && !required.includes(name))) {
      throw new Error(`${name}: ${job.result}`);
    }
  }
}
