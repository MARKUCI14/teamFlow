# Roadmap and Backlog

Keep the system runnable at the end of every milestone. Issues are created by `scripts/create-github-issues.sh`. The full target remains in scope, but each phase prioritizes a small demonstrable path before adding operational depth.

## Delivery strategy

1. **Runnable core demo (M1–M5):** secured REST, simulated prices, order-to-portfolio flow, RabbitMQ and Kafka, then authenticated WebSocket notifications. This establishes the main user journey early.
2. **Complete system capabilities (M6–M8):** alerts, reports through FaaS, then the web shell and micro frontends. Start with the key screens and add secondary screens within the same milestone.
3. **Assessment evidence and deployment depth (M9–M10):** real-data provider, load balancing/scaling demonstration, Kubernetes/OpenFaaS deployment, final diagrams and tutorial. Keep Docker Compose as the easiest clean-clone demo path.

The real-data provider is an additional market mode, not a replacement for the simulator. Implement it behind the same provider interface and normalized tick contract. Choose a provider based on free-tier availability, licensing, and rate limits before implementing its adapter; keep simulation available when credentials or network access are unavailable.

| Milestone | Goal | Rubric items |
|---|---|---|
| **M0 Setup** | Repo, docs, CI, templates, ADRs | Documentation |
| **M1 Foundation** | Gradle project, `common`, user-service, JWT, api-gateway, Compose infra | Secured REST, containers |
| **M2 Market data** | market-data-service, simulated provider, normalized Kafka topic, Redis price cache | Event streaming |
| **M3 Trading core** | order-service, RabbitMQ, matching-service, Kafka trades | Message broker, event streaming |
| **M4 Portfolio** | portfolio-service, reservations, idempotent consumers | Microservices > 2 |
| **M5 Realtime** | notification-service, STOMP, Redis pub/sub, multi-instance | Scalable WebSockets, notifications |
| **M6 Alerts** | alert-service, RabbitMQ notifications | Message broker |
| **M7 FaaS** | report-function, OpenFaaS, portfolio integration | FaaS |
| **M8 Frontend** | shell + market + trading + admin MFEs | Web app, micro frontend |
| **M9 Scale and deploy** | nginx LB and scaling evidence first; real-data provider; K8s manifests, HPA, load test | Load balancing, containers |
| **M10 Finish** | C4/UML final, tutorial repo + text, demo rehearsal | Documentation, P grade |

## Suggested order inside a milestone
Contract (docs + `common`) → service skeleton → persistence → API/messaging → tests → Docker → docs update.

## Risks
| Risk | Mitigation |
|---|---|
| Too many moving parts locally | Compose profiles, infra-only mode, resource limits |
| Module Federation + Vite friction | Prototype shell + one remote in M1/M8 early; fallback to webpack Module Federation |
| JVM cold start on FaaS | `min-replicas: 1` or native image; document |
| Kafka/RabbitMQ complexity | Start with happy path, then add retry/DLQ |
| Scope creep | Anything not tied to a rubric item or the core trade flow goes to "later" |
