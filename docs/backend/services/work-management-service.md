# Work Management Service

## Purpose

Owns the project workflow in one Spring Boot service: projects and memberships, stakeholder submissions and triage, developer issues and comments, and work logs. Keeping these related records together avoids extra service-to-service calls for the core demo.

## API and roles

The public routes are listed in the [REST API reference](../../api/rest-api.md). The service enforces JWT roles, project membership, resource visibility, and ownership for every request. PMs triage submissions and create/assign issues. Developers update assigned issues and create their own work logs. Stakeholders can see permitted project progress and manage their own submissions.

## Events

After committing workflow changes, publish versioned Kafka facts for submissions, issues, and work logs. The notification-service consumes those facts. Use an outbox or a documented publish-after-commit retry and make consumers idempotent.

## Data ownership

The service owns the project, membership, submission, issue, comment, and work-log tables. Other services use its APIs or events; they do not read its tables.
