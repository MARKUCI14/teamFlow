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
- Use REST for user-issued create and update operations. Use RabbitMQ for report-generation commands and Kafka for committed domain facts. notification-service consumes Kafka events and sends authenticated STOMP updates: new submissions notify the project's PM, and newly assigned linked issues notify the developer.
- The React shell composes two remotes: stakeholder and team (PM, developer, and admin views).

## Core workflow and roles

1. A stakeholder with project membership submits a suggestion or bug report through the gateway to work-management-service.
2. work-management-service publishes `SubmissionCreated`; notification-service sends a private notification to the project's PM.
3. The PM triages the submission and creates/assigns linked issues within work-management-service. It publishes `IssueCreated`; notification-service sends an assignment notification to the developer.
4. The assigned developer updates issue status and records their own work time through work-management-service. Workflow facts are published to Kafka and relevant private WebSocket updates are delivered.
5. A project PM or admin requests a report through report-service. work-management-service checks membership and prepares the scoped snapshot; report-service queues generation through RabbitMQ and invokes the stateless report function.

Role boundaries:

- **Admin (`ADMIN`):** global role; manages users, global roles, projects, and project membership.
- **PM (`PROJECT_MANAGER`):** project-scoped membership role; manages that project's stakeholder access, triages submissions, creates and assigns issues, reviews work logs, and requests reports.
- **Stakeholder (`STAKEHOLDER`):** project-scoped membership role; views permitted project progress and creates or views their own submissions.
- **Developer (`DEVELOPER`):** project-scoped membership role; views assigned issues, updates them, comments, and creates their own work logs.

The user-service JWT carries the global `ADMIN` role when assigned; project roles are read from membership records. Project membership and resource visibility are checked by work-management-service. report-service asks it to authorize and prepare a project-scoped report snapshot. Frontend route visibility is not an authorization boundary.
