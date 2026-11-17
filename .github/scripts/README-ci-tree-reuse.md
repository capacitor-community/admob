# CI result reuse

On a branch push that merges a PR, reuse the latest successful PR CI only when
its actual checked-out root Git tree SHA equals the merged repository tree SHA.
The complete tree includes sources, tests, workflows and dependency manifests.
Commit SHA equality alone is not used. Each PR run records its merge checkout
without installing dependencies, with its PR number and run attempt. Evidence is
only uploaded after every covered PR job succeeds; skipped jobs do not qualify.

The verifier checks the Git commit tree and its PR-head parent, and rechecks the
latest run after reading evidence. Failed, pending, cancelled, superseded,
missing or expired evidence cannot authorize reuse; validation runs normally.
Manual and scheduled validation always runs. Existing CI without evidence is
not reusable. The summary links the original run; reused tests are not reported
as newly executed tests.

Deployment, release packaging, security scans and report publication retain their
own work. Deployment builds still run when their output is needed. CI result
reuse preserves the prior observation; it does not claim to test new external
services, dependencies or runner images. Request fresh CI when needed.

Run `node --test .github/scripts/ci-tree-reuse.test.mjs` and actionlint on modified
workflows when changing this logic. The regression tests protect against stale
successes and bypassed failure gates.

Conflicting checkout claims for the same PR and run attempt are rejected before
matching a target tree, including expired conflicting claims.
