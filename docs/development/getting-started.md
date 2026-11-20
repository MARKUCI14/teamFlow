# Getting Started

## Prerequisites
| Tool | Version | Check |
|---|---|---|
| JDK | 21 | `java -version` |
| Node.js | 22 LTS | `node -v` |
| Docker + Compose | recent | `docker compose version` |
| Git, GitHub CLI (`gh`) | recent | `gh auth status` |
| kubectl + kind (or k3d / Docker Desktop K8s) | for Kubernetes work | `kubectl version --client` |
| faas-cli / arkade (optional) | for OpenFaaS | `faas-cli version` |

Recommended IDEs: IntelliJ IDEA (backend), VS Code or WebStorm (frontend).

## Clone and open
```bash
git clone https://github.com/MARKUCI14/soa.git
cd soa
```

## Local run modes
1. **Everything in Docker**: `docker compose up --build`. Open http://localhost.
2. **Infra in Docker, services in IDE**: `docker compose up -d postgres redis rabbitmq kafka`, then run services with `./gradlew :<service>:bootRun --args='--spring.profiles.active=local'` and `npm run dev` in `frontend/`.
3. **Kubernetes**: see [deployment/kubernetes](../deployment/kubernetes.md).

## Environment
Copy `.env.example` to `.env` and adjust (never commit `.env`). Default local credentials are for development only.

## Useful URLs (Compose defaults)
| What | URL |
|---|---|
| App (via nginx) | http://localhost |
| RabbitMQ management | http://localhost:15672 |
| Kafka UI | http://localhost:8090 |
| Swagger UI per service | http://localhost:808x/swagger-ui.html |

## Suggested build order
Follow the [roadmap](../project/roadmap.md): skeleton + auth → market data + Kafka → orders + RabbitMQ + matching → portfolio → notifications + Redis → alerts → FaaS → frontend → scaling → docs/tutorial. Keep the system runnable at the end of every milestone.

## Create the GitHub backlog
```bash
gh auth login
./scripts/create-github-issues.sh MARKUCI14/soa
```
