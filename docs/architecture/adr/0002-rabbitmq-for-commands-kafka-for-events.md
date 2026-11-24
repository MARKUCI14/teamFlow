# ADR 0002: RabbitMQ for Work Commands, Kafka for Domain Events

- Status: Accepted
- Date: 2026-11-23

## Context

TeamFlow must demonstrate both a message broker and event streaming. Report generation is queued, while project activity must be independently consumed by notifications and reporting.

## Decision

Use RabbitMQ for report-generation and targeted notification commands. Use Kafka for committed submission, issue, work-log, and report lifecycle facts. Key events by projectId. REST remains the direct path for user-issued create/update operations to the owning service.

## Consequences

Commands have one logical handler, retries, and dead-lettering. Events can be replayed and independently consumed. Producers must handle the database/broker dual-write problem; consumers must be idempotent.
