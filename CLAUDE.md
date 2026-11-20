# CLAUDE.md

Guidance for Claude Code (and humans) working in this repository. Read this first, then the linked docs.

## Project

**StockSim** is a stock trading simulator built as a university project for *Arhitecturi orientate pe servicii* (Service-Oriented Architectures). Users trade virtual cash against simulated live prices, track a portfolio, set price alerts, and receive real-time notifications.

The project is graded against a fixed rubric (see `docs/development/rubric-checklist.md`). **Every feature must either serve a rubric item or the core trading flow.** Do not add scope that does neither.

## Stack

| Area | Choice |
|---|---|
| Backend | Java 21, Spring Boot 3.x, Spring Security (OAuth2 resource server / JWT), Spring Cloud Gateway, Spring Data JPA, Flyway |
| Build | Gradle multi-project (Kotlin DSL, version catalog) in `backend/` |
| Messaging | RabbitMQ (commands), Kafka (event streams), Redis (cache + WebSocket pub/sub) |
| FaaS | Spring Cloud Function; OpenFaaS on Kubernetes, plain container on Compose |
| Database | PostgreSQL (one instance, one database per service) |
| Frontend | React + TypeScript (strict), Vite, Module Federation (micro frontends) in `frontend/` |
| Edge / LB | nginx |
| Deploy | Docker Compose (`docker-compose.yml`) and Kubernetes (`deploy/k8s`, kustomize) |
| Docs | Markdown + Mermaid (C4, UML) in `docs/` |

## Repository layout

```
soa/
├── CLAUDE.md
├── README.md
├── docker-compose.yml          # full local stack
├── backend/                    # Gradle multi-project root
│   ├── common/                 # event contracts, shared security + error handling
│   ├── api-gateway/  user-service/  market-data-service/  order-service/
│   ├── matching-service/  portfolio-service/  alert-service/
│   ├── notification-service/  report-function/
├── frontend/                   # npm workspaces
│   ├── shell/  market/  trading/  admin/  shared/
├── deploy/
│   ├── nginx/  postgres/       # nginx.conf, init-db.sql
│   └── k8s/                    # base + overlays, openfaas/
├── scripts/                    # helper scripts (issues, load tests)
├── docs/                       # all documentation (start at docs/README.md)
└── .github/                    # issue/PR templates, CI workflow
```

## Commands

```bash
# Infrastructure only (for running services from the IDE)
docker compose up -d postgres redis rabbitmq kafka

# Whole stack
docker compose up --build

# Backend
cd backend && ./gradlew build                    # compile + test everything
cd backend && ./gradlew :order-service:bootRun   # run one service
cd backend && ./gradlew :order-service:test

# Frontend
cd frontend && npm ci
cd frontend && npm run dev                       # all micro frontends
cd frontend && npm run lint && npm run test && npm run build

# Kubernetes (local cluster: kind / k3d / Docker Desktop)
kubectl apply -k deploy/k8s/overlays/local
```

## Architecture rules (do not break these)

1. **Database per service.** No service reads another service's tables. Share data through APIs or events.
2. **Clients only talk to the gateway** (through nginx). Internal endpoints live under `/internal/**` and are never routed by the gateway.
3. **RabbitMQ = commands** (one consumer should act: place order, cancel order, send notification). **Kafka = events/facts** (many independent consumers: price ticks, executed trades). See `docs/architecture/messaging.md`.
4. **Event/command payloads live in `backend/common`** and are versioned. Never change a published schema incompatibly; add a field or bump the version.
5. **Consumers are idempotent** (dedupe on `eventId`/`orderId`). Assume at-least-once delivery.
6. **Stateless services** (except matching-service, see `docs/architecture/scalability.md`). Anything that must survive a restart goes to a database; anything shared across instances goes to Redis.
7. **Every REST endpoint is secured** unless explicitly listed as public (`/auth/**`, health). Never log tokens or passwords.
8. **Config via environment variables**, never hard-coded hosts or secrets. Profiles: `local`, `docker`, `k8s`.
9. **Errors** use RFC 7807 `ProblemDetail`. APIs are documented with OpenAPI (springdoc).
10. **Micro frontends** only communicate through the contracts in `frontend/shared` (auth store, event bus). No imports between `market`, `trading`, `admin`.

## Conventions

- Branching, commits, PRs: `docs/development/git-workflow.md` (Conventional Commits, squash-merge, branch per issue).
- Code style and testing: `docs/development/coding-standards.md`, `docs/development/testing.md`.
- Backend structure: `docs/backend/guide.md`. Frontend structure: `docs/frontend/guide.md`.
- Java packages: `com.stocksim.<service>.{api,application,domain,infrastructure}`.
- Kafka topics / RabbitMQ names: lowercase, dot-separated (`market.ticks`, `orders.place`).

## How to work in this repo (instructions for Claude)

- **Read before coding:** the relevant `docs/` page and the linked GitHub issue. Follow the acceptance criteria.
- **Small, reviewable changes:** one issue per branch/PR. Do not refactor unrelated code.
- **Keep docs in sync:** if you change a service boundary, topic, endpoint, or diagram-relevant behavior, update `docs/` in the same PR (C4/UML in `docs/architecture/`, endpoints in `docs/api/rest-api.md`).
- **Ask before adding technology** that is not in the stack table, and record significant decisions as an ADR in `docs/architecture/adr/`.
- **Tests are part of the change.** Unit tests for logic; Testcontainers integration tests for DB/broker code.
- **Never commit secrets**, `.env` files, or generated build output. Use `.env.example` for documentation.
- **Run before finishing:** `./gradlew build` (backend) and `npm run lint && npm run test` (frontend) for the areas you touched.

## Definition of done

Code builds, tests pass, CI is green, endpoints secured and documented, docs updated, PR template completed, linked issue closed.
