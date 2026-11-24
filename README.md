# TeamFlow

TeamFlow is a role-based project and issue management system for stakeholders, project managers, developers, and administrators. It is a university project for *Arhitecturi orientate pe servicii* (Service-Oriented Architectures).

A stakeholder submits a suggestion or bug report. A project manager reviews it and creates one or more developer issues. Developers update issue status and log work time. Project members receive live updates, and managers can request project reports.

The target architecture uses four Java Spring Boot microservices, secured REST, RabbitMQ, Kafka, an OpenFaaS report generator, and a React + TypeScript microfrontend application. nginx load-balances gateway replicas. The microservices are user, work-management, notification, and report; the API gateway and report function are separate components.

- Documentation index: [docs/README.md](docs/README.md)
- Architecture: [overview](docs/architecture/overview.md)
- Getting started: [guide](docs/development/getting-started.md)

## Status

Project setup and documentation phase. See the [roadmap](docs/project/roadmap.md).
