# API Gateway Guide

## Purpose
The gateway is the public REST entry point behind nginx. It validates access tokens and routes requests to the service that owns each API. It must not implement trading or user domain rules. The JWKS URL is a public pass-through to user-service.

## Planned module files

| Path (under `backend/api-gateway/`) | Responsibility |
|---|---|
| `build.gradle.kts` | Spring Cloud Gateway, Spring Security resource-server, Redis rate limiter, Actuator, and tests. |
| `src/main/java/com/stocksim/gateway/ApiGatewayApplication.java` | Reactive Spring Boot entry point. |
| `config/GatewayRoutes.java` | Declares route predicates and target service URIs; route destinations come from configuration. |
| `config/SecurityConfig.java` | Allows auth, JWKS, and health routes; requires valid JWT for other public API paths. |
| `filter/RequestCorrelationFilter.java` | Adds or propagates a request ID and strips caller-supplied trusted internal identity headers. |
| `filter/ApiExceptionHandler.java` | Maps gateway/security failures to safe Problem Details responses. |
| `src/main/resources/application.yml` | Route URI placeholders, issuer/JWKS settings, CORS, rate limits, Redis, and actuator configuration. |
| `src/test/.../GatewayRoutesTest.java` | Verifies route mapping and public-route behavior without contacting real services. |
| `src/test/.../GatewaySecurityTest.java` | Verifies public paths, 401/403 behavior, and JWT claim handling. |
| `Dockerfile` | Builds and runs the gateway as a non-root image. |

## Request path
nginx sends `/api/**` to a gateway replica. The gateway authenticates the JWT and routes to user, market, order, portfolio, or alert service. It does not route `/internal/**`; internal service calls use their private service address and service authentication. `/.well-known/jwks.json` is unauthenticated and routed to user-service so resource servers can obtain signing keys.

See also: [security](../../architecture/security.md), [REST API](../../api/rest-api.md), [scalability](../../architecture/scalability.md).
