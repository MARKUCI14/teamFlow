# Tutorial Plan (grade P, ~3 pages)

The tutorial covers **one topic** implemented in StockSim, with a working example in a public repository (this repo, or a trimmed extract so readers can run it quickly).

## Candidate topics
1. **Scaling server-side notifications: WebSockets + Redis pub/sub across multiple Spring Boot instances** (recommended: very visible demo, small self-contained code).
2. **Message broker vs event stream in one system: RabbitMQ commands and Kafka events with Spring Boot.**
3. **Securing a REST API across microservices with JWT (gateway + resource servers).**

Pick one by milestone M5; write the text during M10 from the real code.

## Template
1. **Problem** (half page): what breaks without the technique (e.g. user connected to instance 1, event processed by instance 2).
2. **Concepts** (half page): short explanation + one diagram.
3. **Step-by-step implementation** (1.5 pages): dependencies, configuration, key classes with short code excerpts, how to run (`docker compose up --scale notification-service=2`).
4. **Verification** (half page): how to observe it working (logs, two browser tabs, killing an instance).
5. **Pitfalls and alternatives**: ordering, duplicates, reconnects, other approaches.
6. **Repository link and commit/tag** used for the tutorial.

## Repository requirements
- Public; README with run instructions that work from a clean clone.
- Tagged release matching the tutorial text.
