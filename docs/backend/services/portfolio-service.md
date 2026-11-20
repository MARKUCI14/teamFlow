# Portfolio Service Guide

## Purpose
Owns virtual cash, reservations, holdings, trades, portfolio valuation, and generated report records. It consumes execution events and provides a private reservation API for order-service.

## Planned module files

| Path (under `backend/portfolio-service/`) | Responsibility |
|---|---|
| `build.gradle.kts` | MVC, security, JPA/Flyway, Kafka, RabbitMQ, HTTP FaaS client, tests. |
| `PortfolioServiceApplication.java` | Spring Boot entry point. |
| `api/PortfolioController.java` | Current portfolio, trade history, and report endpoints. |
| `api/InternalReservationController.java` | Private reserve/release endpoints for trusted service calls. |
| `api/dto/` | Portfolio, holding, reservation, report status, and download DTOs. |
| `application/PortfolioQueryService.java` | Builds owner-scoped cash/holdings/P&L response using current market values. |
| `application/ReservationService.java` | Atomically reserves/releases cash or shares and rejects insufficient assets. |
| `application/TradeAppliedHandler.java` | Applies each `TradeExecuted` once, updates account/holdings, and records processed event ID. |
| `application/ReportRequestService.java` | Creates PENDING report record, invokes report function asynchronously, stores PDF, marks READY/FAILED, emits ready notification. |
| `domain/Account.java`, `Holding.java`, `Reservation.java`, `PortfolioReport.java` | Portfolio-owned state and invariants. |
| `infrastructure/persistence/` | JPA repositories, processed-trade dedupe, PDF metadata/blob storage decision, Flyway migrations. |
| `infrastructure/messaging/TradeEventListener.java` | Consumes `trades.executed` in portfolio's own consumer group. |
| `infrastructure/messaging/NotificationCommandPublisher.java` | Publishes `notifications.send` after report completion when configured. |
| `infrastructure/report/ReportFunctionClient.java` | Calls the configured FaaS endpoint and validates content type/response. |
| `config/SecurityConfig.java` | Owner-scoped public routes and internal-token-only reservation routes. |
| `resources/application.yml` | DB, Kafka, RabbitMQ, function URL, internal-token placeholder, and actuator. |
| `resources/db/migration/V1__portfolio.sql` | Accounts, holdings, reservations, processed events, and report metadata. |
| `src/test/...` | Reservation atomicity, trade idempotency, valuation, report lifecycle, access control. |
| `Dockerfile` | Non-root runtime image. |

Report API returns `202` with report ID. The PDF generation work happens after acceptance; clients poll report status or receive a WebSocket notification when ready.
