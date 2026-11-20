# Security

## Authentication
- **user-service** issues JWTs signed with **RS256**. Public keys are published at `/.well-known/jwks.json`.
- Access token: 15 minutes, claims `sub` (userId), `roles`, `iat`, `exp`. Refresh token: 7 days, stored hashed, rotated on use.
- Passwords hashed with BCrypt (cost >= 10). Login endpoint is rate limited.

## Authorization
- Roles: `USER`, `ADMIN`. Admin endpoints (`/api/admin/**`, simulation controls) require `ADMIN`.
- Users can only access their own resources: services compare the token `sub` with the resource owner (never trust a `userId` from the request body or path alone).

## Layers
1. **nginx**: TLS termination (self-signed in dev), request size limits, only `/api`, `/ws`, `/` exposed.
2. **api-gateway**: validates JWT, applies CORS, Redis-backed rate limiting, strips spoofable internal headers. Public routes: `/api/auth/**`, `/.well-known/jwks.json`, and `/actuator/health`.
3. **Each service** is also an OAuth2 resource server (validates the JWT again via JWKS), so a bypassed gateway is not a bypassed security model.
4. **Internal endpoints** (`/internal/**`, e.g. fund reservation) are not routed by the gateway and require a service token (shared secret from env/K8s Secret for this project; production alternative is mTLS or a service-account JWT). Documented simplification.

## WebSocket security
- Browser sends `Authorization: Bearer <jwt>` in the STOMP `CONNECT` frame; a channel interceptor validates it and binds the session principal.
- Users subscribe only to `/user/queue/**`, so each sees only their own messages; broadcast topics (`/topic/prices`) are read-only for clients.

## Secrets and config
- No secrets in git. Local: `.env` (ignored) with `.env.example` committed. Kubernetes: `Secret` objects.
- Broker and database credentials come from environment variables.

## Checklist (OWASP-minded)
- [ ] Input validation (`jakarta.validation`) on every DTO
- [ ] ProblemDetail errors without stack traces
- [ ] No sensitive data in logs
- [ ] Dependency scanning (Dependabot or Gradle dependency-check)
- [ ] CORS limited to known origins
- [ ] Security headers (via nginx)
