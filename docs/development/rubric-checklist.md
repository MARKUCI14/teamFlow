# Rubric Checklist

| # | Assessment item | Pts | TeamFlow implementation plan | Evidence | Done |
|---|---|---:|---|---|---|
| 1 | Secured REST API; >2 microservices | 1 | JWT gateway; user, project, submission, issue, worklog, notification, report services | OpenAPI, role tests, diagrams | [ ] |
| 2 | Load balancer or scalable notifications | 1 | nginx gateway LB and Redis WebSocket fan-out | Multi-instance notification demo | [ ] |
| 3 | Message broker | 1 | RabbitMQ queues report generation and notification commands | RabbitMQ UI, retry/DLQ | [ ] |
| 4 | Event streaming | 1 | Kafka submission, issue, worklog, report events | Kafka UI, consumer groups | [ ] |
| 5 | FaaS | 1 | OpenFaaS generates project PDF report | Invoke function and download PDF | [ ] |
| 6 | Web app + server notifications | 1 | React app consumes REST and receives STOMP updates | Live workflow demo | [ ] |
| 7 | Microfrontend architecture | 1 | Shell + project, stakeholder, developer, admin remotes | Runtime composition | [ ] |
| 8 | Containers | 1 | Docker Compose; Kubernetes manifests | Clean local startup | [ ] |
| 9 | UML and C4 documentation | 1 | Context/container and sequence/class/state diagrams | Rendered diagrams | [ ] |

The listed system items total 9 points; confirm with the professor how the system grade is totaled. Tutorial (20%): about three pages on a topic implemented here, with a reproducible working example in a public Git repository.

## Demo flow

1. Start the containerized stack and log in as stakeholder.
2. Submit a bug or suggestion; show the secured REST request and persisted submission.
3. Log in as project manager, triage it, create and assign an issue.
4. Log in as developer, update status and log work time.
5. Show live notifications arriving across notification-service replicas.
6. Request and download the FaaS project report; show RabbitMQ and Kafka activity.
7. Show microfrontend boundaries, deployment, and C4/UML documentation.
