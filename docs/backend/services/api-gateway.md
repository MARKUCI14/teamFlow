# API Gateway

The public REST entry point behind nginx. Validates JWTs, applies rate limiting and routes to the service that owns each API. It performs no project or issue business logic; downstream services enforce role and membership scope independently.
