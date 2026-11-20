# Rubric Checklist

Grade = 80% S (system) + 20% P (tutorial). Items below are the ones listed in the assessment. Tick them as they are implemented **and demonstrable**.

| # | Rubric item | Pts | Implementation in StockSim | Evidence for grading | Done |
|---|---|---|---|---|---|
| 1 | Secured REST API, web server with > 2 microservices | 1 | JWT (RS256) at gateway + resource servers; 8 services | Swagger, security tests, `docs/architecture/security.md` | [ ] |
| 2 | Load balancer or scalable WebSockets | 1 | nginx upstreams (gateway x2, notifications x2) + Redis pub/sub | `docker compose up --scale`, demo of instance switch | [ ] |
| 3 | Message broker | 1 | RabbitMQ: `orders.place`, `orders.cancel`, `notifications.send`, DLQ | RabbitMQ UI, integration tests | [ ] |
| 4 | Event streaming | 1 | Kafka: `market.ticks`, `trades.executed`, `orders.rejected` | Kafka UI, consumer groups | [ ] |
| 5 | FaaS | 1 | `report-function` (OpenFaaS on K8s) | `faas-cli` invoke, PDF output | [ ] |
| 6 | Web app consuming REST + server notifications | 1 | React + TS, STOMP WebSocket | Live demo | [ ] |
| 7 | Micro frontend architecture | 1 | Module Federation: shell + market + trading + admin | Independent builds, runtime composition | [ ] |
| 8 | Containers for deployment | 1 | Dockerfile per service, Compose, Kubernetes manifests | `docker compose up`, `kubectl apply -k` | [ ] |
| 9 | Documentation (UML, C4) | 1 | `docs/architecture/c4-model.md`, `uml-diagrams.md` | Rendered diagrams on GitHub | [ ] |

The listed system items add up to 9 points. Confirm with the professor how the system grade is totaled and whether any additional point or base-system component applies; update this checklist when clarified. Do not assume an extra criterion that is not in the assessment text.

The full target system is tracked in the roadmap. For a manageable delivery order, first demonstrate the core journey (login → select simulated market → place order → update portfolio → receive notification), then complete remaining rubric capabilities and deployment evidence. The real-data market mode is an additional feature and should use the same normalized market-data contract as simulation.

## Tutorial (P, 20%)
- [ ] ~3 pages on one topic implemented in the system (see [tutorial outline](../tutorial/outline.md))
- [ ] Working example in a public Git repository (this repo or a trimmed extract)
- [ ] Steps are reproducible from a clean clone

## Demo script (rehearse before presenting)
1. `docker compose up --build`, open the app.
2. Register/login, show the JWT-secured API in Swagger.
3. Watch live prices (Kafka stream), place an order (RabbitMQ), see the WebSocket notification.
4. Scale `order-service` and `notification-service`; show requests/connections distributed.
5. Request a report (FaaS); download the PDF.
6. Show Kubernetes deployment and the C4/UML docs.
