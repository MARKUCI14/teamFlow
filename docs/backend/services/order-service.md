# Order Service Guide

## Purpose
Owns customer order requests and order lifecycle state. It checks request validity and ownership, coordinates fund/share reservations with portfolio-service, and sends accepted commands to matching-service through RabbitMQ.

## Planned module files

| Path (under `backend/order-service/`) | Responsibility |
|---|---|
| `build.gradle.kts` | MVC, security, JPA/Flyway, RabbitMQ, Kafka consumer, HTTP client, validation, tests. |
| `OrderServiceApplication.java` | Spring Boot entry point. |
| `api/OrderController.java` | Place, list, inspect, and cancel orders for the authenticated owner. |
| `api/dto/` | Validated order input and order/status responses. |
| `application/PlaceOrderUseCase.java` | Validates market/symbol rules, reserves assets, persists pending order, and publishes command. |
| `application/CancelOrderUseCase.java` | Checks ownership and cancellable status, requests cancellation, releases reservation when appropriate. |
| `application/OrderEventHandler.java` | Applies matching results and releases reservations on rejection/cancel. |
| `domain/Order.java` | Order data and allowed state transitions. |
| `domain/OrderStatus.java` | Lifecycle enum: pending, open, filled, rejected, cancelled. |
| `domain/OrderRepository.java` | Persistence port for order state and owner-filtered queries. |
| `infrastructure/persistence/` | JPA mapping/repository, idempotency keys, and Flyway schema. |
| `infrastructure/portfolio/PortfolioReservationClient.java` | Calls the portfolio internal reservation API with service credentials. |
| `infrastructure/messaging/OrderCommandPublisher.java` | Publishes place/cancel RabbitMQ commands with confirms. |
| `infrastructure/messaging/MatchingEventListener.java` | Consumes trade/rejection events from the order consumer group. |
| `config/SecurityConfig.java` | Requires USER for order APIs; ownership is checked in use cases. |
| `resources/application.yml` | DB, broker, portfolio URL, security, and actuator placeholders. |
| `resources/db/migration/V1__orders.sql` | Orders, idempotency, and relevant indexes. |
| `src/test/...` | State transitions, duplicate events, ownership, reservation failures, and broker integration. |
| `Dockerfile` | Non-root runtime image. |

An order accepted over REST returns `202` and a stable order ID. Matching is asynchronous; event handling must be idempotent.

See also: [order API](../../api/rest-api.md), [messaging](../../architecture/messaging.md), [order sequence](../../architecture/uml-diagrams.md).
