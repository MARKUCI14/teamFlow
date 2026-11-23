# Roadmap and Backlog

Keep the stack runnable at each milestone. The core demo is stakeholder submission → manager triage and issue creation → developer status update and time log → live notification. Build only what supports that journey or a rubric item.

| Milestone | Goal | Rubric evidence |
|---|---|---|
| M0 Setup | Repo, docs, CI, ADRs | Documentation |
| M1 Foundation | Gradle, shared contracts, user-service, JWT, gateway, Compose infrastructure | Secured REST, containers |
| M2 Projects | project-service, roles, memberships, project screens | >2 microservices foundation |
| M3 Submissions | stakeholder submission API/UI, Kafka events | Event streaming |
| M4 Issues | manager triage, issue lifecycle and assignment, RabbitMQ command example | Message broker, core workflow |
| M5 Work tracking | worklog-service, Kafka events, project time summaries | Microservices, event streaming |
| M6 Realtime | notification-service, STOMP, Redis fan-out, multiple instances | Web app notifications, scalable WebSockets |
| M7 FaaS | report-service and OpenFaaS PDF report | FaaS |
| M8 Frontend | shell + project, stakeholder, developer, admin microfrontends | Web app, microfrontend |
| M9 Scale/deploy | nginx LB, replica demo, Compose and Kubernetes documentation | Load balancing, containers |
| M10 Finish | C4/UML, tutorial repo and text, demo rehearsal | Documentation, P grade |

## Scope controls

Start with title, description, status, priority, assignee, and project membership. Defer file upload, chat, external integrations, complex workflows, and billing unless core rubric work is complete. A work log records minutes, date, and a short description; do not build timers or payroll features.
