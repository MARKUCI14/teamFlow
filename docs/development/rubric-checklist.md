# Rubric Checklist

| # | Assessment item | Pts | TeamFlow implementation plan | Evidence | Done |
|---|---|---:|---|---|---|
| 1 | Secured REST API; >2 microservices | 1 | JWT gateway; user, work-management, notification, and report services | OpenAPI, role tests, diagrams | [ ] |
| 2 | Load balancer or scalable notifications | 1 | nginx load-balances API-gateway replicas (this one approach satisfies the item) | Multi-instance gateway demo | [ ] |
| 3 | Message broker | 1 | RabbitMQ report-generation command | RabbitMQ UI, retry/DLQ | [ ] |
| 4 | Event streaming | 1 | Kafka workflow and report lifecycle events | Kafka UI, consumer groups | [ ] |
| 5 | FaaS | 1 | OpenFaaS generates project PDF report | Invoke function and download PDF | [ ] |
| 6 | Web app + server notifications | 1 | React app consumes REST and receives STOMP updates | Live workflow demo | [ ] |
| 7 | Microfrontend architecture | 1 | React shell + stakeholder and team remotes | Runtime composition | [ ] |
| 8 | Containers | 1 | Docker Compose; Kubernetes manifests | Clean local startup | [ ] |
| 9 | UML and C4 documentation | 1 | Context/container and sequence/class/state diagrams | Rendered diagrams | [ ] |

The assessment text lists exactly nine system points (one point each in this checklist). The tutorial is assessed separately as grade P; it is not an additional system point. It should be about three pages on a topic implemented here, with a reproducible working example in a public Git repository.

## Demo flow

1. Start the containerized stack and log in as stakeholder.
2. Submit a bug or suggestion as a project stakeholder; show the secured REST request, persisted submission, and PM WebSocket notification.
3. Log in as a project manager, triage the submission, create and assign a linked issue, and show the assigned developer notification.
4. Log in as the assigned project developer, update status and log work time.
5. Show live notifications arriving over the authenticated WebSocket connection.
6. Request and download the FaaS project report; show RabbitMQ and Kafka activity.
7. Show stakeholder/team microfrontend boundaries, deployment, and C4/UML documentation.
