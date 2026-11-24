# ADR 0002: RabbitMQ for Work Commands, Kafka for Domain Events

- Status: Accepted
- Date: 2026-11-23

## Context

TeamFlow must demonstrate both a message broker and event streaming. Report generation is queued, while project activity must be independently consumed by notifications and reporting.

## Decision

Use RabbitMQ for report-generation commands. Use Kafka for committed submission, issue, work-log, and report lifecycle facts, keyed by projectId. REST remains the direct path for user-issued create/update operations to the owning service. The work-management-service publishes workflow facts; report-service publishes report lifecycle facts. notification-service consumes these Kafka facts and sends WebSocket updates; it does not need a separate RabbitMQ notification command.

## Consequences

Commands have one logical handler, retries, and dead-lettering. Events can be replayed and independently consumed. Producers must handle the database/broker dual-write problem; consumers must be idempotent.
