# C4 Model

## System context

```mermaid
flowchart LR
  A[Admin] --> T[TeamFlow]
  PM[Project Manager] --> T
  S[Stakeholder] --> T
  D[Developer] --> T
  T --> PG[(PostgreSQL)]
  T --> MQ[RabbitMQ]
  T --> K[Kafka]
  T --> R[Redis]
  T --> F[Report FaaS]
```

## Containers

```mermaid
flowchart TB
  U[Admin / PM / Stakeholder / Developer] --> N[nginx load balancer]
  N --> W[React shell + role-based microfrontends]
  N --> G[API gateway replicas]
  N --> WS[Notification service replicas / WebSocket]
  G --> US[User service]
  G --> PS[Project service]
  G --> SS[Submission service]
  G --> IS[Issue service]
  G --> LS[Worklog service]
  G --> RS[Report service]
  SS --> K[(Kafka)]
  IS --> K
  LS --> K
  K --> WS
  RS --> MQ[(RabbitMQ)]
  MQ --> F[Report function / OpenFaaS]
  WS <--> R[(Redis pub/sub)]
  G <--> R
  PS --> DB[(Service-owned PostgreSQL databases)]
  SS --> DB
  IS --> DB
  LS --> DB
```

## Issue lifecycle

```mermaid
stateDiagram-v2
  [*] --> OPEN: manager creates issue
  OPEN --> IN_PROGRESS: developer starts work
  IN_PROGRESS --> BLOCKED: dependency or clarification needed
  BLOCKED --> IN_PROGRESS: unblocked
  IN_PROGRESS --> IN_REVIEW: work submitted
  IN_REVIEW --> DONE: manager accepts
  IN_REVIEW --> IN_PROGRESS: changes requested
  OPEN --> CANCELLED: manager cancels
```

Detailed service boundaries and sequence diagrams: [services](services.md), [UML](uml-diagrams.md).
