# Work Management Service

## Purpose

Owns the project workflow in one Spring Boot service: projects and memberships, stakeholder submissions and triage, developer issues and comments, and work logs. Keeping these related records together avoids extra service-to-service calls for the core demo.

## API and roles

The public routes are listed in the [REST API reference](../../api/rest-api.md). This service validates access-token signatures using user-service JWKS and checks project membership for project reads. Only project members can list or read a project; global Admins must also be added to see project data. A global Admin or an existing project manager can create a project, and the creator receives a `PROJECT_MANAGER` membership. Project managers and global Admins can add members; a global Admin may add members without first joining the project.

Project APIs are `GET /api/projects`, `POST /api/projects`, `GET /api/projects/{id}`, `PATCH /api/projects/{id}`, and `POST /api/projects/{id}/members`. Project lists are paginated. Membership roles are `PROJECT_MANAGER`, `STAKEHOLDER`, and `DEVELOPER`. A membership stores the account UUID as an external reference; user-service remains the owner of account data.

The service uses its own `work_management` PostgreSQL schema. Flyway creates the schema and tables; Hibernate validates the migration. Configure `WORK_DB_URL`, `WORK_DB_USERNAME`, `WORK_DB_PASSWORD`, and `WORK_DB_SCHEMA`. JWT validation uses `JWT_ISSUER` and `USER_SERVICE_URL` to retrieve JWKS. Health is public at `/actuator/health`; other endpoints require a valid bearer token.

The project, membership, submission, issue, comment, and work-log API contracts are documented in the [REST API reference](../../api/rest-api.md). Stakeholders create and view their own submissions. Project PMs and project-member Admins can view all project submissions, triage them, and create linked issues. Issue assignees must be project developers.

## Events

After a submission transaction commits, work-management-service publishes the versioned `SubmissionCreated` fact to `submissions.events`, keyed by project ID. The producer uses the shared event envelope. Delivery failures are logged; an outbox/retry mechanism is a future reliability improvement. Issue and work-log event publishing are not part of this implementation.

## Data ownership

The service owns the project, membership, submission, issue, comment, and work-log tables. Other services use its APIs or events; they do not read its tables.
