# Backend Guide

The backend skeleton is a Gradle multi-project build using Java 21, Spring Boot 4, and Spring Cloud 2025.1. Each runnable module has an entry point and exposes `/actuator/health`. Each stateful microservice owns its persistence and publishes versioned contracts; services do not share database tables.

## Modules

- `common`: versioned event envelope and shared ProblemDetail helper; no service-owned DTOs or business logic.
- `api-gateway`: Spring Cloud Gateway public REST edge with starter routes to the user, work-management, and report services.
- `user-service`: authentication, accounts, and global roles.
- `work-management-service`: projects, memberships, submissions, issues, comments, and work logs.
- `notification-service`: Kafka consumers and authenticated STOMP delivery.
- `report-service`: report requests and RabbitMQ coordination.
- `report-function`: stateless Spring Cloud Function deployed through OpenFaaS.

Use package layers `{api, application, domain, infrastructure}` under `edu.portikmark.teamflow.<module-package>`. Existing roots are `gateway`, `users`, `workmanagement`, `notifications`, `reports`, `reportfunction`, and `common`. Put validation at the API boundary and business rules in domain/application code. Keep internal APIs off the public gateway. See the [file layout guide](file-layout-guide.md) for the planned structure of each service and frontend module. Use [service catalog](../architecture/services.md), [REST reference](../api/rest-api.md), and [messaging contracts](../architecture/messaging.md) as source of truth.

Build all backend modules from `backend/` with `./gradlew build`. Run a service with `./gradlew :<module>:bootRun`; the starter health URLs and ports are listed in [Getting Started](../development/getting-started.md). Gateway target URLs can be overridden with `USER_SERVICE_URL`, `WORK_MANAGEMENT_SERVICE_URL`, and `REPORT_SERVICE_URL`.
