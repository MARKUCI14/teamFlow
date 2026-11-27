# REST API (planned)

All public requests go through nginx and api-gateway. Except auth, JWKS, and health, APIs require a JWT. `ADMIN` is a global role. `PROJECT_MANAGER`, `STAKEHOLDER`, and `DEVELOPER` are project membership roles checked by work-management-service for the project being accessed. IDs are UUIDs; list endpoints are paginated. `PM` below means a member with the project-scoped `PROJECT_MANAGER` role; `Admin` means global `ADMIN`.

The API gateway routes auth and user endpoints to user-service, project/submission/issue/work-log endpoints to work-management-service, and report endpoints to report-service. The `/ws` WebSocket path is separate from these REST gateway routes and requires its own notification proxy. Registration and login return `accessToken`, `tokenType`, and `expiresIn` (seconds). Send the token as `Authorization: Bearer <accessToken>`.

## Authentication and projects

| Method | Endpoint | Purpose | Role |
|---|---|---|---|
| POST | `/api/auth/register`, `/api/auth/login` | Create account / receive tokens | Public |
| GET | `/api/users/me` | Current user and roles | Any authenticated |
| GET | `/.well-known/jwks.json` | Public RS256 signing keys | Public |
| GET | `/api/projects` | List the caller's projects (paginated) | Project member; returns projects where the caller is a member |
| POST | `/api/projects` | Create a project | Global Admin or a user who is already a PM; creator becomes a PM member |
| GET | `/api/projects/{id}` | View project details | Project member; non-members receive 404 |
| PATCH | `/api/projects/{id}` | Update project name or description | Project PM or a global Admin who is a project member |
| POST | `/api/projects/{id}/members` | Add a member with a project role | Project PM or global Admin |

Project creation accepts `{"name":"TeamFlow","description":"Optional description"}`. The name is required. `PATCH /api/projects/{id}` accepts either or both fields; omitted fields are unchanged. Membership creation accepts `{"userId":"<account-uuid>","role":"DEVELOPER"}`. Roles are `PROJECT_MANAGER`, `STAKEHOLDER`, and `DEVELOPER`. A project has one membership per user; attempting to add a duplicate returns 409. The `userId` is stored as an account reference owned by user-service; work-management-service does not read the user-service database or verify account existence.

All project members can list and read their projects. A global Admin who is not a member cannot read project data; an Admin can create projects and add members. Authenticated users without permission to create projects receive 403. Unknown projects and projects the caller is not a member of return 404.

## Stakeholder submissions

| Method | Endpoint | Purpose | Role |
|---|---|---|---|
| POST | `/api/projects/{id}/submissions` | Submit bug or suggestion | Project stakeholder |
| GET | `/api/projects/{id}/submissions` | List permitted submissions (paginated) | Project stakeholder (own) / project PM or member Admin (all) |
| GET | `/api/submissions/{id}` | View submission | Author / project PM or member Admin |
| PATCH | `/api/submissions/{id}` | Triage status or request clarification | Project PM or member Admin |
| POST | `/api/submissions/{id}/issues` | Create linked issues and assign developers | Project PM or member Admin |

Submission creation accepts `{"title":"Login fails","description":"The form rejects a valid password.","type":"BUG"}`. `title` and `description` are required; `type` is `BUG` or `SUGGESTION`. New submissions have status `PENDING`. Stakeholders see only their own submissions; PMs and global Admins who are project members can see all submissions in that project.

Triage accepts `{"status":"NEEDS_CLARIFICATION","clarificationMessage":"Which browser did you use?"}`. Statuses are `PENDING`, `NEEDS_CLARIFICATION`, `ACCEPTED`, and `REJECTED`; the clarification message is optional and is cleared when omitted from a later triage request.

Linked issue creation accepts a non-empty `issues` array, for example `{"issues":[{"title":"Fix login validation","description":"Accept valid passwords.","assigneeId":"<developer-uuid>"}]}`. Each issue has a required title and description and an optional assignee. Any assignee must be a project member with the `DEVELOPER` role. The response is an array of created issues, each linked to the source submission.

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

After a submission is stored, work-management-service publishes `SubmissionCreated` to Kafka for the project's notification flow. Notification delivery is handled by the notification-service as that consumer is implemented. Notifications are delivered to authenticated principals, never to a client-selected user ID.
