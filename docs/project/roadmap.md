# Roadmap and Backlog

Keep the stack runnable at each milestone. The core demo is project member stakeholder submission → PM notification → PM triage and linked issue assignment → developer notification → developer status update and time log → live notification. `ADMIN` is global; PM, stakeholder, and developer roles are project-scoped. Build only what supports that journey or a rubric item.

| Milestone | Goal | Rubric evidence |
|---|---|---|
| M0 Setup | Repo, docs, CI, ADRs | Documentation |
| M1 Foundation | Gradle, shared contracts, user-service, gateway, and Compose infrastructure | Secured REST, containers |
| M2 Work management | one service for projects, memberships, submissions, issues, and work logs; enforce project roles | Core workflow and role-scoped access |
| M3 Events and notifications | Kafka workflow events and notification-service STOMP updates | More than two internal microservices, event streaming, server-side notifications |
| M4 Reports | report-service, work-management-service scoped snapshot authorization, RabbitMQ report command, and OpenFaaS PDF function | Message broker, FaaS |
| M5 React app | shell plus stakeholder and team microfrontend remotes | Web app, microfrontend |
| M6 Scale and deploy | nginx load-balances gateway replicas; Compose and Kubernetes docs | Load balancing, containers |
| M7 Documentation and tutorial | UML/C4 alignment, tutorial repo, and demo rehearsal | Documentation, grade P |

## Scope controls

Start with title, description, status, priority, assignee, and project membership. Defer file upload, chat, external integrations, complex workflows, and billing unless core rubric work is complete. A work log records minutes, date, and a short description; do not build timers or payroll features.
