# User Service Guide

## Purpose
Owns user accounts, credentials, global roles, and signing keys. It issues short-lived RS256 JWTs; other services validate them as resource servers. Project memberships and project-scoped roles are owned by work-management-service. Refresh sessions are outside the current API.

## Module files

| Path (under `backend/user-service/`) | Responsibility |
|---|---|
| `build.gradle` | MVC, security, JPA, PostgreSQL, Flyway, validation, JWT signing, Actuator, and tests. |
| `UserServiceApplication.java` | Spring Boot entry point. |
| `api/AuthController.java` | Registration and login endpoints. |
| `api/UserController.java` | Current-user profile endpoint. |
| `application/AuthService.java` | Registers accounts, checks credentials, and issues access JWTs. |
| `domain/User.java` | User identity, email, password hash, and optional global role. |
| `infrastructure/UserRepository.java` | User persistence. |
| `infrastructure/security/SigningKeys.java` | Loads RSA keys and configures JWT encoding/decoding. |
| `config/SecurityConfig.java` | Public auth/JWKS access and bearer-token protection for user endpoints. |
| `resources/application.yml` | Database, token lifetimes, key path, issuer, and actuator placeholders. |
| `resources/db/migration/V1__users.sql` | Creates the user-owned account table and constraints. |
| `Dockerfile` | Non-root runtime image. |
| `src/test/...` | Registration, password handling, token/JWKS validation, and authorization tests. |

## Public contract
`POST /api/auth/register`, `/login`; `GET /api/users/me`; `GET /.well-known/jwks.json`. Configure the database and RSA PEM paths with `USER_DB_*` and `JWT_*` environment variables. New accounts have no global role by default. Passwords are BCrypt-hashed and never returned or logged.

Register or log in with JSON containing `email` and `password`. The response contains `accessToken`, `tokenType`, and `expiresIn`; use the token as a bearer token for `/api/users/me`.

See also: [security](../../architecture/security.md), [API reference](../../api/rest-api.md).
