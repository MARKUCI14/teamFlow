# ADR 0003: Spring Cloud Function for Project Reports

- Status: Accepted
- Date: 2026-11-23

## Context

The course requires a FaaS. Project managers need a useful report of issue activity and logged time.

## Decision

Implement a stateless Spring Cloud Function that accepts an authorized, project-scoped report snapshot and returns a PDF. report-service owns request state and result metadata; work-management-service checks project membership and prepares the snapshot. report-service coordinates generation through the `reports.generate` RabbitMQ command. Run the same function container locally and through OpenFaaS in Kubernetes.

## Consequences

The function is independently scalable and contains no user database credentials. It receives only the minimal snapshot authorized by work-management-service; report-service stores the job state and result metadata.
