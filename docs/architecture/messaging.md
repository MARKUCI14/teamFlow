# Messaging Design

RabbitMQ carries commands (“please perform this work”) to one handler. Kafka carries facts (“this happened”) to any number of independent consumers.

## RabbitMQ

| Routing key / queue | Command | Consumer |
|---|---|---|
| `reports.generate` | Generate a report for a scoped project snapshot | report-service / FaaS adapter |

Queues are durable, messages persistent, publishers use confirms, and bounded retries lead to a dead-letter queue. Issue creation and status changes are synchronous REST commands to the service that owns the issue; Kafka announces the committed result.

The notification-service is intended to consume Kafka domain events and send WebSocket updates. Targeted notification delivery does not use a separate RabbitMQ command.

## Kafka topics

work-management-service publishes `SubmissionCreated` to `submissions.events` after the submission database transaction commits. The key is the project ID. Its versioned payload contains the submission ID, author ID, title, type, and initial status.

| Topic | Key | Producer | Consumer groups |
|---|---|---|---|
| `submissions.events` | projectId | work-management-service | notifications (planned) |
| `issues.events` | projectId | work-management-service | notifications, reports |
| `worklogs.events` | projectId | work-management-service | notifications, reports |
| `reports.events` | projectId | report-service | notifications |

Independent services use separate groups; replicas of one service share a group and split partitions. Use project ID as key to retain per-project ordering.

## Envelope and example

```json
{"eventId":"uuid","type":"IssueStatusChanged","version":1,"occurredAt":"2026-11-23T10:00:00Z","payload":{"issueId":"uuid","projectId":"uuid","status":"IN_PROGRESS","changedBy":"uuid"}}
```

Events are versioned and consumers are idempotent on `eventId`. Use an outbox or documented publish-after-commit retry to address the database/broker dual-write problem. Assume at-least-once delivery.
