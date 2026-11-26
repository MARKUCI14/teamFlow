# Docker Compose

The root Compose file starts the local infrastructure, all current backend applications, and the API gateway. The repository does not yet contain frontend source or nginx configuration, so those are not part of this Compose stack.

## Start

Copy the example environment file, generate local JWT signing keys and Kafka TLS files, validate the Compose configuration, and start the stack:

```bash
cp .env.example .env
./scripts/generate-jwt-keys.sh
./scripts/generate-kafka-certs.sh
docker compose config
docker compose up -d
docker compose ps
```

Compose interpolates credentials from `.env`. Do not commit that file. The JWT script creates an RSA private/public key pair under ignored `.local/jwt-keys/`; Compose mounts the keys into user-service as read-only secrets. These keys are only for local development. Kafka uses SASL/PLAIN over TLS; its self-signed keystore, truststore, and password files are generated under ignored `.local/kafka-certs/`. The certificate covers the internal `kafka` hostname and host-side `localhost` address. Do not put real or production credentials in `.env`.

Compose waits for PostgreSQL's health check before starting either backend service. Flyway creates each service's schema on startup. Work-management-service uses the `work_management` schema and retrieves JWT signing keys from user-service over the Compose network.

## Test the public API through the gateway

The APIs are available directly at `http://localhost:8081` for user-service and `http://localhost:8082` for work-management-service. Check health and request the public signing keys:

## TODO: edit this!
```bash
curl -fsS http://localhost:8081/actuator/health
curl -fsS http://localhost:8081/.well-known/jwks.json
curl -fsS http://localhost:8082/actuator/health
The public API is available at `http://localhost:8080`; user-service is reachable only on the internal Compose network. Check gateway health and request the public signing keys through the gateway:

```bash
curl -fsS http://localhost:8080/actuator/health
curl -fsS http://localhost:8080/.well-known/jwks.json
```

Register and capture an access token, then request the current-user endpoint:

```bash
register_response=$(curl -fsS -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"student@example.com","password":"correct-horse-battery"}')
access_token=$(printf '%s' "$register_response" | jq -r '.accessToken')
curl -fsS http://localhost:8080/api/users/me -H "Authorization: Bearer $access_token"
```

An unauthenticated request to `/api/users/me` should return `401`. The work-management and report applications are currently starter services; their containers run, but business endpoints are not implemented yet.

Login uses the same JSON fields at `/api/auth/login`. Registration returns 201; using an already registered email returns 409. Invalid login credentials return 401. `jq` is used above only to extract the token from the JSON response.

Use the captured access token to call work-management-service directly. For example, list accessible projects with:

```bash
curl -fsS http://localhost:8082/api/projects \
  -H "Authorization: Bearer $access_token"
```

## Local endpoints

All published ports bind to localhost:

| Dependency | Address | Health check |
|---|---|---|
| PostgreSQL | `localhost:5432`, database `teamflow` | `pg_isready` |
| user-service | `http://localhost:8081` | `GET /actuator/health` |
| work-management-service | `http://localhost:8082` | `GET /actuator/health` |
| API gateway | `http://localhost:8080` | `GET /actuator/health` |
| RabbitMQ | `localhost:5672`; management UI `http://localhost:15672` | `rabbitmq-diagnostics ping` |
| Kafka | `localhost:9094` (SASL/PLAIN + TLS) | Authenticated Kafka broker API request |

Backend containers communicate by Compose service names. Containers on the Compose network use `postgres:5432`, `rabbitmq:5672`, and `kafka:9092`. For Kafka host clients, use `localhost:9094` with SASL/PLAIN and the generated truststore. Example credentials are in `.env.example` and should only be used locally.

This setup provides one PostgreSQL instance for the local stack. user-service owns its account table and applies its schema through Flyway. Other stateful services must keep their own schemas or databases as described in the [service catalog](../architecture/services.md).

## Stop

```bash
docker compose down
```

Remove `.local/jwt-keys/` and rerun `./scripts/generate-jwt-keys.sh` to regenerate the local signing keys. Remove `.local/kafka-certs/` and rerun the certificate script to regenerate Kafka TLS files. Avoid printing or sharing `docker compose config` output when `.env` contains non-example values because Compose renders interpolated environment values.
