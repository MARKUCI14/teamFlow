# Coding Standards

## Java / Spring
- Java 21, Spring Boot 3.x. Use records for DTOs and events; constructor injection only.
- Package layout: `com.teamflow.<service>.{api, application, domain, infrastructure}`.
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
- Formatting: Spotless (google-java-format AOSP or palantir) enforced in `./gradlew build`.

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
