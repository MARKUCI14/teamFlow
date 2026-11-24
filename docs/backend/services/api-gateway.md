# API Gateway

The public REST entry point behind nginx. Validates JWTs and routes requests to user-service, work-management-service, or report-service. nginx load-balances stateless gateway replicas. The gateway performs no business logic; each owning service enforces role and membership scope independently.
