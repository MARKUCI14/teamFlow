# Security

## Authentication and authorization

user-service issues short-lived RS256 JWTs and publishes JWKS. Tokens identify the user and carry roles; project membership remains authoritative for access to project resources. Roles are `ADMIN`, `PROJECT_MANAGER`, `STAKEHOLDER`, and `DEVELOPER`.

- Admin manages accounts, roles, and membership.
- Project managers manage projects they belong to, triage submissions, create/assign issues, review logs, and request reports.
- Stakeholders can read permitted project status and create/view their own submissions.
- Developers can access assigned issues, update allowed fields, comment, and create their own work logs.

Services enforce both role and project/resource scope. Never trust a user ID supplied in a request body as proof of identity.

## Layers

1. nginx terminates TLS and exposes only web, `/api`, and `/ws` routes.
2. api-gateway validates JWTs, applies CORS and Redis-backed rate limits, and strips spoofable identity headers.
3. Each API service validates JWTs too, so bypassing the gateway does not bypass authorization.
4. Internal endpoints are not routed publicly and require service authentication.

## WebSockets and secrets

Authenticate the STOMP CONNECT frame with the bearer token. Bind the authenticated principal and deliver only to `/user/queue/notifications`; never accept a client-supplied target user ID. Keep secrets in ignored local `.env` or Kubernetes Secrets. Hash passwords with BCrypt, validate all DTOs, avoid logging credentials, and return structured errors without stack traces.
