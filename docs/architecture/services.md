# Service Catalog

| Service | Port | Store | Exposes | Consumes | Produces | Replicas |
|---|---|---|---|---|---|---|
| api-gateway | 8080 | Redis (rate limit) | Public REST entry point | n/a | n/a | 2 |
| user-service | 8081 | `userdb` | `/api/auth/**`, `/api/users/**`, JWKS | n/a | n/a | 1 |
| market-data-service | 8082 | `marketdb` | `/api/market/**`, admin sim controls | n/a | Kafka `market.ticks` | 1 |
| order-service | 8083 | `orderdb` | `/api/orders/**` | Kafka `trades.executed`, `orders.rejected` | RabbitMQ `orders.place`, `orders.cancel` | 3 |
| matching-service | 8084 | `matchingdb`, Redis | internal only | RabbitMQ `orders.place`, `orders.cancel`; Kafka `market.ticks` | Kafka `trades.executed`, `orders.rejected` | 1 (see scalability) |
| portfolio-service | 8085 | `portfoliodb` | `/api/portfolio/**`, `/internal/reservations` | Kafka `trades.executed` | RabbitMQ `notifications.send`; calls report-function | 1-2 |
| alert-service | 8086 | `alertdb` | `/api/alerts/**` | Kafka `market.ticks` | RabbitMQ `notifications.send` | 1 |
| notification-service | 8087 | Redis | WebSocket `/ws` (STOMP) | RabbitMQ `notifications.send`; Kafka `trades.executed`, `orders.rejected`, `market.ticks` | Redis pub/sub `notifications`, `prices`; STOMP `/topic/prices` | 2 |
| report-function | 8088 | none | `POST /function/report` (OpenFaaS) or `/report` (Compose) | HTTP | PDF | 0..n (scale to zero on OpenFaaS) |

## Infrastructure

| Component | Port | Notes |
|---|---|---|
| nginx | 80 / 443 | Edge LB; `/api` → gateways, `/ws` → notification-service, `/` → shell |
| PostgreSQL | 5432 | Databases: `userdb`, `marketdb`, `orderdb`, `matchingdb`, `portfoliodb`, `alertdb` |
| Redis | 6379 | Price cache, rate limiting, WebSocket pub/sub |
| RabbitMQ | 5672 / 15672 | AMQP + management UI |
| Kafka (KRaft) | 9092 | Single broker locally; 3 partitions per topic |
| Kafka UI (optional) | 8090 | Debugging |
| Frontend dev servers | 5173-5176 | shell, market, trading, admin |

## Responsibilities in one line
- **user-service**: who you are. **market-data-service**: what things cost. **order-service**: what you asked for. **matching-service**: what actually happened. **portfolio-service**: what you own. **alert-service**: what you want to be told. **notification-service**: telling you, live. **report-function**: a PDF of what you own.
