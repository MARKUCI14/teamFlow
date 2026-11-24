# Tutorial Plan (Grade P)

## Proposed title

**Scaling project notifications across Spring Boot microservices with Kafka, Redis, and WebSockets**

## Working example

A small public repository with two notification-service instances, Kafka events for issue status changes, Redis pub/sub fan-out, and authenticated STOMP clients. A developer updates an issue; a project manager connected to another instance receives the update. Include Docker Compose, sample users, curl commands, and a short troubleshooting section.

## Three-page outline

1. **Problem and architecture:** why service events and WebSocket connections can land on different instances; diagram Kafka → Redis → connected sessions.
2. **Implementation:** event envelope, consumer group, Redis channel, session delivery, JWT/STOMP authorization, reconnect behavior.
3. **Run and verify:** clone and start, log in as two roles, change issue status, scale notification service, observe delivery; discuss idempotency and failure recovery.

The public example must work from a clean clone and correspond to implemented system behavior. Do not present planned behavior as completed evidence.
