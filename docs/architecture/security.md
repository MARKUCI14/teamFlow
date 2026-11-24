# Security

## Authentication and authorization

user-service issues short-lived RS256 JWTs and publishes JWKS. Tokens identify the user and carry global roles; work-management-service remains authoritative for project membership and resource access. Roles are `ADMIN`, `PROJECT_MANAGER`, `STAKEHOLDER`, and `DEVELOPER`.

- Admin manages accounts, roles, and membership.
- Project managers manage projects they belong to, triage submissions, create/assign issues, review logs, and request reports.
- Stakeholders can read permitted project status and create/view their own submissions.
- Developers can access assigned issues, update allowed fields, comment, and create their own work logs.

Each owning service enforces role and resource scope. work-management-service checks project membership for projects, submissions, issues, and work logs. Never trust a user ID supplied in a request body as proof of identity.

## Layers

1. nginx terminates TLS and exposes only web, `/api`, and `/ws` routes.
2. api-gateway validates JWTs, applies CORS, and strips spoofable identity headers.
3. user-service, work-management-service, and report-service validate JWTs too, so bypassing the gateway does not bypass authorization. notification-service authenticates STOMP CONNECT frames.
4. Internal endpoints are not routed publicly and require service authentication.

## WebSockets and secrets

Authenticate the STOMP CONNECT frame with the bearer token. Bind the authenticated principal and deliver only to `/user/queue/notifications`; never accept a client-supplied target user ID. Keep secrets in ignored local `.env` or Kubernetes Secrets. Hash passwords with BCrypt, validate all DTOs, avoid logging credentials, and return structured errors without stack traces.
