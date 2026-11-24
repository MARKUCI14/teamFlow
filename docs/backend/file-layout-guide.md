# TeamFlow File Layout Guide

This guide describes the repository layout. The Gradle root, module build files, application entry points, and health configuration exist; the remaining feature paths are targets to add when implemented.

Create files when a feature needs them. Do not add empty classes or duplicate shared business logic. `Review` in an issue means inspect the listed paths and update them only if the change requires it.

## Repository layout

```text
.
├── .github/workflows/ci.yml
├── backend/                         # Gradle multi-project build
│   ├── settings.gradle
│   ├── build.gradle
│   ├── gradle/libs.versions.toml
│   ├── gradlew
│   ├── gradle/wrapper/              # Wrapper JAR and pinned Gradle distribution properties
│   ├── build-logic/                 # shared Gradle conventions, only where useful
│   ├── common/                      # versioned messages and common error types
│   ├── api-gateway/
│   ├── user-service/
│   ├── work-management-service/
│   ├── notification-service/
│   ├── report-service/
│   └── report-function/             # stateless Spring Cloud Function
├── frontend/                        # npm workspace
│   ├── package.json
│   ├── shell/
│   ├── stakeholder/
│   ├── team/
│   └── shared/
├── docker-compose.yml               # root entry point used by local docs and CI
├── deploy/nginx/nginx.conf
├── .env.example
└── docs/
```

The four internal microservices are `user-service`, `work-management-service`, `notification-service`, and `report-service`. `api-gateway` is the public edge. `report-function` is the FaaS component. Neither is counted as an internal microservice.

## Backend conventions

Each backend module has its own `build.gradle`, application entry point, configuration, and tests. Spring services use Java 21 and the package layers `api`, `application`, `domain`, and `infrastructure`; add a layer only when it has real responsibilities.

```text
<module>/
├── build.gradle
└── src/
    ├── main/java/edu/portikmark/teamflow/<module-package>/
    │   ├── <Module>Application.java
    │   ├── api/                     # controllers, request/response DTOs
    │   ├── application/             # use cases and transaction boundaries
    │   ├── domain/                  # business rules and domain types
    │   ├── infrastructure/          # database, broker, HTTP, and security adapters
    │   └── config/                  # Spring configuration
    ├── main/resources/
    │   ├── application.yml
    │   └── db/migration/            # Flyway SQL when the module owns a schema
    └── test/java/edu/portikmark/teamflow/<module-package>/
```

Use valid Java package names (no hyphens). Controllers accept and return DTOs, not persistence entities. Put schema changes in Flyway migrations and set Hibernate to validate the schema. Each service owns its database access; other services use documented APIs or events.

## Backend modules

### `backend/common`

Keep this module small and limited to contracts that genuinely cross service boundaries: the versioned event envelope and shared ProblemDetail helper. Do not add service API DTOs or business logic.

```text
backend/common/src/main/java/edu/portikmark/teamflow/common/
├── events/                          # versioned event envelope and shared event metadata
└── errors/                          # shared ProblemDetail/error contract, if needed
```

Do not put service entities, repositories, or business workflows here. Keep API DTOs service-owned unless they are explicitly part of a shared versioned contract.

### `backend/api-gateway`

The gateway routes public REST calls and validates JWTs. It has no business database or domain model.

```text
backend/api-gateway/
├── src/main/java/edu/portikmark/teamflow/gateway/
│   ├── GatewayApplication.java
│   ├── config/                      # route and CORS configuration
│   └── security/                    # JWT resource-server configuration
├── src/main/resources/application.yml
└── src/test/java/edu/portikmark/teamflow/gateway/
```

Keep business authorization in the service that owns the resource. Route `/ws` through nginx to notification-service as documented by deployment configuration.

### `backend/user-service`

Owns accounts, credentials, global roles, refresh sessions, and token signing/JWKS.

```text
backend/user-service/src/main/java/edu/portikmark/teamflow/users/
├── UserServiceApplication.java
├── api/                             # AuthController, UserController, DTOs
├── application/                     # registration, authentication, token use cases
├── domain/                          # User, RefreshSession, account rules
├── infrastructure/
│   ├── persistence/                  # repositories and persistence mappings
│   └── security/                     # password hashing and signing-key adapter
└── config/                          # endpoint access rules and service configuration
```

Also add `src/main/resources/application.yml`, Flyway migrations under `src/main/resources/db/migration/`, and tests under `src/test/java/edu/portikmark/teamflow/users/`. Project membership and project roles belong to work-management-service, not here.

### `backend/work-management-service`

Owns projects, memberships, submissions, triage, issues, comments, and work logs. Keep each feature in this service; organize by feature inside the standard layers.

