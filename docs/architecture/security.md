# Security

## Authentication and authorization

user-service issues short-lived RS256 JWTs and publishes JWKS at `/.well-known/jwks.json`. Tokens identify the user and carry global roles when assigned. `PROJECT_MANAGER`, `STAKEHOLDER`, and `DEVELOPER` are project-scoped membership roles, not global JWT roles. user-service validates bearer tokens on its protected endpoints, and work-management-service validates bearer-token signatures against user-service JWKS before checking membership and resource access. Gateway-wide JWT enforcement is tracked separately in issue #47.

- Admin manages accounts, global roles, projects, and project membership.
- Project managers manage the projects where they hold that membership role: triage submissions, create/assign issues, review logs, and request reports.
- Stakeholders can read permitted project status and create/view their own submissions in projects where they hold that membership role.
- Developers can access assigned issues, update allowed fields, comment, and create their own work logs in projects where they hold that membership role.

Each owning service enforces role and resource scope. work-management-service derives the caller identity from the validated JWT subject and checks project membership for project data. A `userId` in a membership request identifies the account to add; it is not used as the caller identity. Never trust a user ID supplied in a request body as proof of identity.

## Layers

1. nginx is the planned TLS edge for the web app, public REST API, and WebSocket route.
2. api-gateway is the public REST edge; JWT enforcement and CORS are tracked in issue #47.
3. user-service and work-management-service validate JWTs directly. Other owning services must validate tokens when their protected APIs are implemented. notification-service authenticates STOMP CONNECT frames.
4. Internal service endpoints remain private and are not routed through the public API.

## WebSockets and secrets

Authenticate the STOMP CONNECT frame with the bearer token. Bind the authenticated principal and deliver only to `/user/queue/notifications`; never accept a client-supplied target user ID. Keep secrets in ignored local `.env` or Kubernetes Secrets. Configure `JWT_PRIVATE_KEY_PATH` and `JWT_PUBLIC_KEY_PATH` to mounted PEM key files (PKCS#8 private key, X.509 public key); share the same key pair across instances. Configure `JWT_ISSUER` and `JWT_ACCESS_TOKEN_TTL` (default `PT15M`). The user-service database uses `USER_DB_URL`, `USER_DB_USERNAME`, and `USER_DB_PASSWORD`; Flyway owns its schema and Hibernate validates it. Hash passwords with BCrypt, validate all DTOs, avoid logging credentials, and return structured errors without stack traces.
