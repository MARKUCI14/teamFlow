# Backend Guide

## Structure
```
backend/
├── settings.gradle.kts           # includes all modules
├── build.gradle.kts              # shared config
├── gradle/libs.versions.toml     # version catalog (single source of versions)
├── buildSrc/ (or build-logic/)   # convention plugins: java, spring-service, docker
├── common/                       # events, commands, shared error + security config
└── <service>/
    ├── build.gradle.kts
    ├── Dockerfile
    └── src/{main,test}/...
```
Pin the Spring Boot and Spring Cloud versions in `libs.versions.toml` (use the latest stable pair compatible with each other; check the Spring Cloud release train compatibility table when upgrading).

## Creating a new service (checklist)
1. Add `include("<service>")` to `settings.gradle.kts`.
2. Apply the `spring-service` convention plugin; add only needed starters.
3. Package root `com.stocksim.<service>` with `api/application/domain/infrastructure`.
4. `application.yml` + profile files `application-local.yml`, `-docker.yml`, `-k8s.yml`; all hosts/secrets from env vars.
5. Flyway migration `V1__init.sql`, DB name `<service>db`.
6. Security: resource server config from `common` (JWKS URL via env).
7. Actuator health (`/actuator/health`), liveness/readiness probes, OpenAPI.
8. `Dockerfile` (multi-stage, JRE 21, non-root user) and compose/K8s entries.
9. Tests incl. Testcontainers; add to CI (automatic via `./gradlew build`).
10. Update `docs/architecture/services.md`, C4, and API docs.

## Key dependencies
| Need | Dependency |
|---|---|
| REST | `spring-boot-starter-web`, `-validation`, springdoc-openapi |
| Security | `spring-boot-starter-oauth2-resource-server`, `spring-boot-starter-security` |
| Gateway | `spring-cloud-starter-gateway` (reactive) |
| DB | `spring-boot-starter-data-jpa`, PostgreSQL driver, Flyway |
| RabbitMQ | `spring-boot-starter-amqp` |
| Kafka | `spring-kafka` |
| Redis | `spring-boot-starter-data-redis` |
| WebSocket | `spring-boot-starter-websocket` |
| FaaS | `spring-cloud-function-web` (+ OpenPDF or Apache PDFBox for PDFs) |
| Tests | `spring-boot-starter-test`, Testcontainers, Spring Security test |
| Ops | `spring-boot-starter-actuator`, `micrometer-registry-prometheus` |

## Messaging code patterns

**RabbitMQ publisher** (order-service)
```java
rabbitTemplate.convertAndSend("orders.exchange", "order.place", envelope(new PlaceOrderCommand(...)));
```
**RabbitMQ consumer** (matching-service)
```java
@RabbitListener(queues = "orders.place")
void onPlace(Envelope<PlaceOrderCommand> msg) { engine.handle(msg); }  // throw -> retry -> DLQ
```
**Kafka producer** (key = symbol / userId)
```java
kafkaTemplate.send("trades.executed", trade.userId().toString(), envelope(trade));
```
**Kafka consumer** (own group per service)
```java
@KafkaListener(topics = "trades.executed", groupId = "portfolio")
void onTrade(Envelope<TradeExecuted> e) { service.apply(e); } // idempotent
```
**Serialization**: JSON (Jackson) with type headers disabled; use explicit target types and the shared `Envelope<T>` from `common`.

## Notification-service (WebSocket + Redis) outline
- STOMP endpoint `/ws`, application prefix `/app`, user destination prefix `/user`.
- `ChannelInterceptor` validates the JWT on `CONNECT`.
- `NotificationDispatcher.publish(userId, payload)` → `redisTemplate.convertAndSend("notifications", json)`.
- `RedisMessageListener` on every instance → `simpMessagingTemplate.convertAndSendToUser(userId, "/queue/notifications", payload)`.
- Inputs: Kafka listeners + RabbitMQ listener both call `publish(...)`.

## Market data simulation
Geometric random walk per symbol: `p(t+1) = p(t) * exp((mu - sigma²/2)dt + sigma * sqrt(dt) * Z)`. Admin can change `sigma`, tick interval, and pause. Ticks are published to Kafka and the latest price is cached in Redis (`price:<symbol>`).

## report-function
Spring Cloud Function bean `report` (`Function<ReportRequest, byte[]>`). Request contains the portfolio snapshot (holdings, cash, P&L); output is the PDF. Packaged as a container; OpenFaaS template wraps it with `of-watchdog` (HTTP mode, port 8080).

## Configuration reference (env vars)
`DB_URL`, `DB_USER`, `DB_PASSWORD`, `REDIS_HOST`, `RABBIT_HOST`, `RABBIT_USER`, `RABBIT_PASSWORD`, `KAFKA_BOOTSTRAP`, `JWKS_URI`, `INTERNAL_TOKEN`, `REPORT_FUNCTION_URL`, `SERVER_PORT`.

## Observability
Actuator health/metrics; propagate `X-Request-Id`; log it in every line (MDC). Optional Prometheus + Grafana in Compose profile `observability`.
