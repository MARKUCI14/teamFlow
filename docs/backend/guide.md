# Backend Guide

The planned backend is a Gradle multi-project build using Java 21 and Spring Boot. Each service owns its persistence and publishes versioned contracts; services do not share database tables.

## Modules

- `common`: event envelope, security utilities, ProblemDetail conventions.
- `api-gateway`, `user-service`, `project-service`, `submission-service`, `issue-service`, `worklog-service`, `notification-service`, `report-service`, `report-function`.

Use package layers `{api, application, domain, infrastructure}` under `com.teamflow.<service>`. Put validation at the API boundary and business rules in domain/application code. Keep internal APIs off the public gateway. Use [service catalog](../architecture/services.md), [REST reference](../api/rest-api.md), and [messaging contracts](../architecture/messaging.md) as source of truth.
