# Coding Standards

## Java / Spring
- Java 21, Spring Boot 4.x, and the compatible Spring Cloud release train from the version catalog. Use records for DTOs and events; constructor injection only.
- Package layout: `edu.portikmark.teamflow.<module-package>.{api, application, domain, infrastructure}`. Use the existing module roots `gateway`, `users`, `workmanagement`, `notifications`, `reports`, `reportfunction`, and `common`.
  - `api`: controllers, request/response DTOs, exception handlers.
  - `application`: use cases / services, transaction boundaries.
  - `domain`: entities and domain logic, no Spring dependencies where practical.
  - `infrastructure`: JPA repositories, messaging adapters, REST clients, config.
- Entities are never returned from controllers; map to DTOs.
- Validation with `jakarta.validation` annotations on request DTOs.
- Errors: `@RestControllerAdvice` returning RFC 7807 `ProblemDetail`.
- Durations are stored as integer minutes; timestamps use `Instant` in UTC.
- Database changes only through Flyway migrations (`src/main/resources/db/migration/V<n>__desc.sql`); `ddl-auto: validate`.
- Logging: SLF4J, structured key=value, include `projectId`, `issueId`, `userId`, `eventId` where relevant. No secrets or tokens.

## Static analysis

Every Java module applies the shared checks from `backend/lint.gradle`. Run them with `cd backend && ./gradlew check` or `./gradlew build` using Java 21, which is the project toolchain target.

| Tool | Version | Configured rules |
|---|---|---|
| Checkstyle | 10.12.4 | Modified Google Java Style with four-space indentation and a 120-character Java line limit. See [`backend/checkstyle.xml`](../../backend/checkstyle.xml). |
| PMD | 7.7.0 on Java versions above 17; otherwise 6.45.0 | Uses the selected rules from best-practices, code-style, design, documentation, error-prone, multithreading, performance, and security categories, with project exclusions and overrides. See [`backend/pmd-ruleset.xml`](../../backend/pmd-ruleset.xml). |
| SpotBugs | 4.9.4 | Runs at maximum effort with the report threshold set to low confidence; known patterns are excluded by the project filter. See [`backend/spotbugs-filter.xml`](../../backend/spotbugs-filter.xml). |

The `violations` task combines all three reports. The analyzers are configured to continue after findings so the combined report can be produced, but the aggregate fails the build for any finding at `INFO` severity or higher (`maxViolations = 0`). The `check` task runs this aggregate, so findings also fail `build` and backend CI.

## REST conventions
- Base path `/api/<resource>`, plural nouns, JSON, kebab-case paths, camelCase fields.
- Status codes: 200/201 success, 202 for async acceptance, 400 validation, 401/403 auth, 404, and 409 conflict.
- Pagination: `?page=0&size=20&sort=createdAt,desc`.
- Every controller method documented with springdoc annotations; the generated OpenAPI feeds the frontend types.
- Idempotency: Mutation endpoints accept an optional `Idempotency-Key` header where retries could create duplicate work.

## TypeScript / React
- `strict: true`; no `any` (use `unknown` + narrowing).
- Function components and hooks; one component per file; `PascalCase.tsx`.
- Server state with TanStack Query; local UI state with `useState`/`useReducer`.
- API client types generated from OpenAPI (`openapi-typescript`); do not hand-write DTO types.
- ESLint + Prettier enforced in CI; imports ordered.
- Accessibility basics: labels, keyboard focus, semantic elements.

## Naming
- Services: `kebab-case`. Kafka topics / queues: dotted lowercase. Env vars: `UPPER_SNAKE_CASE`.
- Tests: `<Class>Test` (unit), `<Class>IT` (integration).

## Documentation in code
Comment *why*, not *what*. Public contracts (events, REST) live in docs and OpenAPI, not in comments.
