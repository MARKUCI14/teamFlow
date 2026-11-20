# Git Workflow

## Branching (GitHub Flow)
- `main` is always releasable and protected (PR required, CI green).
- One short-lived branch per issue: `<type>/<issue-number>-<slug>`, e.g. `feat/12-order-service-api`, `fix/34-ws-reconnect`, `docs/40-c4-update`.
- Types: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`, `infra`.

## Commits (Conventional Commits)
```
<type>(<scope>): <imperative summary, <= 72 chars>

optional body explaining why
Refs #12
```
Scopes: `gateway`, `user`, `market`, `order`, `matching`, `portfolio`, `alert`, `notification`, `report`, `common`, `frontend`, `shell`, `infra`, `docs`.
Examples: `feat(order): publish orders.place command`, `fix(notification): dedupe redis messages`.

## Pull requests
1. Open a PR early (draft is fine) using the template; link the issue with `Closes #N`.
2. Keep PRs small (< ~400 changed lines where possible).
3. CI must pass; update docs in the same PR.
4. Squash-merge; the PR title becomes the commit message (Conventional Commit format).
5. Delete the branch after merge.

If you work alone, still open PRs: they document the history and are good evidence for grading.

## Issues
- Use the templates (feature / task / bug). Every issue has acceptance criteria, a milestone, and labels.
- Mark the **rubric item** it contributes to, if any.

## Labels
| Label | Meaning |
|---|---|
| `feat`, `task`, `bug`, `docs` | issue type |
| `backend`, `frontend`, `infra`, `security`, `messaging` | area |
| `rubric` | directly earns rubric points |
| `blocked` | waiting on something |

## Milestones
`M0` … `M10`, defined in [roadmap](../project/roadmap.md).

## Branch protection (GitHub settings)
Require PR before merging, require status check `CI`, disallow force pushes to `main`.
