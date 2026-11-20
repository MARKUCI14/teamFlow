# ADR-0003: FaaS via Spring Cloud Function (OpenFaaS on Kubernetes, container on Compose)

- Status: accepted

## Context
The report generator is stateless and bursty, a good FaaS candidate. The project must run on both Docker Compose and Kubernetes, and the team uses Java.

## Decision
Implement `report-function` with Spring Cloud Function (a `Function<ReportRequest, byte[]>`). On Kubernetes, deploy it with OpenFaaS (of-watchdog in HTTP mode wrapping the same container, autoscaling and scale-to-zero). On Compose, run the same image as a normal container exposing the same HTTP contract. portfolio-service calls it through a configurable base URL.

## Consequences
+ One artifact, two runtimes; real FaaS behavior (invoke, autoscale, cold start) on K8s.
- JVM cold start is slow; mitigate with minimal dependencies, optional GraalVM native image, or `min-replicas: 1`.
- OpenFaaS adds install steps (documented in `docs/deployment/kubernetes.md`).

## Alternatives considered
Knative Serving (heavier), LocalStack Lambda (not Kubernetes-native), a plain REST service (does not satisfy "FaaS").
