# REST API (planned)

All public requests go through nginx and api-gateway. Except auth, JWKS, and health, APIs require a JWT. Project resources also require membership and role checks. IDs are UUIDs; list endpoints are paginated.

The gateway routes auth and user endpoints to user-service, project/submission/issue/work-log endpoints to work-management-service, report endpoints to report-service, and `/ws` to notification-service.

## Authentication and projects

| Method | Endpoint | Purpose | Role |
|---|---|---|---|
| POST | `/api/auth/register`, `/api/auth/login` | Create account / receive tokens | Public |
| GET | `/api/users/me` | Current user and roles | Any authenticated |
| GET | `/api/projects` | List accessible projects | Member |
| POST | `/api/projects` | Create project | Admin or PM |
| GET | `/api/projects/{id}` | View project | Member, subject to project visibility |
| PATCH | `/api/projects/{id}` | Update project | Admin or PM |
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
| GET | `/api/projects/{id}/issues` | List visible project issues | Member, subject to project visibility |
| POST | `/api/projects/{id}/issues` | Create an issue directly | PM / Admin |
| GET | `/api/issues/{id}` | View issue | Member, subject to project visibility |
| PATCH | `/api/issues/{id}` | Update issue status | Assigned developer / PM / Admin |
| PATCH | `/api/issues/{id}` | Change issue assignee | PM / Admin |
| POST | `/api/issues/{id}/comments` | Add comment | Assigned developer / PM / Admin |
| GET | `/api/issues/{id}/worklogs` | List own entries or review issue entries | Developer (own) / PM / Admin |
| POST | `/api/issues/{id}/worklogs` | Log minutes and work date | Assigned developer (own entry) |
| GET | `/api/projects/{id}/worklogs` | Project time summary | PM / Admin |

## Reports and realtime

| Method | Endpoint | Purpose | Role |
|---|---|---|---|
| POST | `/api/projects/{id}/reports` | Queue activity/time PDF; returns 202 | PM / Admin |
| GET | `/api/reports/{id}` | Check report status | Requester / Admin |
| GET | `/api/reports/{id}/download` | Download completed PDF | Requester / Admin |
| WebSocket | `/ws` | STOMP `/user/queue/notifications` | Authenticated |

Use RFC 7807 ProblemDetail errors. Do not expose internal service endpoints through the gateway.
