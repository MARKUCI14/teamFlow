# Repository Guidance

## Project

**TeamFlow** is a role-based project and issue management system for the Service-Oriented Architectures course. Stakeholders submit bugs/suggestions; project managers triage them into developer issues; developers update issues and log time. See `docs/architecture/overview.md` and `docs/development/rubric-checklist.md`.

Every feature must support the core workflow or a rubric item. This repository currently contains documentation and setup plans; do not claim planned software is implemented.

## Planned stack

Java 21, Spring Boot, Gradle, PostgreSQL (database per service), RabbitMQ for commands, Kafka for events, Redis for WebSocket fan-out/rate limits, Spring Cloud Function/OpenFaaS, React + TypeScript Module Federation, nginx, Docker Compose, Kubernetes.

## Planned modules

Backend: common, api-gateway, user-service, project-service, submission-service, issue-service, worklog-service, notification-service, report-service, report-function.

Frontend: shell, shared, projects, stakeholder, developer, admin.

## Architecture rules

1. Each service owns its data; never read another service's tables.
2. Clients use the gateway; internal APIs are private and authenticated.
3. RabbitMQ carries commands; Kafka carries committed domain facts. See `docs/architecture/messaging.md`.
4. Version event contracts and make consumers idempotent.
5. Enforce global roles and project membership at the service boundary; UI guards are not security.
6. Keep services stateless where possible; shared realtime delivery uses Redis pub/sub.
7. Never hard-code secrets or log tokens/passwords. Use RFC 7807 errors and OpenAPI.
8. Microfrontends communicate through shared shell contracts, not direct imports.
9. Java packages use `com.teamflow.<service>.{api,application,domain,infrastructure}`.
10. Keep REST, event, C4/UML, and deployment docs synchronized with changes.

Start from `README.md`; implementation sequence is in `docs/project/roadmap.md`.
