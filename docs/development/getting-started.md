# Getting Started

The backend now has a runnable Gradle/Spring Boot skeleton. Frontend and deployment implementation is tracked in the [roadmap](../project/roadmap.md).

## Planned stack

Java 21 / Spring Boot 4 / Spring Cloud 2025.1 / Gradle, PostgreSQL, RabbitMQ, Kafka, React + TypeScript, Docker Compose, nginx, and OpenFaaS.

## Build order

Follow the roadmap: auth and gateway → combined work-management service → Kafka-backed notifications → report-service and OpenFaaS function → React shell and two remotes → nginx load-balancing demo, deployment, diagrams, and tutorial.

Keep the runnable demo path focused on stakeholder request through developer work tracking.

## Backend build and health checks

Use Java 21 and the committed Gradle wrapper:

```bash
cd backend
./gradlew build
```

Start each runnable module in a separate terminal from `backend/`:

| Module | Command | Health endpoint |
|---|---|---|
| API gateway | `./gradlew :api-gateway:bootRun` | `http://localhost:8080/actuator/health` |
| User service | `./gradlew :user-service:bootRun` | `http://localhost:8081/actuator/health` |
| Work management service | `./gradlew :work-management-service:bootRun` | `http://localhost:8082/actuator/health` |
| Notification service | `./gradlew :notification-service:bootRun` | `http://localhost:8083/actuator/health` |
| Report service | `./gradlew :report-service:bootRun` | `http://localhost:8084/actuator/health` |
| Report function | `./gradlew :report-function:bootRun` | `http://localhost:8085/actuator/health` |

The `common` module is a Java library and does not run as a service. Gateway routes target the local service ports by default; set `USER_SERVICE_URL`, `WORK_MANAGEMENT_SERVICE_URL`, or `REPORT_SERVICE_URL` to override them.
