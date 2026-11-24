# User Service Guide

## Purpose
Owns user accounts, credentials, roles, login/refresh sessions, and signing keys. It issues JWTs; other services validate them as resource servers. It owns global accounts, credentials, and system roles; project membership is owned by project-service.

## Planned module files

| Path (under `backend/user-service/`) | Responsibility |
|---|---|
| `build.gradle.kts` | MVC, security, JPA, PostgreSQL, Flyway, validation, JWT signing, Actuator, and tests. |
| `UserServiceApplication.java` | Spring Boot entry point. |
| `api/AuthController.java` | Register, login, refresh, and logout endpoints. |
| `api/UserController.java` | Current-user profile and admin user listing endpoints. |
| `api/dto/` | Validated auth/profile request and response DTOs; never expose entities. |
| `application/RegistrationService.java` | Creates a user, hashes the password, and assigns the default role. |
| `application/AuthenticationService.java` | Checks credentials and creates/rotates/revokes refresh sessions. |
| `application/TokenService.java` | Creates short-lived access JWTs and exposes the public-key set for JWKS. |
| `domain/User.java` | User identity, email, password hash, role, and account status. |
| `domain/RefreshSession.java` | Hashed refresh-token state, expiry, rotation, and revocation rules. |
| `infrastructure/persistence/` | User and refresh-session JPA entities/repositories and domain mappings. |
| `infrastructure/security/PasswordEncoderConfig.java` | BCrypt password encoder configuration. |
| `infrastructure/security/SigningKeyProvider.java` | Loads private/public signing keys from mounted secret/configuration. |
| `config/SecurityConfig.java` | Public auth/JWKS access and role protection for user endpoints. |
| `resources/application.yml` | Database, token lifetimes, key path, issuer, and actuator placeholders. |
| `resources/db/migration/V1__users_and_refresh_sessions.sql` | Creates user and refresh-session tables and constraints. |
| `Dockerfile` | Non-root runtime image. |
| `src/test/...` | Registration, token rotation, password handling, authorization, and repository tests. |

## Public contract
`POST /api/auth/register`, `/login`, `/refresh`, `/logout`; `GET /api/users/me`; `GET /.well-known/jwks.json`. The gateway routes the JWKS endpoint without authentication. Passwords and refresh tokens are never stored or logged in plaintext.

See also: [security](../../architecture/security.md), [API reference](../../api/rest-api.md).
