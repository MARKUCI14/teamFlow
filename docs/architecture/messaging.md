# Messaging Design

## Why both RabbitMQ and Kafka

| | RabbitMQ (commands) | Kafka (events) |
|---|---|---|
| Intent | "Please do X" (one handler) | "X happened" (any number of readers) |
| Model | Queue, competing consumers, ack/nack | Partitioned log, consumer groups, replay |
| Used for | place/cancel order, send notification | price ticks, executed trades, rejections |
| Failure handling | retry, dead-letter queue | offset commit, retry topic / DLT |
| Ordering | per queue | per partition (key) |

## RabbitMQ topology

| Exchange | Type | Routing key | Queue | Consumer |
|---|---|---|---|---|
| `orders.exchange` | direct | `order.place` | `orders.place` | matching-service |
| `orders.exchange` | direct | `order.cancel` | `orders.cancel` | matching-service |
| `notifications.exchange` | direct | `notification.send` | `notifications.send` | notification-service |
| `dlx.exchange` | fanout | n/a | `dead.letter` | manual inspection |

All queues are durable, messages persistent, publisher confirms on, consumers use bounded retry (3 attempts, exponential backoff) then dead-letter.

## Kafka topics

| Topic | Key | Partitions | Producer | Consumer groups |
|---|---|---|---|---|
| `market.ticks` | symbol | 3 | market-data-service | `matching`, `alerts`, `ws-prices` |
| `trades.executed` | userId | 3 | matching-service | `portfolio`, `orders`, `notifications` |
| `orders.rejected` | userId | 3 | matching-service | `orders`, `notifications` |

Each service uses its own consumer group so every service sees every event. Instances of the same service share a group and split partitions.

## Message envelope

All events and commands share a header (defined in `backend/common`):

```json
{
  "eventId": "7b0f0a62-1c54-4a8f-9b53-0fd1c2e0a6b1",
  "type": "TradeExecuted",
  "version": 1,
  "occurredAt": "2026-01-15T10:15:30.123Z",
  "payload": { }
}
```

## Schemas (v1)

**PlaceOrderCommand** (RabbitMQ `orders.place`)
```json
{ "orderId": "uuid", "userId": "uuid", "symbol": "AAPL", "side": "BUY",
  "type": "LIMIT", "quantity": 10, "limitPrice": 195.50 }
```

**CancelOrderCommand** (RabbitMQ `orders.cancel`)
```json
{ "orderId": "uuid", "userId": "uuid" }
```

**SendNotificationCommand** (RabbitMQ `notifications.send`)
```json
{ "userId": "uuid", "category": "ALERT", "title": "AAPL above 200", "body": "AAPL is now 200.35", "data": {} }
```

**PriceTick** (Kafka `market.ticks`)
```json
{ "symbol": "AAPL", "price": 200.35, "volume": 1200, "timestamp": "2026-01-15T10:15:30Z" }
```

**TradeExecuted** (Kafka `trades.executed`)
```json
{ "tradeId": "uuid", "orderId": "uuid", "userId": "uuid", "symbol": "AAPL",
  "side": "BUY", "quantity": 10, "price": 200.35, "executedAt": "2026-01-15T10:15:31Z" }
```

**OrderRejected** (Kafka `orders.rejected`)
```json
{ "orderId": "uuid", "userId": "uuid", "reason": "UNKNOWN_SYMBOL" }
```

## Delivery guarantees and idempotency
- At-least-once everywhere. Consumers must tolerate duplicates:
  - order-service: only transitions state if the transition is valid (FILLED is terminal).
  - portfolio-service: stores processed `tradeId` in a table with a unique constraint.
  - matching-service: dedupes on `orderId`.
- Dual-write problem (DB save + publish): order-service uses publish-after-commit with retry, or a **transactional outbox** (stretch goal). Document the trade-off in the tutorial.

## Evolution rules
Add optional fields freely; never rename or remove fields in a published version; a breaking change means a new `version` and a migration period where both are handled.
