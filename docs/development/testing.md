# Testing Strategy

- Unit: role policies, issue status transitions, submission triage, work-log validation, report formatting.
- Service/API: JWT validation, project membership checks, pagination, ownership, and ProblemDetail responses.
- Integration: PostgreSQL repositories, RabbitMQ retry/dead-letter behavior, Kafka idempotent consumers, Redis WebSocket fan-out.
- End-to-end: stakeholder submission → PM triage/issue assignment → developer update/time log → live notification/report.

Add tests with implemented behavior and verify role boundaries across projects.
