# Architecture Overview

## Goal
A stock trading simulator for learning: users register, receive virtual cash, trade symbols using either simulated prices or real market data, follow their portfolio, set price alerts, and request PDF reports. The point of the project is the **architecture**: cooperating services using REST, message brokers, event streaming, a FaaS, WebSockets, load balancing, and containers. This document describes the target system; implementation progress is tracked in the [roadmap](../project/roadmap.md) and [rubric checklist](../development/rubric-checklist.md).

## Actors
- **Trader**: registers, trades, tracks portfolio, sets alerts, requests reports.
- **Admin**: manages symbols, chooses market data mode, configures simulation speed/volatility, and views users.

## Main capabilities
1. Authentication and authorization (JWT, roles `USER`, `ADMIN`).
2. Live market prices from a selectable simulated provider or real-data provider, streamed to clients.
3. Order placement (market and limit, buy and sell), cancellation.
4. Order execution by a matching engine.
5. Portfolio: cash, holdings, realized/unrealized P&L.
6. Price alerts ("notify me when AAPL > 200").
7. Real-time notifications (order filled/rejected, alert fired, report ready).
8. On-demand PDF portfolio report (FaaS).

## Architectural style
- Microservices with **database per service**, deployed as containers.
- **Synchronous REST** for client-facing queries and commands through an API gateway.
- **RabbitMQ** for commands/work queues (order placement, cancellation, notification delivery).
- **Kafka** for event streams (price ticks, executed trades, rejections).
- **Redis** for the market-data latest-price cache and WebSocket pub/sub fan-out across notification-service instances.
- **Market data provider interface** separates the deterministic simulator from a real-data adapter. Provider selection is explicit and shared by REST, Kafka ticks, alerts, and matching; provider credentials and terms are handled through configuration.
- **FaaS** for stateless, bursty PDF generation.
- **nginx** as edge load balancer; replicated stateless services.
- **Micro frontends** composed in a shell app.

## Key flows (details in [uml-diagrams](uml-diagrams.md))

**Place order**: client → gateway → order-service (validate, reserve funds in portfolio-service, persist `PENDING`) → RabbitMQ `orders.place` → matching-service executes → Kafka `trades.executed` → portfolio-service updates holdings, order-service marks `FILLED`, notification-service pushes WebSocket message.

**Price alert**: market-data-service → Kafka `market.ticks` → alert-service detects crossing → RabbitMQ `notifications.send` → notification-service → WebSocket.

**Report**: client requests a report → portfolio-service records `PENDING` and returns `202` → report generation invokes FaaS → portfolio-service stores the PDF and marks it `READY` → RabbitMQ `notifications.send` ("report ready").

## Rubric mapping

| Rubric item | Where |
|---|---|
| Secured REST API, >2 microservices | gateway + 8 services, JWT (see [security](security.md)) |
| Load balancing / scalable WebSockets | nginx upstreams; Redis pub/sub for notification-service ([scalability](scalability.md)) |
| Message broker | RabbitMQ ([messaging](messaging.md)) |
| Event streaming | Kafka ([messaging](messaging.md)) |
| FaaS | `report-function` (Spring Cloud Function, OpenFaaS) ([ADR-0003](adr/0003-faas-spring-cloud-function.md)) |
| Web app + server notifications | React app, STOMP over WebSocket |
| Micro frontend | Module Federation: shell, market, trading, admin |
| Containers | Docker Compose + Kubernetes |
| Documentation | this folder: C4, UML |
