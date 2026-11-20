# ADR-0001: Monorepo with Gradle multi-project backend

- Status: accepted

## Context
Nine backend modules plus four micro frontends are developed by a very small team and evaluated as one system.

## Decision
One Git repository. `backend/` is a Gradle multi-project build (Kotlin DSL, version catalog, shared convention plugin, `common` module for event contracts). `frontend/` uses npm workspaces.

## Consequences
+ Atomic changes across services and contracts, one CI pipeline, easy for graders to clone and run.
- Services are not independently versioned; mitigated by keeping `common` contracts backward compatible.

## Alternatives considered
Repo per service (more realistic, far more overhead); Maven (equally valid, Gradle chosen by the team).
