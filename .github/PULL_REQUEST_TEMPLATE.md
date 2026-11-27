## What does this PR do?

<!-- Summarize the change and why it is needed. -->

Closes #<issue-number>

## How to test it

<!-- List the commands or steps used to verify this change. -->

1.

## Screenshots (UI or diagram changes)

<!-- Add screenshots when this PR changes the UI or diagrams. -->

## Checklist

- [ ] Branch follows the project convention: `feat/<issue-number>-short-name`, `fix/<issue-number>-short-name`, or `docs/<issue-number>-short-name`.
- [ ] Commits follow Conventional Commits (`feat:`, `fix:`, `docs:`, and similar types).
- [ ] Relevant checks pass locally: `cd backend && ./gradlew build` and/or `cd frontend && npm run lint && npm test && npm run build`.
- [ ] New or changed behavior has tests, or is marked N/A for documentation or design changes.
- [ ] If the database schema changes, this adds a new Flyway migration and does not edit an applied migration.
- [ ] No secrets are committed; `.env.example` contains placeholders only.
- [ ] Documentation is updated when needed (`README.md`, `docs/`, or API/OpenAPI annotations).
- [ ] If using the project board, the issue is moved to **In Review**.
