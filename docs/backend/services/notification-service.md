# Notification Service Guide

## Purpose
Owns live connections and translates server-side events/commands into STOMP messages. Redis pub/sub lets any service event reach a browser connected to any notification-service replica.

## Planned module files

| Path (under `backend/notification-service/`) | Responsibility |
|---|---|
| `build.gradle.kts` | WebSocket/STOMP, Spring Security, Redis, RabbitMQ, Kafka, JSON, Actuator, tests. |
| `NotificationServiceApplication.java` | Spring Boot entry point. |
| `config/WebSocketConfig.java` | Registers `/ws`, STOMP application/broker prefixes, and allowed origins. |
| `config/WebSocketSecurityConfig.java` | Authorizes only intended inbound destinations; prevents clients impersonating users. |
| `security/StompJwtChannelInterceptor.java` | Validates JWT from STOMP CONNECT and binds principal to user ID. |
| `application/NotificationDispatcher.java` | Accepts normalized user notifications/prices and publishes to Redis channels. |
| `application/RedisFanoutListener.java` | Receives Redis messages on every replica and delivers only to local sessions/user destinations. |
| `infrastructure/messaging/NotificationCommandListener.java` | Consumes RabbitMQ `notifications.send`. |
| `infrastructure/messaging/TradeEventListener.java` | Converts order/trade/rejection Kafka events into user notifications. |
| `infrastructure/messaging/PriceTickListener.java` | Converts market ticks into price broadcasts. |
| `infrastructure/redis/RedisPubSubConfig.java` | Declares channel listeners, serializers, and Redis connection. |
| `resources/application.yml` | JWT/JWKS, Redis, broker, Kafka groups, WebSocket origin, and actuator settings. |
| `src/test/...` | CONNECT authentication, destination authorization, payload conversion, and Redis fan-out tests. |
| `Dockerfile` | Non-root runtime image. |

Clients authenticate in the STOMP CONNECT frame and subscribe to `/user/queue/notifications`; price updates use `/topic/prices`. Never trust a client-supplied user ID for a private destination.
