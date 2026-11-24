# Testing Strategy

- Unit: role policies, issue status transitions, submission triage, work-log validation, report formatting.
- Service/API: JWT validation, project membership checks in work-management-service, pagination, ownership, and ProblemDetail responses.
- Integration: service-owned PostgreSQL repositories, RabbitMQ report commands, and Kafka idempotent consumers.
- End-to-end: stakeholder submission → PM triage/issue assignment → developer update/time log → live notification/report.

Add tests with implemented behavior and verify role boundaries across projects.
