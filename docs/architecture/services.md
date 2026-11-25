# Service Catalog

TeamFlow has four internal microservices. The API gateway is the public edge; the report function is a FaaS component and is not counted as a microservice.

| Component | Port | Owns | Main API / messages |
|---|---:|---|---|
| nginx | 80/443 | routing | web app, `/api`, `/ws`; load-balances gateway replicas |
| api-gateway | 8080 | no business data | secured public REST routing and JWKS validation |
| user-service | 8081 | accounts, credentials, global roles, signing keys | `/api/auth/**`, `/api/users/**`, JWKS |
| work-management-service | 8082 | projects, memberships, submissions, issues, comments, work logs | `/api/projects/**`, `/api/submissions/**`, `/api/issues/**`, `/api/worklogs/**`; publishes workflow events |
| notification-service | 8083 | live WebSocket sessions | `/ws`; consumes Kafka events and sends STOMP updates |
| report-service | 8084 | report requests, job state, result metadata | `/api/reports/**`; sends RabbitMQ `reports.generate` commands |
| report-function | 8085 | none | stateless Spring Cloud Function that returns a project report PDF |

Each stateful microservice owns its own database or schema and accesses it only through its own code. The work-management-service groups related project workflow data in one service boundary. The report function receives an authorized snapshot from report-service and stores no user or job data.

Kafka carries committed submission, issue, work-log, and report lifecycle facts. On `SubmissionCreated`, notification-service sends a private notification to the project manager; after the PM creates and assigns a linked issue, `IssueCreated` triggers an assignment notification to the developer. RabbitMQ carries report-generation commands. For reports, report-service asks work-management-service to authorize the request and prepare the project-scoped snapshot. nginx load-balances stateless API-gateway replicas; notification-service starts as a single instance.
