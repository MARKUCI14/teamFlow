# Kubernetes Deployment (planned)

The target local deployment uses kind, k3d, or Docker Desktop with Kustomize. Deploy the API gateway and four internal microservices as Deployments and ClusterIP Services; use Ingress/nginx for web, REST, and WebSocket routing. Configure health/readiness probes, resource requests, ConfigMaps, and Secrets. Use separate PostgreSQL databases or schemas for stateful services and persistent broker volumes for the demo.

Deploy report-function through OpenFaaS and invoke it from report-service with a scoped report snapshot. Demonstrate load balancing with two API-gateway replicas; notification-service remains a single instance in the initial demo. Kubernetes manifests are planned and will be added after the Compose path is runnable.
