# C4 Model

Rendered by GitHub (Mermaid C4 support is experimental; if a diagram renders poorly, view it in the Mermaid Live Editor).

## Level 1: System Context

```mermaid
C4Context
  title System Context: StockSim
  Person(trader, "Trader", "Trades with virtual cash, follows portfolio, sets alerts")
  Person(admin, "Admin", "Manages symbols and simulation settings")
  System(stocksim, "StockSim", "Stock trading simulator with live prices, orders, portfolio, alerts and reports")
  Rel(trader, stocksim, "Uses", "HTTPS, WebSocket")
  Rel(admin, stocksim, "Administers", "HTTPS")
```

## Level 2: Containers

```mermaid
C4Container
  title Container diagram: StockSim
  Person(user, "Trader / Admin")

  Container(nginx, "nginx", "nginx", "Edge load balancer, TLS, static routing")

  Container_Boundary(web, "Web app (micro frontends)") {
    Container(shell, "Shell", "React, TypeScript, Module Federation", "Host: login, layout, routing")
    Container(mfemarket, "Market MFE", "React, TypeScript", "Live prices and charts")
    Container(mfetrading, "Trading MFE", "React, TypeScript", "Order form, portfolio, alerts")
    Container(mfeadmin, "Admin MFE", "React, TypeScript", "Symbols, simulation, users")
  }

  Container(gateway, "api-gateway", "Spring Cloud Gateway", "JWT validation, routing, rate limiting (x2)")
  Container(userSvc, "user-service", "Spring Boot", "Registration, login, JWT issuing")
  Container(marketSvc, "market-data-service", "Spring Boot", "Simulated price generator, symbols")
  Container(orderSvc, "order-service", "Spring Boot", "Order API and lifecycle (x3)")
  Container(matchSvc, "matching-service", "Spring Boot", "Order execution, limit order book")
  Container(portSvc, "portfolio-service", "Spring Boot", "Cash, holdings, P&L, reports")
  Container(alertSvc, "alert-service", "Spring Boot", "Price alert rules and evaluation")
  Container(notifSvc, "notification-service", "Spring Boot, STOMP", "WebSocket push (x2)")
  Container(fn, "report-function", "Spring Cloud Function, OpenFaaS", "Generates PDF report")

  ContainerQueue(rabbit, "RabbitMQ", "AMQP", "orders.place, orders.cancel, notifications.send")
  ContainerQueue(kafka, "Kafka", "Event stream", "market.ticks, trades.executed, orders.rejected")
  ContainerDb(redis, "Redis", "Redis", "Price cache, WebSocket pub/sub, rate limits")
  ContainerDb(pg, "PostgreSQL", "PostgreSQL", "One database per service")

  Rel(user, nginx, "HTTPS / WSS")
  Rel(nginx, shell, "Serves static assets")
  Rel(shell, mfemarket, "Loads remote")
  Rel(shell, mfetrading, "Loads remote")
  Rel(shell, mfeadmin, "Loads remote")
  Rel(nginx, gateway, "/api", "HTTP, round robin")
  Rel(nginx, notifSvc, "/ws", "WebSocket")

  Rel(gateway, userSvc, "REST")
  Rel(gateway, marketSvc, "REST")
  Rel(gateway, orderSvc, "REST")
  Rel(gateway, portSvc, "REST")
  Rel(gateway, alertSvc, "REST")

  Rel(orderSvc, portSvc, "Reserve funds", "REST /internal")
  Rel(orderSvc, rabbit, "Publishes orders.place / cancel")
  Rel(rabbit, matchSvc, "Delivers order commands")
  Rel(marketSvc, kafka, "Publishes market.ticks")
  Rel(matchSvc, kafka, "Publishes trades.executed, orders.rejected")
  Rel(kafka, matchSvc, "Consumes market.ticks")
  Rel(kafka, portSvc, "Consumes trades.executed")
  Rel(kafka, orderSvc, "Consumes trades, rejections")
  Rel(kafka, alertSvc, "Consumes market.ticks")
  Rel(kafka, notifSvc, "Consumes ticks, trades, rejections")
  Rel(alertSvc, rabbit, "Publishes notifications.send")
  Rel(portSvc, rabbit, "Publishes notifications.send")
  Rel(rabbit, notifSvc, "Delivers notifications.send")
  Rel(portSvc, fn, "Generate report", "HTTP")
  Rel(notifSvc, redis, "Pub/sub fan-out")
  Rel(gateway, redis, "Rate limit counters")
  Rel(matchSvc, redis, "Latest price cache")
  Rel(userSvc, pg, "JDBC")
  Rel(orderSvc, pg, "JDBC")
  Rel(portSvc, pg, "JDBC")
```

## Level 3: Components of matching-service

```mermaid
C4Component
  title Component diagram: matching-service
  Container_Boundary(m, "matching-service") {
    Component(listener, "OrderCommandListener", "Spring AMQP", "Consumes orders.place / orders.cancel")
    Component(engine, "MatchingEngine", "Domain service", "Executes market orders, evaluates limit orders")
    Component(book, "OpenOrderBook", "JPA repository", "Persisted open limit orders")
    Component(prices, "PriceCache", "Redis + Kafka consumer", "Latest price per symbol from market.ticks")
    Component(pub, "TradeEventPublisher", "Spring Kafka", "Publishes trades.executed, orders.rejected")
  }
  ContainerQueue(rabbit, "RabbitMQ")
  ContainerQueue(kafka, "Kafka")
  ContainerDb(pg, "matchingdb")
  Rel(rabbit, listener, "order commands")
  Rel(listener, engine, "executes")
  Rel(engine, book, "stores/loads limit orders")
  Rel(engine, prices, "reads price")
  Rel(kafka, prices, "market.ticks")
  Rel(prices, engine, "tick triggers limit evaluation")
  Rel(engine, pub, "emits fills/rejections")
  Rel(pub, kafka, "publishes")
  Rel(book, pg, "JDBC")
```

## Level 3: Components of order-service

```mermaid
C4Component
  title Component diagram: order-service
  Container_Boundary(o, "order-service") {
    Component(ctrl, "OrderController", "Spring MVC", "REST /api/orders")
    Component(app, "OrderApplicationService", "Spring service", "Validation, reservation, state transitions")
    Component(repo, "OrderRepository", "Spring Data JPA", "orders table")
    Component(client, "PortfolioClient", "REST client", "Reserve/release funds")
    Component(cmd, "OrderCommandPublisher", "Spring AMQP", "Publishes orders.place/cancel")
    Component(evt, "OrderEventConsumer", "Spring Kafka", "trades.executed, orders.rejected")
  }
  Rel(ctrl, app, "calls")
  Rel(app, repo, "persists")
  Rel(app, client, "reserves funds")
  Rel(app, cmd, "publishes command")
  Rel(evt, app, "applies status changes")
```
