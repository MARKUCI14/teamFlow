# Service Catalog

| Service | Port | Owns | Main API / messages |
|---|---:|---|---|
| api-gateway | 8080 | rate-limit state in Redis | secured public REST routing |
| user-service | 8081 | accounts, roles | `/api/auth/**`, `/api/users/**`, JWKS |
| project-service | 8082 | projects, memberships | `/api/projects/**`; `projects.*` |
| submission-service | 8083 | stakeholder submissions | `/api/submissions/**`; `submissions.*` |
| issue-service | 8084 | developer issues, comments | `/api/issues/**`; `issues.*` |
| worklog-service | 8085 | time entries | `/api/worklogs/**`; `worklogs.*` |
| notification-service | 8086 | ephemeral WebSocket sessions | `/ws`; consumes Kafka, Redis pub/sub |
| report-service | 8087 | report jobs and metadata | `/api/reports/**`; RabbitMQ report commands |
| report-function | 8088 | none | HTTP function that returns project report PDF |

Each service owns a separate PostgreSQL database/schema and never reads another service's tables. Kafka producers publish domain facts; consuming services use independent consumer groups. nginx listens on 80/443 and routes `/api`, `/ws`, and the web shell. Redis is used for rate limits and WebSocket fan-out; RabbitMQ handles report-generation work; Kafka streams project events.
