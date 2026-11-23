# Getting Started

This repository currently contains documentation and setup plans; backend, frontend, and deployment implementation is tracked in the [roadmap](../project/roadmap.md).

## Planned stack

Java 21 / Spring Boot / Gradle, PostgreSQL, RabbitMQ, Kafka, Redis, React + TypeScript, Docker Compose, nginx, and OpenFaaS.

## Build order

Follow the roadmap: auth and gateway → projects/membership → stakeholder submissions → manager triage and developer issues → work logs → Kafka notifications and Redis WebSocket fan-out → FaaS report → microfrontends → scaling, deployment, diagrams, and tutorial.

Keep the runnable demo path focused on stakeholder request through developer work tracking.
