# Docker Compose Dependencies

The current root Compose file starts the local infrastructure dependencies only: PostgreSQL, RabbitMQ, and a single-node Kafka broker. The backend and frontend containers will be added in a later issue.

## Start

Copy the example environment file, generate a local Kafka TLS certificate, validate the Compose configuration, and start the dependencies:

```bash
cp .env.example .env
./scripts/generate-kafka-certs.sh
docker compose config
docker compose up -d
docker compose ps
```

Compose interpolates credentials from `.env`. Do not commit that file. Kafka uses SASL/PLAIN over TLS; its self-signed keystore, truststore, and password files are generated under `.local/kafka-certs/`, which is ignored by Git. The certificate covers the internal `kafka` hostname and host-side `localhost` address. These files are readable by the local Kafka container user and are for development only; do not put real or production credentials in `.env`.

## Local endpoints

All published ports bind to localhost:

| Dependency | Address | Health check |
|---|---|---|
| PostgreSQL | `localhost:5432`, database `teamflow` | `pg_isready` |
| RabbitMQ | `localhost:5672`; management UI `http://localhost:15672` | `rabbitmq-diagnostics ping` |
| Kafka | `localhost:9094` (SASL/PLAIN + TLS) | Authenticated Kafka broker API request |

Containers on the Compose network use `postgres:5432`, `rabbitmq:5672`, and `kafka:9092`. For Kafka host clients, use `localhost:9094` with SASL/PLAIN and the generated truststore. Example credentials are in `.env.example` and should only be used locally.

This setup provides one PostgreSQL database instance for the local stack. When a service adds persistence, its schema or database ownership must follow the service boundary documented in the [service catalog](../architecture/services.md). This dependency issue does not add database access to the Spring services.

## Stop

```bash
docker compose down
```

Remove `.local/kafka-certs/` and rerun the certificate script to regenerate the local Kafka certificate. Avoid printing or sharing `docker compose config` output when `.env` contains non-example values because Compose renders interpolated environment values.
