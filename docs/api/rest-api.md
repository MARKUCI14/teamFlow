# REST API (planned)

All public requests go through nginx and api-gateway. Except auth, JWKS, and health, APIs require a JWT. `ADMIN` is a global role. `PROJECT_MANAGER`, `STAKEHOLDER`, and `DEVELOPER` are project membership roles checked by work-management-service for the project being accessed. IDs are UUIDs; list endpoints are paginated. `PM` below means a member with the project-scoped `PROJECT_MANAGER` role; `Admin` means global `ADMIN`.

The gateway routes auth and user endpoints to user-service, project/submission/issue/work-log endpoints to work-management-service, report endpoints to report-service, and `/ws` to notification-service. Registration and login return `accessToken`, `tokenType`, and `expiresIn` (seconds). Send the token as `Authorization: Bearer <accessToken>`.

## Authentication and projects

| Method | Endpoint | Purpose | Role |
|---|---|---|---|
| POST | `/api/auth/register`, `/api/auth/login` | Create account / receive tokens | Public |
| GET | `/api/users/me` | Current user and roles | Any authenticated |
| GET | `/.well-known/jwks.json` | Public RS256 signing keys | Public |
| GET | `/api/projects` | List accessible projects | Authenticated; returns accessible projects only |
| POST | `/api/projects` | Create project | Admin or PM; creator receives project membership |
| GET | `/api/projects/{id}` | View project | Project member, subject to project visibility |
| PATCH | `/api/projects/{id}` | Update project | Project PM / Admin |
| POST | `/api/projects/{id}/members` | Add project member with role | Project PM / Admin |

## Stakeholder submissions

| Method | Endpoint | Purpose | Role |
|---|---|---|---|
| POST | `/api/projects/{id}/submissions` | Submit bug or suggestion | Project stakeholder / project PM |
| GET | `/api/projects/{id}/submissions` | List project submissions | Project member; visibility policy applies |
| GET | `/api/submissions/{id}` | View submission | Author / project PM / Admin |
| PATCH | `/api/submissions/{id}` | Triage status or request clarification | Project PM / Admin |
| POST | `/api/submissions/{id}/issues` | Create linked issue(s) and assign developer(s) | Project PM / Admin |

## Issues and work logs

| Method | Endpoint | Purpose | Role |
|---|---|---|---|
| GET | `/api/projects/{id}/issues` | List visible project issues | Project member; visibility policy applies |
| POST | `/api/projects/{id}/issues` | Create an issue directly | Project PM / Admin |
| GET | `/api/issues/{id}` | View issue | Project member; visibility policy applies |
| PATCH | `/api/issues/{id}` | Update issue status | Assigned project developer / project PM / Admin |
| PATCH | `/api/issues/{id}` | Change issue assignee | Project PM / Admin |
| POST | `/api/issues/{id}/comments` | Add comment | Assigned project developer / project PM / Admin |
| GET | `/api/issues/{id}/worklogs` | List own entries or review issue entries | Developer (own) / project PM / Admin |
| POST | `/api/issues/{id}/worklogs` | Log minutes and work date | Assigned project developer (own entry) |
| GET | `/api/projects/{id}/worklogs` | Project time summary | Project PM / Admin |

## Reports and realtime

| Method | Endpoint | Purpose | Role |
|---|---|---|---|
| POST | `/api/projects/{id}/reports` | Queue activity/time PDF; returns 202 | Project PM / Admin |
| GET | `/api/reports/{id}` | Check report status | Requester / Admin |
| GET | `/api/reports/{id}/download` | Download completed PDF | Requester / Admin |
| WebSocket | `/ws` | STOMP `/user/queue/notifications` | Authenticated |

Use RFC 7807 ProblemDetail errors. Do not expose internal service endpoints through the gateway.

Registration and login accept `{"email":"person@example.com","password":"at-least-8-characters"}`. Registration returns 201; duplicate email returns 409; invalid credentials return 401. New accounts have no global role until assigned by an administrative process. Project roles are not global token roles.

## Workflow notifications

After a submission is stored, work-management-service publishes `SubmissionCreated`; notification-service sends a private WebSocket notification to that project's PM. After the PM triages the submission and creates/assigns a linked issue, `IssueCreated` causes notification-service to send a private assignment notification to the assigned developer. Notifications are delivered to authenticated principals, never to a client-selected user ID.
