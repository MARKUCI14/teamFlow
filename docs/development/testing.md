# Testing Strategy

| Level | Tools | Scope |
|---|---|---|
| Unit | JUnit 5, Mockito, AssertJ | Domain logic (matching rules, P&L math, alert evaluation) |
| Slice | `@WebMvcTest`, `@DataJpaTest` | Controllers (incl. security rules), repositories |
| Integration | Spring Boot Test + **Testcontainers** (Postgres, Kafka, RabbitMQ, Redis) | Messaging adapters, Flyway migrations, end-to-end per service |
| Contract | JSON schema/examples from `docs/architecture/messaging.md`, tests in `common` | Event/command compatibility |
| End-to-end | Docker Compose + a small script or Playwright | Register → login → place order → see notification |
| Load | k6 (`scripts/load/`) | Scaling demo, latency under load |
| Frontend | Vitest + Testing Library; Playwright (optional) | Components, hooks, key user flows |

## Rules
- Business rules are tested without Spring where possible (fast).
- Every consumer has a duplicate-delivery test (idempotency).
- Every secured endpoint has tests for 401 (no token), 403 (wrong role/owner), and success.
- Integration tests use `@ServiceConnection`/Testcontainers; no reliance on a locally running broker.
- CI runs everything except Playwright/load tests (run manually before milestones).

## Coverage guideline
Aim for meaningful coverage of domain and application layers (~70%+); do not chase numbers in glue code.
