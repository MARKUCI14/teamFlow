# TeamFlow

TeamFlow is a role-based project and issue management system for stakeholders, project managers, developers, and administrators. It is a university project for *Arhitecturi orientate pe servicii* (Service-Oriented Architectures).

A stakeholder submits a suggestion or bug report. A project manager reviews it and creates one or more developer issues. Developers update issue status and log work time. Project members receive live updates, and managers can request project reports.

The target architecture uses four Java Spring Boot microservices, secured REST, RabbitMQ, Kafka, an OpenFaaS report generator, and a React + TypeScript microfrontend application. nginx load-balances gateway replicas. The microservices are user, work-management, notification, and report; the API gateway and report function are separate components.

- Documentation index: [docs/README.md](docs/README.md)
- Architecture: [overview](docs/architecture/overview.md)
- Getting started: [guide](docs/development/getting-started.md)

## Status

Implementation is ongoing. See the [roadmap](docs/project/roadmap.md) for feature status and upcoming work.

## CI and main branch

The GitHub Actions workflow keeps the aggregate **`CI / ci`** status check available across backend, frontend, and Compose checks. Require this check before merging to `main`; see the [Git workflow and branch protection guide](docs/development/git-workflow.md). The frontend check uses `npm ci`, lint, tests, and build.

## Local backend stack

Start the backend applications and PostgreSQL, RabbitMQ, and authenticated Kafka dependencies with Docker Compose:

```bash
cp .env.example .env
./scripts/generate-jwt-keys.sh
./scripts/generate-kafka-certs.sh
docker compose config
docker compose up -d
docker compose ps
```

The generated JWT keys and Kafka TLS files stay under the ignored `.local/` directory. See the [Compose guide](docs/deployment/compose.md) for connection details and shutdown instructions.

## Frontend shell

The React shell, stakeholder remote, and team remote run locally with:

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`; the shell expects the API gateway at `http://localhost:8080` and the notification WebSocket at `ws://localhost:8083/ws`. See the [frontend guide](docs/frontend/guide.md) for environment overrides and checks.
