# Implementation Baseline

This page records the decisions to use when implementation starts. The project remains in the planning phase; these are agreed design decisions, not claims that the software is implemented.

## Assessment mapping

The supplied assessment text lists nine system points. TeamFlow will cover all nine without treating any example project or extra component as a requirement:

1. Secured REST API, within a system using more than two microservices.
2. Scalability, satisfied by load balancing **or** scalable WebSockets. TeamFlow uses nginx load balancing for this one point.
3. Message broker: RabbitMQ.
4. Event streaming: Kafka.
5. FaaS.
6. Web app consuming REST and receiving server-side notifications.
7. Microfrontend architecture.
8. Containers.
9. UML and C4 documentation.

The tutorial is a separate grade P requirement: about three pages on an implemented topic, with a working example in a public Git repository. It is not an additional system point.

## Architecture decisions

- Keep one repository with four internal microservices: user-service, work-management-service, notification-service, and report-service. The API gateway is the public edge, and the report function is a separate FaaS component; neither is counted as an internal microservice.
- work-management-service owns projects, memberships, submissions, issues, comments, and work logs. This keeps the core workflow in one service boundary.
- Each stateful service owns its data. report-service owns report jobs and result metadata; the stateless report function receives an authorized project snapshot and persists nothing.
- Use nginx to load-balance stateless API-gateway replicas. Start notification-service as one instance; Redis is not included.
- Use REST for user-issued create and update operations. Use RabbitMQ for report-generation commands and Kafka for committed domain facts. notification-service consumes Kafka events and sends authenticated STOMP updates.
- The React shell composes two remotes: stakeholder and team (PM, developer, and admin views).

## Core workflow and roles

1. A stakeholder submits a suggestion or bug report through the gateway to work-management-service.
2. PM triages it and creates or links issues within work-management-service.
3. An assigned developer updates issue status and records their own work time through that service.
4. work-management-service publishes domain events to Kafka; notification-service delivers relevant updates over WebSockets.
5. A PM or admin requests a report through report-service, which queues generation through RabbitMQ and invokes the stateless report function.

Role boundaries:

- **Admin:** manages users, roles, and project membership across the system.
- **PM:** creates projects, manages stakeholder access, triages submissions, creates and assigns issues, reviews work logs, and requests reports.
- **Stakeholder:** views permitted project progress and creates or views their own submissions.
- **Developer:** views assigned issues, updates them, comments, and creates their own work logs.

Project membership and resource visibility are checked by work-management-service. report-service asks it to authorize and prepare a project-scoped report snapshot. Frontend route visibility is not an authorization boundary.
