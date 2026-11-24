# ADR-0001: Monorepo with Gradle multi-project backend

- Status: accepted

## Context
Four internal backend microservices plus an API gateway, a shared `common` module, and a report function are developed by a very small team and evaluated as one system. The React frontend uses a shell and two microfrontend remotes.

## Decision
One Git repository. `backend/` is a Gradle multi-project build (Kotlin DSL, version catalog, shared convention plugin, `common` module for event contracts). It contains `user-service`, `work-management-service`, `notification-service`, and `report-service`, plus the edge `api-gateway` and `report-function`. `frontend/` uses npm workspaces for the shell and stakeholder/team remotes.

## Consequences
+ Atomic changes across services and contracts, one CI pipeline, easy for graders to clone and run.
- Services are not independently versioned; mitigated by keeping `common` contracts backward compatible.

## Alternatives considered
Repo per service (more realistic, far more overhead); Maven (equally valid, Gradle chosen by the team).
