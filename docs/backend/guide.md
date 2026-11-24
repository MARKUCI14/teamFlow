# Backend Guide

The planned backend is a Gradle multi-project build using Java 21 and Spring Boot. Each stateful microservice owns its persistence and publishes versioned contracts; services do not share database tables.

## Modules

- `common`: event envelope and ProblemDetail conventions.
- `api-gateway`: public REST edge.
- `user-service`: authentication, accounts, and global roles.
- `work-management-service`: projects, memberships, submissions, issues, comments, and work logs.
- `notification-service`: Kafka consumers and authenticated STOMP delivery.
- `report-service`: report requests and RabbitMQ coordination.
- `report-function`: stateless Spring Cloud Function deployed through OpenFaaS.

Use package layers `{api, application, domain, infrastructure}` under `com.teamflow.<service>`. Put validation at the API boundary and business rules in domain/application code. Keep internal APIs off the public gateway. Use [service catalog](../architecture/services.md), [REST reference](../api/rest-api.md), and [messaging contracts](../architecture/messaging.md) as source of truth.
