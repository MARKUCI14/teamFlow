#!/usr/bin/env bash
# Creates labels, milestones and the initial backlog.
# Usage: ./scripts/create-github-issues.sh MARKUCI14/soa
# Requires: GitHub CLI (gh) authenticated (gh auth login). Safe to re-run for labels; issues/milestones are NOT deduplicated.
set -euo pipefail
REPO="${1:?usage: $0 <owner/repo>}"

label() { gh label create "$1" --color "$2" --description "$3" --force --repo "$REPO" >/dev/null; }
label feat 1d76db "New capability"
label task 0e8a16 "Technical/setup work"
label bug d73a4a "Something is broken"
label docs 0075ca "Documentation"
label backend 5319e7 "Java/Spring services"
label frontend fbca04 "React/TypeScript apps"
label infra 006b75 "Docker, Kubernetes, CI"
label security b60205 "Security"
label messaging c5def5 "RabbitMQ / Kafka / Redis"
label rubric ff9f1c "Directly earns rubric points"
label blocked 000000 "Waiting on something"

milestones=(
"M0 Setup" "M1 Foundation" "M2 Market data" "M3 Trading core" "M4 Portfolio"
"M5 Realtime" "M6 Alerts" "M7 FaaS" "M8 Frontend" "M9 Scale and deploy" "M10 Finish"
)
for m in "${milestones[@]}"; do
  gh api "repos/$REPO/milestones" -f title="$m" >/dev/null 2>&1 || echo "milestone exists or failed: $m"
done

issue() { # title | labels | milestone | body
  gh issue create --repo "$REPO" --title "$1" --label "$2" --milestone "$3" --body "$4" >/dev/null
  echo "created: $1"
}

# ---- M0 Setup
issue "chore(infra): protect main and enable CI" "task,infra" "M0 Setup" $'## Goal\nBranch protection and a green CI skeleton.\n\n## Steps\n- [ ] Require PR + status check `CI` on main\n- [ ] Verify .github/workflows/ci.yml runs on an empty project\n\n## Definition of done\n- [ ] Direct pushes to main blocked'
issue "docs: review architecture docs and record decisions" "docs" "M0 Setup" $'## Goal\nReview docs/ with the course requirements and confirm ADRs.\n\n- [ ] Confirm the 10th rubric point with the professor\n- [ ] Accept ADR-0001..0003'

# ---- M1 Foundation
issue "chore(backend): Gradle multi-project skeleton with version catalog" "task,backend" "M1 Foundation" $'## Goal\nbackend/ with settings, version catalog, convention plugin, `common` module.\n\n## Definition of done\n- [ ] `./gradlew build` passes\n- [ ] Spotless configured'
issue "chore(infra): docker-compose with postgres, redis, rabbitmq, kafka" "task,infra" "M1 Foundation" $'## Goal\nInfra-only Compose run with healthchecks and init-db.sql (one DB per service).\n\n## Definition of done\n- [ ] `docker compose up -d` healthy\n- [ ] .env.example documented'
issue "feat(user): registration, login and JWT issuing (RS256, JWKS)" "feat,backend,security,rubric" "M1 Foundation" $'## Acceptance criteria\n- [ ] Register/login/refresh/logout\n- [ ] BCrypt, refresh rotation\n- [ ] JWKS endpoint\n- [ ] Tests for 401/403 paths\n\nRubric: secured REST API'
issue "feat(gateway): Spring Cloud Gateway with JWT validation and rate limiting" "feat,backend,security,rubric" "M1 Foundation" $'## Acceptance criteria\n- [ ] Routes to all services under /api\n- [ ] JWT validation, CORS, Redis rate limiter\n- [ ] /internal/** not routed\n\nRubric: secured REST API'
issue "feat(common): event/command contracts and shared error handling" "feat,backend,messaging" "M1 Foundation" $'## Acceptance criteria\n- [ ] Envelope<T>, commands, events per docs/architecture/messaging.md\n- [ ] ProblemDetail advice, resource-server config\n- [ ] Contract tests with sample JSON'
issue "chore(infra): Dockerfiles and compose entries for user-service and gateway" "task,infra,rubric" "M1 Foundation" $'Multi-stage Dockerfile, non-root, healthcheck, compose wiring.\n\nRubric: containers'

# ---- M2 Market data
issue "feat(market): symbol management and price simulation" "feat,backend" "M2 Market data" $'## Acceptance criteria\n- [ ] Symbols CRUD (ADMIN)\n- [ ] Geometric random walk, configurable tick/volatility/pause\n- [ ] REST: symbols, latest prices, history'
issue "feat(market): publish price ticks to Kafka and cache latest in Redis" "feat,backend,messaging,rubric" "M2 Market data" $'## Acceptance criteria\n- [ ] Topic market.ticks (3 partitions, key=symbol)\n- [ ] Redis key price:<symbol>\n- [ ] Integration test with Testcontainers\n\nRubric: event streaming'

# ---- M3 Trading core
issue "feat(order): order REST API and persistence" "feat,backend" "M3 Trading core" $'## Acceptance criteria\n- [ ] POST/GET/DELETE /api/orders\n- [ ] Owner checks, validation, Idempotency-Key\n- [ ] State machine per docs'
issue "feat(order): publish place/cancel commands to RabbitMQ" "feat,backend,messaging,rubric" "M3 Trading core" $'## Acceptance criteria\n- [ ] orders.exchange, orders.place, orders.cancel, DLX\n- [ ] Publisher confirms\n\nRubric: message broker'
issue "feat(matching): consume order commands and execute market orders" "feat,backend,messaging" "M3 Trading core" $'## Acceptance criteria\n- [ ] Consume orders.place, read latest price\n- [ ] Dedupe on orderId, retry then DLQ'
issue "feat(matching): limit order book evaluated on price ticks" "feat,backend" "M3 Trading core" $'## Acceptance criteria\n- [ ] Persisted open orders\n- [ ] Fill when price crosses limit\n- [ ] Cancel support'
issue "feat(matching): publish trades.executed and orders.rejected to Kafka" "feat,backend,messaging,rubric" "M3 Trading core" $'Topics keyed by userId, schemas per docs.\n\nRubric: event streaming'
issue "feat(order): consume trade/rejection events and update order status" "feat,backend,messaging" "M3 Trading core" $'Own consumer group `orders`; idempotent, valid transitions only.'

