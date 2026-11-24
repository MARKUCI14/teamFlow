# ADR 0003: Spring Cloud Function for Project Reports

- Status: Accepted
- Date: 2026-11-23

## Context

The course requires a FaaS. Project managers need a useful report of issue activity and logged time.

## Decision

Implement a stateless Spring Cloud Function that accepts a project-scoped report snapshot and returns a PDF. report-service owns authorization, job state, and result metadata. Run the same function container locally and through OpenFaaS in Kubernetes.

## Consequences

The function is independently scalable and contains no user database credentials. The report-service must prepare a minimal authorized snapshot and safely store or retrieve the output.
