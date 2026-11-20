# Docker Compose

`docker-compose.yml` at the repository root runs the whole system.

## Services
| Group | Services |
|---|---|
| Infra | `postgres`, `redis`, `rabbitmq`, `kafka` (KRaft), `kafka-ui` (profile `tools`) |
| Backend | `api-gateway` (x2), `user-service`, `market-data-service`, `order-service` (x3), `matching-service`, `portfolio-service`, `alert-service`, `notification-service` (x2), `report-function` |
| Frontend | `shell` (nginx serving static build + remotes) |
| Edge | `nginx` |

## Conventions
- One network `stocksim`; services reach each other by service name.
- Healthchecks on every container (`/actuator/health`, `pg_isready`, `rabbitmq-diagnostics ping`, etc.); `depends_on: condition: service_healthy`.
- Postgres init script `deploy/postgres/init-db.sql` creates one database and user per service.
- Config from `.env` (see `.env.example`). Never commit `.env`.
- Replicas: use `deploy.replicas` or `docker compose up --scale order-service=3`. For nginx upstreams that need fixed names (gateway, notification), define explicit `api-gateway-1/2`, `notification-service-1/2` services (YAML anchors keep it short) or use Docker DNS round robin for the service name.
- Profiles: default = full system; `tools` = Kafka UI, pgAdmin; `observability` = Prometheus + Grafana.

## Commands
```bash
cp .env.example .env
docker compose up --build            # everything
docker compose up -d postgres redis rabbitmq kafka   # infra only
docker compose --profile tools up -d
docker compose up --scale order-service=3 -d
docker compose logs -f order-service
docker compose down -v               # stop and delete volumes
```

## Image conventions
- Multi-stage Dockerfile per Java service: build with Gradle (or use `./gradlew bootBuildImage`), run on a JRE 21 base as a non-root user, expose the service port, `HEALTHCHECK` or rely on Compose healthcheck.
- Frontend: build with Node, serve static files with nginx.
- Image names: `stocksim/<service>:<git-sha|dev>`.

## `.env.example` (keys)
```
POSTGRES_USER=stocksim
POSTGRES_PASSWORD=change-me
RABBIT_USER=stocksim
RABBIT_PASSWORD=change-me
INTERNAL_TOKEN=change-me
JWT_PRIVATE_KEY_PATH=/run/secrets/jwt_private.pem
```
