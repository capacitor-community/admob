import assert from 'node:assert/strict';
import test from 'node:test';
import { resolveReuse, requireResults } from './ci-tree-reuse.mjs';

const tree = 'a'.repeat(40);
const merge = 'b'.repeat(40);
const head = 'c'.repeat(40);

function fixture() {
  const pr = {
    number: 60,
    merged_at: '2026-10-20T00:00:00Z',
    base: { ref: 'main' },
    merge_commit_sha: 'final',
    head: { sha: head },
  };
  const run = {
    event: 'pull_request',
    id: 123,
    run_number: 10,
    run_attempt: 2,
    head_sha: head,
    path: '.github/workflows/ci.yml',
    status: 'completed',
    conclusion: 'success',
    pull_requests: [{ number: 60 }],
    html_url: 'https://github.com/owner/repo/actions/runs/123',
  };
  const artifact = {
    name: `ci-tree-pr-60-${tree}-${merge}-2`,
    expired: false,
  };
  const commit = { tree: { sha: tree }, parents: [{ sha: 'base' }, { sha: head }] };
  const runs = [run];
  const artifacts = [artifact];
  const context = {
    eventName: 'push',
    ref: 'refs/heads/main',
    sha: 'final',
    repo: { owner: 'owner', repo: 'repo' },
  };
  const github = {
    rest: {
      repos: { listPullRequestsAssociatedWithCommit: 'pulls' },
      actions: {
        listWorkflowRuns: 'runs',
        listWorkflowRunArtifacts: 'artifacts',
      },
      git: { getCommit: async () => ({ data: commit }) },
    },
    paginate: async (endpoint, args) => {
      if (endpoint === 'pulls') return [pr];
      if (endpoint === 'runs') {
        assert.equal(args.event, 'pull_request');
        assert.equal(args.status, undefined);
        assert.equal(args.head_sha, undefined);
        return runs;
      }
      assert.equal(args.run_id, 123);
      return artifacts;
    },
  };
  return { github, context, pr, run, runs, artifact, artifacts, commit };
}

test("reuse the successful latest PR run's actual full tree across different commits", async () => {
  const f = fixture();
  const result = await resolveReuse(f.github, f.context, tree, 'ci.yml');
  assert.deepEqual(result, {
    reuse: true,
    run: 123,
    url: f.run.html_url,
    tree,
  });
});

const rejected = {
  'PR event': (f) => (f.context.eventName = 'pull_request'),
  'merge queue event': (f) => (f.context.eventName = 'merge_group'),
  'manual verification': (f) => (f.context.eventName = 'workflow_dispatch'),
  'other branch': (f) => (f.context.ref = 'refs/heads/other'),
  'direct push': (f) => (f.pr.merge_commit_sha = 'other'),
  'unmerged PR': (f) => (f.pr.merged_at = null),
  'older PR revision': (f) => (f.run.head_sha = 'old'),
  'different workflow': (f) => (f.run.path = 'other.yml'),
  'failed run': (f) => (f.run.conclusion = 'failure'),
  'cancelled run': (f) => (f.run.conclusion = 'cancelled'),
  'pending rerun': (f) => (f.run.status = 'in_progress'),
  'newer failed CI': (f) => f.runs.push({ ...f.run, run_number: 11, conclusion: 'failure' }),
  'newer pending CI': (f) => f.runs.push({ ...f.run, run_number: 11, status: 'queued' }),
  'missing evidence': (f) => (f.artifacts.length = 0),
  'ambiguous evidence': (f) => f.artifacts.push({ ...f.artifact }),
  'expired evidence': (f) => (f.artifact.expired = true),
  'evidence from earlier attempt': (f) => (f.run.run_attempt = 3),
  'different recorded tree': (f) => (f.artifact.name = f.artifact.name.replace(tree, 'd'.repeat(40))),
  'different actual tree': (f) => (f.commit.tree.sha = 'd'.repeat(40)),
  'unrelated tested commit': (f) => (f.commit.parents = [{ sha: 'other' }]),
};
for (const [name, mutate] of Object.entries(rejected)) {
  test(`run tests instead for ${name}`, async () => {
    const f = fixture();
    mutate(f);
    assert.equal((await resolveReuse(f.github, f.context, tree, 'ci.yml')).reuse, false);
  });
}

test('gate rejects failures and unverified skipped tests', () => {
  const needs = (result) => ({ 'ci-reuse': { result: 'success' }, test: { result } });
  requireResults(false, needs('success'), ['test']);
  requireResults(true, needs('skipped'), ['test']);
  for (const result of ['failure', 'cancelled', 'skipped']) {
    assert.throws(() => requireResults(false, needs(result), ['test']));
  }
  assert.throws(() => requireResults(true, needs('success'), ['test']));
  assert.throws(() => requireResults(true, { 'ci-reuse': { result: 'failure' } }, []));
  requireResults(true, needs('success'), ['test'], ['test']);
});

test('a newer run or rerun starting during verification prevents reuse', async () => {
  for (const field of ['id', 'run_attempt', 'status', 'conclusion']) {
    const f = fixture();
    f.github.rest.git.getCommit = async () => {
      f.runs[0] = {
        ...f.run,
        [field]: field === 'status' ? 'in_progress' : field === 'conclusion' ? 'failure' : 999,
      };
      return { data: f.commit };
    };
    assert.equal((await resolveReuse(f.github, f.context, tree, 'ci.yml')).reuse, false);
  }
});

test('unavailable APIs and empty historical PR associations fail safely', async () => {
  const missing = fixture();
  missing.github.paginate = async () => {
    throw new Error('rate limit');
  };
  assert.equal((await resolveReuse(missing.github, missing.context, tree, 'ci.yml')).reuse, false);
  const closed = fixture();
  closed.run.pull_requests = [];
  assert.equal((await resolveReuse(closed.github, closed.context, tree, 'ci.yml')).reuse, true);
  closed.artifact.name = closed.artifact.name.replace('pr-60', 'pr-61');
  assert.equal((await resolveReuse(closed.github, closed.context, tree, 'ci.yml')).reuse, false);
});

test('same contract supports master and maintenance branches', async () => {
  for (const branch of ['master', 'ios26']) {
    const f = fixture();
    f.context.ref = `refs/heads/${branch}`;
    f.pr.base.ref = branch;
    assert.equal((await resolveReuse(f.github, f.context, tree, 'ci.yml')).reuse, true);
  }
});

test('conflicting checkout claims cannot hide the real tested tree', async () => {
  for (const expired of [false, true]) {
    const f = fixture();
    f.artifacts.push({ name: f.artifact.name.replace(tree, 'd'.repeat(40)), expired });
    assert.equal((await resolveReuse(f.github, f.context, tree, 'ci.yml')).reuse, false);
  }
});
