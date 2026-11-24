# Git Workflow

Use one short-lived branch per issue: `feat/<issue>-<slug>`, `fix/<issue>-<slug>`, or `docs/<issue>-<slug>`. Use Conventional Commits, for example `feat(issue): add assignment and status transitions` or `docs(architecture): update event flow`. Keep API, event schemas, diagrams, and deployment docs aligned when boundaries change.

## Protecting `main`

Configure a GitHub repository ruleset so changes reach `main` through pull requests and passing CI:

1. Open **Settings → Rules → Rulesets** and create a **New branch ruleset**.
2. Give it a clear name, set enforcement to **Active**, and target the `main` branch.
3. Enable **Require a pull request before merging**. No minimum approval count is required by this project guardrail.
4. Enable **Require status checks to pass** and add **`CI / ci`** as the required check. This is the stable aggregate job in `.github/workflows/ci.yml`.
5. Save the ruleset.

The aggregate check passes when all present modules pass. Backend checks run when `backend/gradlew`, `backend/settings.gradle`, and `backend/build.gradle` exist. The frontend job starts when `frontend/package.json` exists and requires `frontend/package-lock.json` plus `lint` and `build` scripts; tests run when a `test` script exists. Compose validation and image builds run when `docker-compose.yml` exists. Jobs for modules that are not present yet are skipped and do not fail the aggregate check. See the [README](../../README.md) for the CI overview.
