# Docker Compose Deployment (planned)

Compose will run nginx, the React shell and its stakeholder/team remotes, two API-gateway replicas, the four internal microservices (user, work-management, notification, and report), PostgreSQL, RabbitMQ, Kafka, and the report function. Keep `.env` local and use `.env.example` only for variable names.

```bash
docker compose up --build
```

Expose the web app on port 80, RabbitMQ management and Kafka UI only for local development. Use health checks and service-name DNS. Run two API-gateway replicas behind nginx to demonstrate load balancing; keep notification-service at one instance. All services must use environment-based configuration. Compose files are planned; see [roadmap](../project/roadmap.md).
