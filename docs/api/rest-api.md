# REST API (planned)

All public requests go through nginx and api-gateway. Except auth, JWKS, and health, APIs require a JWT. Project resources also require membership and role checks. IDs are UUIDs; list endpoints are paginated.

## Authentication and projects

| Method | Endpoint | Purpose | Role |
|---|---|---|---|
| POST | `/api/auth/register`, `/api/auth/login` | Create account / receive tokens | Public |
| GET | `/api/users/me` | Current user and roles | Any authenticated |
| GET/POST | `/api/projects` | List accessible / create project | Member / Admin or PM |
| GET/PATCH | `/api/projects/{id}` | View/update project | Member / Admin or PM |
| POST | `/api/projects/{id}/members` | Add project member with role | PM / Admin |

## Stakeholder submissions

| Method | Endpoint | Purpose | Role |
|---|---|---|---|
| POST | `/api/projects/{id}/submissions` | Submit bug or suggestion | Stakeholder / PM |
| GET | `/api/projects/{id}/submissions` | List project submissions | Member; visibility policy applies |
| GET | `/api/submissions/{id}` | View submission | Author / PM / Admin |
| PATCH | `/api/submissions/{id}` | Triage status or request clarification | PM / Admin |
| POST | `/api/submissions/{id}/issues` | Create linked issue(s) | PM / Admin |

## Issues and work logs

| Method | Endpoint | Purpose | Role |
|---|---|---|---|
| GET/POST | `/api/projects/{id}/issues` | List / create issue | Member / PM |
| GET/PATCH | `/api/issues/{id}` | View / update issue status or assignment | Member / assignee / PM |
| POST | `/api/issues/{id}/comments` | Add comment | Project member |
| GET/POST | `/api/issues/{id}/worklogs` | List / log minutes and work date | Developer / PM |
| GET | `/api/projects/{id}/worklogs` | Project time summary | PM / Admin |

## Reports and realtime

| Method | Endpoint | Purpose | Role |
|---|---|---|---|
| POST | `/api/projects/{id}/reports` | Queue activity/time PDF; returns 202 | PM / Admin |
| GET | `/api/reports/{id}` | Check report status | Requester / Admin |
| GET | `/api/reports/{id}/download` | Download completed PDF | Requester / Admin |
| WebSocket | `/ws` | STOMP `/user/queue/notifications` | Authenticated |

Use RFC 7807 ProblemDetail errors. Do not expose internal service endpoints through the gateway.
