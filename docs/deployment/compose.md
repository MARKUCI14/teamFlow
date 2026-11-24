# Docker Compose Deployment (planned)

Compose will run nginx, shell and microfrontend assets, gateway replicas, user/project/submission/issue/worklog/notification/report services, PostgreSQL, Redis, RabbitMQ, Kafka, and the report function. Keep `.env` local and use `.env.example` only for variable names.

```bash
docker compose up --build
```

Expose the web app on port 80, RabbitMQ management and Kafka UI only for local development. Use health checks and service-name DNS. Scale stateless APIs and notification replicas for demonstrations; nginx must have resolvable upstreams. All services must use environment-based configuration. Compose files are planned; see [roadmap](../project/roadmap.md).
