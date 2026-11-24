# Tutorial Plan (Grade P)

## Proposed title

**Securing a Spring Boot REST API with JWT and project roles**

## Working example

A small public repository based on TeamFlow's user-service and work-management-service. It demonstrates login, JWT validation, and role- and project-scoped access to a project workflow endpoint. Include Docker Compose, synthetic sample users, curl commands, and a short troubleshooting section. Do not include real credentials.

## Three-page outline

1. **Problem and architecture:** explain the user-service JWT issuer, API gateway, and work-management authorization boundary.
2. **Implementation:** show token issuance/validation and how project roles and membership protect a REST endpoint.
3. **Run and verify:** clone and start, log in as stakeholder and PM, call allowed and denied endpoints, and inspect the HTTP responses.

The public example must work from a clean clone and correspond to implemented system behavior. Do not present planned behavior as completed evidence.
