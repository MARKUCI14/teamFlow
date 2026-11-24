# Getting Started

This repository currently contains documentation and setup plans; backend, frontend, and deployment implementation is tracked in the [roadmap](../project/roadmap.md).

## Planned stack

Java 21 / Spring Boot / Gradle, PostgreSQL, RabbitMQ, Kafka, React + TypeScript, Docker Compose, nginx, and OpenFaaS.

## Build order

Follow the roadmap: auth and gateway → combined work-management service → Kafka-backed notifications → report-service and OpenFaaS function → React shell and two remotes → nginx load-balancing demo, deployment, diagrams, and tutorial.

Keep the runnable demo path focused on stakeholder request through developer work tracking.
