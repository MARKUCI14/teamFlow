# UML Diagrams

## Sequence: place a market order

```mermaid
sequenceDiagram
  actor T as Trader
  participant N as nginx
  participant G as api-gateway
  participant O as order-service
  participant P as portfolio-service
  participant R as RabbitMQ
  participant M as matching-service
  participant K as Kafka
  participant NS as notification-service
  participant W as Browser (WebSocket)

  T->>N: POST /api/orders (JWT)
  N->>G: forward
  G->>G: validate JWT, rate limit
  G->>O: POST /orders
  O->>P: POST /internal/reservations
  P-->>O: 201 reserved
  O->>O: save order PENDING
  O->>R: publish orders.place
  O-->>T: 202 Accepted {orderId}
  R->>M: deliver command
  M->>M: read latest price, execute
  M->>K: publish trades.executed
  par consumers
    K->>P: trades.executed (update cash, holdings)
  and
    K->>O: trades.executed (status FILLED)
  and
    K->>NS: trades.executed
    NS->>W: STOMP /user/queue/notifications
  end
```

## Sequence: price alert

```mermaid
sequenceDiagram
  participant MD as market-data-service
  participant K as Kafka
  participant A as alert-service
  participant R as RabbitMQ
  participant NS as notification-service
  participant Redis
  participant W as Browser

  loop every second per symbol
    MD->>K: market.ticks {symbol, price}
  end
  K->>A: tick
  A->>A: evaluate active alerts for symbol
  A->>A: mark alert TRIGGERED
  A->>R: notifications.send {userId, text}
  R->>NS: deliver
  NS->>Redis: PUBLISH notifications {userId, payload}
  Redis-->>NS: message to every instance
  NS->>W: push if user connected to this instance
```

## Sequence: generate report (FaaS)

```mermaid
sequenceDiagram
  actor T as Trader
  participant P as portfolio-service
  participant F as report-function
  participant R as RabbitMQ
  participant NS as notification-service

  T->>P: POST /api/portfolio/reports
  P-->>T: 202 {reportId, status: PENDING}
  P->>F: POST /function/report (portfolio snapshot JSON)
  F-->>P: 200 application/pdf
  P->>P: store report READY
  P->>R: notifications.send "report ready"
  R->>NS: deliver
  NS-->>T: WebSocket push
  T->>P: GET /api/portfolio/reports/{id}/download
```

## State: order lifecycle

```mermaid
stateDiagram-v2
  [*] --> PENDING: order accepted
  PENDING --> OPEN: limit order resting in book
  PENDING --> FILLED: market order executed
  OPEN --> FILLED: price crosses limit
  PENDING --> REJECTED: insufficient funds / unknown symbol
  OPEN --> CANCELLED: user cancels
  PENDING --> CANCELLED: user cancels
  FILLED --> [*]
  REJECTED --> [*]
  CANCELLED --> [*]
```

## Class diagram: core domain

```mermaid
classDiagram
  class User { UUID id; String email; String passwordHash; Role role }
  class Order { UUID id; UUID userId; String symbol; Side side; OrderType type; int quantity; BigDecimal limitPrice; OrderStatus status; Instant createdAt }
  class Trade { UUID id; UUID orderId; UUID userId; String symbol; Side side; int quantity; BigDecimal price; Instant executedAt }
  class Account { UUID userId; BigDecimal cash; BigDecimal reservedCash }
  class Holding { UUID userId; String symbol; int quantity; BigDecimal avgCost }
  class PriceAlert { UUID id; UUID userId; String symbol; Direction direction; BigDecimal threshold; AlertStatus status }
  class Symbol { String ticker; String name; BigDecimal basePrice; double volatility; boolean active }
  class Report { UUID id; UUID userId; ReportStatus status; Instant createdAt }
  User "1" --> "*" Order
  Order "1" --> "0..*" Trade
  User "1" --> "1" Account
  User "1" --> "*" Holding
  User "1" --> "*" PriceAlert
  User "1" --> "*" Report
  Order ..> Symbol
```

## Deployment overview

```mermaid
flowchart LR
  subgraph Edge
    nginx
  end
  subgraph Services
    gw1[api-gateway x2]
    ord[order-service x3]
    ns[notification-service x2]
    other[user, market-data, matching,<br/>portfolio, alert]
    fn[report-function]
  end
  subgraph Infra
    pg[(PostgreSQL)]
    redis[(Redis)]
    rmq{{RabbitMQ}}
    kfk{{Kafka}}
  end
  nginx --> gw1 --> ord
  nginx --> ns
  gw1 --> other
  other --> pg
  ord --> pg
  other --> rmq
  other --> kfk
  ns --> redis
  gw1 --> redis
  other --> fn
```
