# Matching Service Guide

## Purpose
Consumes order commands and applies market/limit matching rules. It is the authority for execution decisions and publishes immutable trade or rejection events. It does not expose public customer APIs.

## Planned module files

| Path (under `backend/matching-service/`) | Responsibility |
|---|---|
| `build.gradle.kts` | Spring AMQP, Kafka, JPA/Flyway, Redis client if used, Actuator, tests. |
| `MatchingServiceApplication.java` | Spring Boot entry point. |
| `application/MatchingEngine.java` | Applies order rules and returns fills, resting orders, or rejection reasons. |
| `application/PlaceOrderHandler.java` | Deduplicates and validates a place command before invoking the engine. |
| `application/CancelOrderHandler.java` | Cancels an eligible resting order and emits the required outcome. |
| `application/MarketTickHandler.java` | Re-evaluates resting limit orders when a normalized price tick arrives. |
| `domain/OrderBook.java` | In-memory/domain representation and deterministic price-time priority rules. |
| `domain/RestingOrder.java` | Persistable open order data needed to resume matching after restart. |
| `domain/Trade.java` | Immutable execution result with price, quantity, side, and timestamp. |
| `infrastructure/messaging/OrderCommandListener.java` | Consumes RabbitMQ place/cancel queues with retry and dead-letter behavior. |
| `infrastructure/messaging/MarketTickListener.java` | Consumes `market.ticks` using the matching consumer group. |
| `infrastructure/messaging/TradeEventPublisher.java` | Publishes `trades.executed` and `orders.rejected` to Kafka. |
| `infrastructure/persistence/` | Deduplication records and resting order persistence in `matchingdb`. |
| `infrastructure/price/LatestPriceReader.java` | Reads latest normalized price from Redis or the local Kafka-derived view; choose one mechanism and document it consistently. |
| `config/MessagingConfig.java` | Declares durable queues, retry/DLX policy, Kafka topics, and consumer settings. |
| `resources/application.yml` | Broker, database, Redis (if used), and actuator configuration. |
| `resources/db/migration/V1__matching_state.sql` | Creates open-order and processed-command tables. |
| `src/test/...` | Matching rules, price/time priority, duplicates, retries, and broker integration. |
| `Dockerfile` | Non-root runtime image. |

Run one active matcher in the first version to preserve a single order-book owner. Scale-out requires explicit symbol partitioning/ownership; see [scalability](../../architecture/scalability.md).
