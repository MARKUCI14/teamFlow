# API Gateway

The public REST entry point behind nginx. It routes documented API paths to user-service, work-management-service, and report-service, validates RS256 bearer tokens, and applies CORS. The gateway exposes its own `/actuator/health`; it does not route internal actuator or service-management paths.

Registration, login, JWKS, CORS preflight requests, and the gateway health check are public. All other requests require a valid token issued by user-service. JWT validation uses `JWT_ISSUER` and the user-service JWKS at `${USER_SERVICE_URL}/.well-known/jwks.json`. Keep `JWT_ISSUER` equal to the `iss` claim user-service puts in access tokens.

Set `GATEWAY_CORS_ALLOWED_ORIGINS` to a comma-separated list of allowed browser origins. The local default is `http://localhost:5173`; production environments must set their own explicit origins. Only the documented REST methods and the `Authorization` and `Content-Type` headers are allowed. Cookies are not enabled for cross-origin requests.

Project and resource authorization remains in each owning service. The gateway authenticates the caller but does not decide project membership or permissions. The `/ws` WebSocket endpoint is outside the REST gateway and remains a separate notification proxy concern.