```text
backend/work-management-service/src/main/java/edu/portikmark/teamflow/workmanagement/
├── WorkManagementApplication.java
├── project/                         # API, use cases, domain, persistence
├── membership/                      # project roles and access policy
├── submission/                      # intake and triage
├── issue/                           # lifecycle, assignment, comments
├── worklog/                         # time entry and summaries
├── messaging/                       # Kafka event publication/outbox adapter
└── config/
```

For a feature, keep its controller and DTOs in `api`, its use case and transaction logic in `application`, rules in `domain`, and JPA/Kafka code in `infrastructure`. Add Flyway migrations and tests only as those features require them. Do not create separate project, submission, issue, or work-log microservices.

### `backend/notification-service`

Consumes Kafka facts and sends authenticated STOMP notifications. Start with one instance; Redis fan-out is not part of the plan.

```text
backend/notification-service/src/main/java/edu/portikmark/teamflow/notifications/
├── NotificationServiceApplication.java
├── messaging/                       # Kafka listeners and event-to-notification mapping
├── websocket/                       # STOMP endpoint, destinations, principal delivery
├── security/                        # WebSocket token/principal configuration
└── config/
```

Add `application.yml` and tests for event handling, authorization, and destination routing. Do not give this service direct access to work-management tables.

### `backend/report-service`

Owns report requests, job status, and result metadata. It checks access through work-management-service, sends a RabbitMQ command, and records the result.

```text
backend/report-service/src/main/java/edu/portikmark/teamflow/reports/
├── ReportServiceApplication.java
├── api/                             # report request, status, and download endpoints
├── application/                     # authorization, job lifecycle, orchestration
├── domain/                          # report job/status/result metadata
├── infrastructure/
│   ├── persistence/                  # report-owned records
│   ├── messaging/                   # reports.generate publisher/consumer adapter
│   └── workmanagement/               # scoped snapshot client
└── config/
```

Add `application.yml`, migrations, and tests for authorization, job lifecycle, and RabbitMQ handling as needed.

### `backend/report-function`

The report function is stateless: it receives an authorized project snapshot and returns a PDF. It has no user database credentials and does not own report job state.

```text
backend/report-function/
├── build.gradle
├── src/main/java/edu/portikmark/teamflow/reportfunction/
│   ├── ReportFunctionApplication.java
│   ├── GenerateReportFunction.java
│   ├── api/                          # input/output payload types
│   └── pdf/                          # PDF formatting code
├── src/main/resources/application.yml
└── src/test/java/edu/portikmark/teamflow/reportfunction/
```

Add the function container and OpenFaaS deployment configuration when implementing its deployment. Keep the function callable locally for development.

## Frontend workspaces

The React + TypeScript frontend uses an npm workspace. The shell owns login, routing, and the shared notification connection. Remotes do not import one another.

```text
frontend/
├── package.json                     # workspace scripts and shared tooling
├── shell/
│   ├── package.json
│   ├── vite.config.ts                # shell and remote composition
│   └── src/                          # auth, routes, navigation, notifications
├── stakeholder/
│   ├── package.json
│   ├── vite.config.ts
│   └── src/                          # project progress and submissions
├── team/
│   ├── package.json
│   ├── vite.config.ts
│   └── src/                          # PM, developer, and admin views
└── shared/
    └── src/                          # API client, auth types, UI primitives
```

Put tests beside the relevant workspace or in its `src` test folders. Generate API types from OpenAPI rather than copying backend DTOs by hand. The UI may hide unavailable actions, but backend APIs enforce access.

## CI, configuration, and deployment files

```text
.github/workflows/ci.yml             # stable aggregate check; skip absent modules
.env.example                         # variable names and safe local defaults only
docker-compose.yml                   # local dependencies and full stack
deploy/nginx/nginx.conf              # REST balancing and WebSocket proxying
backend/<module>/Dockerfile           # module container when Compose needs it
frontend/<workspace>/Dockerfile       # frontend container when Compose needs it
```

Keep secrets in local environment values, not in tracked files. Compose starts PostgreSQL, RabbitMQ, Kafka, services, frontend components, nginx, and the report function as each becomes available. nginx balances API gateway replicas; notification-service starts with one instance.

## How to use this guide

Each implementation issue should list:

- **Create:** new files required by that issue.
- **Modify:** existing files expected to change.
- **Review:** related files to check for consistency; change them only when needed.

Use the [service catalog](../architecture/services.md), [REST API](../api/rest-api.md), [messaging design](../architecture/messaging.md), and [frontend guide](../frontend/guide.md) for behavior and contracts. Update those references when an implementation changes an approved boundary or public contract.
