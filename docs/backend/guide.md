# Backend Implementation Guide

This guide defines the planned backend layout and the job of each build, source, configuration, deployment, and test file. These are implementation conventions: create the files as the roadmap reaches each milestone. Service boundaries and ports are in the [service catalog](../architecture/services.md); detailed behavior is in each [service guide](services/).

## Backend repository map

```text
backend/
├── settings.gradle.kts              # Declares included Gradle projects.
├── build.gradle.kts                 # Shared plugin versions and root tasks.
├── gradle.properties                # Gradle/JVM build settings.
├── gradlew, gradlew.bat              # Wrapper launchers; pin Gradle version.
├── gradle/wrapper/gradle-wrapper.properties # Wrapper distribution/version.
├── gradle/libs.versions.toml         # Central dependency and plugin versions.
├── build-logic/                      # Reusable Gradle convention plugins.
│   ├── build.gradle.kts              # Build logic plugin dependencies.
│   └── src/main/kotlin/              # java-library, spring-service, spring-function plugins.
├── common/                           # Shared contracts and technical support only.
└── <service>/                        # One independent deployable Gradle application per service.
```

### Root and shared module files

| File or directory | Responsibility |
|---|---|
| `settings.gradle.kts` | Gives the build its name and includes `common` and every service project. |
| `build.gradle.kts` | Applies shared quality/build configuration and aggregates verification tasks; business dependencies belong in service build files. |
| `gradle/libs.versions.toml` | Single place for pinned plugin/library versions and dependency aliases. |
| `gradle.properties` | Sets Gradle memory, parallelism, and build cache options; contains no secrets. |
| `build-logic/` | Holds convention plugins so service modules use consistent Java, Spring, test, and packaging configuration. |
| `common/build.gradle.kts` | Declares only libraries required by shared contracts/utilities. |
| `common/src/main/java/com/stocksim/common/messaging/Envelope.java` | Shared event envelope: id, type, schema version, timestamp, payload. |
| `common/src/main/java/com/stocksim/common/messaging/event/` | Versioned event DTOs shared by producers and consumers. |
| `common/src/main/java/com/stocksim/common/messaging/command/` | Versioned command DTOs shared by RabbitMQ publishers and consumers. |
| `common/src/main/java/com/stocksim/common/security/` | Reusable JWT claim extraction and service security helpers; each app still declares its own access rules. |
| `common/src/main/java/com/stocksim/common/web/` | Shared error response conventions and request correlation helpers; no domain logic. |
| `common/src/test/` | Serialization/compatibility tests for shared payloads. |

Avoid turning `common` into a shared domain model or shared database layer. A service owns its entities and persistence model.

## Standard Spring service layout

Every normal service follows this pattern; its service-specific guide lists which adapters and files it needs.

```text
<service>/
├── build.gradle.kts
├── Dockerfile
├── src/main/java/com/stocksim/<service>/
│   ├── <Service>Application.java
│   ├── api/
│   ├── application/
│   ├── domain/
│   └── infrastructure/
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/V1__initial_schema.sql
└── src/test/java/com/stocksim/<service>/
```

| File or directory | Responsibility |
|---|---|
| `<service>/build.gradle.kts` | Service-specific dependencies and test configuration; versions come from the catalog. |
| `Dockerfile` | Builds a small, non-root runtime image for this service. |
| `<Service>Application.java` | Spring Boot entry point and component-scan root. |
| `api/` | HTTP controllers, request/response DTOs, validation, and service-specific exception mapping. Never return JPA entities. |
| `application/` | Use cases, orchestration, transaction boundaries, and ports that the domain needs. |
| `domain/` | Service-owned entities/value objects, invariants, and domain policies; keep framework dependencies out where practical. |
| `infrastructure/` | JPA, broker, Redis, HTTP client, cryptography, and framework adapter implementations. |
| `application.yml` | Safe defaults, server settings, actuator, serialization, and environment-variable placeholders. Profile-specific values come from environment, not committed secrets. |
| `db/migration/V<n>__*.sql` | Forward-only Flyway schema change for this service's own database. |
| `src/test/...` | Unit, slice, and integration tests mirroring production packages. |

Do not create profile files just to hold hostnames; configure Compose and Kubernetes through environment variables. Keep `application-local.yml` only for genuinely local behavior and never place credentials in it.

## Service guides

| Guide | Main responsibilities |
|---|---|
| [API Gateway](services/api-gateway.md) | Public routing, JWT verification, throttling, CORS, and safe header handling. |
| [User Service](services/user-service.md) | Registration, login, user profile, token signing, and public JWKS. |
| [Market Data Service](services/market-data-service.md) | Symbols, simulated/real providers, normalized quotes, Kafka ticks, and Redis latest prices. |
| [Order Service](services/order-service.md) | Authenticated order API, reservations, order state, RabbitMQ commands, and Kafka outcome events. |
| [Matching Service](services/matching-service.md) | Order command handling, matching rules, persisted open orders, and trade/rejection events. |
| [Portfolio Service](services/portfolio-service.md) | Funds/reservations, positions, P&L, report lifecycle, and report FaaS invocation. |
| [Alert Service](services/alert-service.md) | User-owned price rules, tick evaluation, and notification commands. |
| [Notification Service](services/notification-service.md) | JWT-authenticated STOMP connections, event conversion, and Redis cross-instance fan-out. |
| [Report Function](services/report-function.md) | Stateless PDF generation function packaged for OpenFaaS and local Compose invocation. |

## Cross-cutting implementation rules

- Services own their data and schema; other services call APIs or consume events.
- Public APIs use DTO validation and RFC 7807 `ProblemDetail`; enforce ownership in the owning service.
- Message consumers are idempotent and acknowledge only after durable work succeeds.
- Configuration is supplied through environment variables; secrets use local ignored `.env` or Kubernetes Secrets.
- Every service exposes health/readiness and OpenAPI where it serves REST.
- Add tests in the same change as behavior; see [testing strategy](../development/testing.md).
- Update the service catalog, REST reference, messaging contracts, diagrams, and deployment docs when a boundary or contract changes.

## Dependency map

| Capability | Typical library |
|---|---|
| REST + validation | Spring MVC, Jakarta Validation, springdoc |
| JWT resource server | Spring Security OAuth2 Resource Server |
| Persistence | Spring Data JPA, PostgreSQL driver, Flyway |
| RabbitMQ / Kafka | Spring AMQP / Spring Kafka |
| Redis | Spring Data Redis |
| WebSocket | Spring WebSocket + STOMP |
| FaaS handler | Spring Cloud Function Web |
| Health and metrics | Spring Boot Actuator, Micrometer |

Use only dependencies the service actually needs.
