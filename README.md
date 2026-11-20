# StockSim

Stock trading simulator built with a service-oriented architecture (Java Spring Boot microservices, RabbitMQ, Kafka, Redis, a FaaS report generator, and a React + TypeScript micro-frontend web app). Market prices can come from a simulated provider or a real-data provider; simulation remains available for offline demos.

University project for *Arhitecturi orientate pe servicii* (UBB Cluj).

- Documentation index: [`docs/README.md`](docs/README.md)
- Architecture: [`docs/architecture/overview.md`](docs/architecture/overview.md)
- Getting started: [`docs/development/getting-started.md`](docs/development/getting-started.md)
- Contributor/AI guidance: [`CLAUDE.md`](CLAUDE.md)

## Quick start

```bash
docker compose up --build      # once services exist
# open http://localhost
```

> Status: project setup phase. See [`docs/project/roadmap.md`](docs/project/roadmap.md).
