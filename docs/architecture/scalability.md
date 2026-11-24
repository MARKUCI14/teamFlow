# Scalability

| Component | Approach |
|---|---|
| nginx + api-gateway | least-connections across stateless gateway replicas |
| project/submission/issue/worklog APIs | stateless replicas behind gateway; service-owned PostgreSQL |
| Kafka consumers | partition by projectId; same-service replicas share consumer groups |
| RabbitMQ workers | competing consumers on durable queues |
| notification-service | nginx WebSocket upstream plus Redis pub/sub fan-out |
| report-function | OpenFaaS scales workers according to queued report workload |

When a Kafka consumer handles a notification for a user connected to another WebSocket replica, it publishes `{userId, message}` to Redis. Every notification instance subscribes and delivers only to its local sessions. Clients reconnect with STOMP heartbeat and backoff. This is the system's scalable server-side notification example.

Demonstrate by running multiple gateway and notification replicas, sending project events while clients connected through different instances, and observing delivery. Kafka topic partition count bounds consumer parallelism; increase partitions only with a migration plan. Keep APIs stateless and use idempotency keys for retried commands.
