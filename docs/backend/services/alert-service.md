# Alert Service Guide

## Purpose
Owns a user's price-alert rules. It evaluates normalized Kafka price ticks, transitions a matching alert once, and asks notification-service to deliver the message through RabbitMQ.

## Planned module files

| Path (under `backend/alert-service/`) | Responsibility |
|---|---|
| `build.gradle.kts` | MVC, security, JPA/Flyway, Kafka consumer, RabbitMQ publisher, tests. |
| `AlertServiceApplication.java` | Spring Boot entry point. |
| `api/AlertController.java` | Create, list, and delete only the authenticated user's alerts. |
| `api/dto/` | Validated symbol, above/below direction, threshold, and alert response DTOs. |
| `application/AlertCommandService.java` | Creates/deletes rules after ownership and symbol validation. |
| `application/PriceTickHandler.java` | Finds relevant active alerts, evaluates threshold crossing, and triggers each at most once. |
| `domain/PriceAlert.java` | Alert rule, owner, threshold, status, and trigger transition. |
| `infrastructure/persistence/` | Alert repository indexed by symbol/status and Flyway migration. |
| `infrastructure/messaging/PriceTickListener.java` | Consumes `market.ticks` with alert-service's own group. |
| `infrastructure/messaging/NotificationCommandPublisher.java` | Publishes a user-targeted notification command. |
| `config/SecurityConfig.java` | Enforces USER role; use case/repository scopes every operation by owner. |
| `resources/application.yml` | DB, Kafka group, RabbitMQ, and actuator settings. |
| `resources/db/migration/V1__alerts.sql` | Alert table and indexes. |
| `src/test/...` | Threshold direction/boundary tests, duplicate tick handling, owner checks, broker tests. |
| `Dockerfile` | Non-root runtime image. |

See [messaging](../../architecture/messaging.md) for the shared tick and notification command shapes.
