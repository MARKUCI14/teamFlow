# Kubernetes Deployment (planned)

The target local deployment uses kind, k3d, or Docker Desktop with Kustomize. Deploy each API as a Deployment and ClusterIP Service; use Ingress/nginx for web, REST, and WebSocket routing. Configure health/readiness probes, resource requests, ConfigMaps, and Secrets. Use separate PostgreSQL databases per service and persistent broker volumes for the demo.

Deploy report-function through OpenFaaS and invoke it from report-service with a scoped report snapshot. Demonstrate replica scaling for gateway and notification-service. Kubernetes manifests are planned and will be added after the Compose path is runnable.
