# Kubernetes (and OpenFaaS)

## Layout (kustomize)
```
deploy/k8s/
├── base/
│   ├── namespace.yaml
│   ├── infra/        # postgres, redis, rabbitmq, kafka (StatefulSets or Helm values)
│   ├── services/     # one folder per service: deployment.yaml, service.yaml, configmap.yaml
│   ├── frontend/     # shell deployment + service
│   ├── ingress.yaml  # nginx ingress: /api -> api-gateway, /ws -> notification-service, / -> shell
│   └── kustomization.yaml
├── overlays/
│   ├── local/        # kind/k3d: NodePort/ingress host, low resources, 1-2 replicas
│   └── prod-like/    # more replicas, HPAs
└── openfaas/
    └── stack.yml     # report-function definition
```

## Cluster setup (local)
```bash
kind create cluster --name stocksim          # or k3d / Docker Desktop
# nginx ingress controller
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml
kubectl apply -k deploy/k8s/overlays/local
```
Load locally built images into kind: `kind load docker-image stocksim/order-service:dev --name stocksim`.

## Requirements per workload
- `Deployment` with readiness (`/actuator/health/readiness`) and liveness probes, resource requests/limits.
- `ConfigMap` for config, `Secret` for passwords, JWT keys, `INTERNAL_TOKEN`.
- `Service` (ClusterIP) per service; Kubernetes provides the load balancing between replicas.
- `HorizontalPodAutoscaler` for `order-service` and `notification-service` (CPU target 60%, min 2, max 5).
- Ingress routes WebSocket traffic: set annotations `nginx.ingress.kubernetes.io/proxy-read-timeout: "3600"` and `proxy-send-timeout: "3600"`.
- Stateful infra: simplest is community Helm charts (Bitnami-style) for PostgreSQL, Redis, RabbitMQ, Kafka with small resource settings; note this in the docs as a dev-only choice.

## OpenFaaS for report-function
Install (one of):
```bash
arkade install openfaas
# or Helm: helm repo add openfaas https://openfaas.github.io/faas-netes/ && helm upgrade --install openfaas openfaas/openfaas -n openfaas --create-namespace
```
`stack.yml` sketch (custom Dockerfile + of-watchdog in HTTP mode):
```yaml
version: 1.0
provider:
  name: openfaas
  gateway: http://127.0.0.1:8080
functions:
  report-function:
    image: stocksim/report-function:dev
    labels:
      com.openfaas.scale.min: "1"
      com.openfaas.scale.max: "5"
```
Deploy and invoke:
```bash
faas-cli deploy -f deploy/k8s/openfaas/stack.yml
curl -X POST -H "Content-Type: application/json" -d @sample-report-request.json \
  http://<openfaas-gateway>/function/report-function --output report.pdf
```
portfolio-service reads `REPORT_FUNCTION_URL`: in Compose `http://report-function:8088/report`, in K8s `http://gateway.openfaas:8080/function/report-function`.
Use `scale.min: "0"` to demonstrate scale-to-zero (expect a JVM cold start on the first call).

## Demo commands
```bash
kubectl get pods -n stocksim
kubectl scale deployment order-service --replicas=5 -n stocksim
kubectl top pods -n stocksim
```