# ---- M4 Portfolio
issue "feat(portfolio): accounts, reservations and internal API" "feat,backend,security" "M4 Portfolio" $'## Acceptance criteria\n- [ ] Starting cash on registration (event or lazy create)\n- [ ] POST/DELETE /internal/reservations with internal token\n- [ ] Insufficient funds -> 409'
issue "feat(portfolio): apply executed trades (holdings, cash, P&L)" "feat,backend,messaging" "M4 Portfolio" $'Consumer group `portfolio`; processed tradeId table for idempotency; P&L with latest prices.'
issue "feat(order): reserve funds before accepting an order" "feat,backend" "M4 Portfolio" $'order-service calls portfolio-service /internal/reservations; release on reject/cancel.'

# ---- M5 Realtime
issue "feat(notification): STOMP WebSocket endpoint with JWT on CONNECT" "feat,backend,security" "M5 Realtime" $'## Acceptance criteria\n- [ ] /ws STOMP, channel interceptor validates JWT\n- [ ] /user/queue/notifications'
issue "feat(notification): Redis pub/sub fan-out across instances" "feat,backend,messaging,rubric" "M5 Realtime" $'## Acceptance criteria\n- [ ] Two instances; event handled on A reaches user connected to B\n- [ ] Channels: notifications, prices\n\nRubric: scalable WebSockets'
issue "feat(notification): consume Kafka events and RabbitMQ commands" "feat,backend,messaging" "M5 Realtime" $'Kafka: trades.executed, orders.rejected, market.ticks (group ws-prices). RabbitMQ: notifications.send.'

# ---- M6 Alerts
issue "feat(alert): alert CRUD API" "feat,backend" "M6 Alerts" $'POST/GET/DELETE /api/alerts, owner checks.'
issue "feat(alert): evaluate alerts on ticks and send notification command" "feat,backend,messaging,rubric" "M6 Alerts" $'Consume market.ticks (group alerts); publish notifications.send; mark TRIGGERED once.\n\nRubric: message broker'

# ---- M7 FaaS
issue "feat(report): Spring Cloud Function that renders portfolio PDF" "feat,backend,rubric" "M7 FaaS" $'Function<ReportRequest, byte[]>; unit test; container image.\n\nRubric: FaaS'
issue "feat(infra): deploy report-function on OpenFaaS and integrate with portfolio-service" "feat,infra,rubric" "M7 FaaS" $'stack.yml, faas-cli deploy, REPORT_FUNCTION_URL per environment, report status + notification when ready.'

# ---- M8 Frontend
issue "feat(shell): Vite + React + TS host app with login and routing" "feat,frontend,rubric" "M8 Frontend" $'Auth store, layout, lazy remotes, notification toasts.\n\nRubric: web app'
issue "feat(shared): typed API client, auth store, event bus, WebSocket hook" "feat,frontend" "M8 Frontend" $'Generated OpenAPI types, 401 refresh flow, useNotifications().'
issue "feat(market-mfe): live prices and charts remote" "feat,frontend,rubric" "M8 Frontend" $'Federated remote, standalone dev mode.\n\nRubric: micro frontend'
issue "feat(trading-mfe): order form, orders, portfolio, alerts, reports remote" "feat,frontend,rubric" "M8 Frontend" $'Federated remote; realtime order status updates.\n\nRubric: micro frontend'
issue "feat(admin-mfe): symbols and simulation controls remote" "feat,frontend" "M8 Frontend" $'ADMIN-only routes, symbol CRUD, simulation controls.'

# ---- M9 Scale and deploy
issue "feat(infra): nginx edge load balancer config" "feat,infra,rubric" "M9 Scale and deploy" $'Upstreams: gateway x2, notifications x2 (WebSocket upgrade), static shell.\n\nRubric: load balancing'
issue "chore(infra): Dockerfiles and compose for all services, scaling demo" "task,infra,rubric" "M9 Scale and deploy" $'`docker compose up --scale order-service=3`; X-Served-By header proof.\n\nRubric: containers'
issue "chore(infra): Kubernetes manifests (kustomize) with ingress and HPA" "task,infra,rubric" "M9 Scale and deploy" $'base + overlays/local, probes, ConfigMaps/Secrets, HPA for order and notification.\n\nRubric: containers'
issue "test(infra): k6 load test and scaling evidence" "task,infra" "M9 Scale and deploy" $'scripts/load/*.js; record results for demo/tutorial.'

# ---- M10 Finish
issue "docs: finalize C4 and UML diagrams to match implementation" "docs,rubric" "M10 Finish" $'Verify every diagram against the running system; export PNG/SVG if Mermaid C4 renders poorly.\n\nRubric: documentation'
issue "docs: write tutorial (~3 pages) and publish example repo" "docs,rubric" "M10 Finish" $'Follow docs/tutorial/outline.md; public repo + tag; verify from a clean clone.\n\nGrade P'
issue "chore: demo rehearsal and README polish" "task" "M10 Finish" $'Run the demo script in docs/development/rubric-checklist.md end to end.'

echo "Done. See https://github.com/$REPO/issues"
