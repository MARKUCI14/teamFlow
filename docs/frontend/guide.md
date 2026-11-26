# Frontend Guide

The frontend is an npm workspace with a React + strict TypeScript shell, a shared API/notification package, and two Vite Module Federation remotes. The remotes in this issue are small loadable workspaces; their project-specific workflows can be added in their own issues.

```text
frontend/
  shell/        login, navigation, route composition, notification connection
  stakeholder/  stakeholder remote workspace
  team/         PM, developer, and admin remote workspace
  shared/       auth/API types, API client, STOMP connection
```

## Run locally

Install the workspace dependencies and start all three Vite applications:

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`. The shell loads the remotes from `http://localhost:5174/remoteEntry.js` and `http://localhost:5175/remoteEntry.js`. To use different addresses, set `VITE_STAKEHOLDER_REMOTE_URL` and `VITE_TEAM_REMOTE_URL` in `shell/.env.local`.

The defaults connect REST requests to the API gateway at `http://localhost:8080` and STOMP to `ws://localhost:8083/ws`. Set `VITE_API_BASE_URL` and `VITE_NOTIFICATION_URL` in `shell/.env.local` when these endpoints differ. These values are public browser configuration, not secrets. Start the backend and gateway separately using the [Compose guide](../deployment/compose.md).

## Authentication and API calls

The shell supports registration and login through the gateway. After receiving the access token, it requests `/api/users/me` and keeps the token in memory. Reloading the page clears the session, so the user must sign in again. Logout clears the in-memory session. The shared API client adds `Authorization: Bearer <token>` to authenticated calls and turns API errors into readable messages.

Navigation exposes the stakeholder and team workspaces to authenticated users. This is only route composition; backend services remain responsible for authorization and project membership.

## Remotes and notifications

The stakeholder and team Vite applications expose `./RemoteApp` through Module Federation. The shell lazy-loads each remote and displays loading or unavailable states. Both remotes can also run on their own for local preview.

The shell owns one STOMP connection and subscribes to `/user/queue/notifications`. It sends the access token in the STOMP CONNECT `Authorization` header, reconnects after disconnects, and shows connection state. The notification service must implement the documented authenticated `/ws` endpoint for this connection to reach `connected`; its backend implementation is separate from this frontend shell.

## Checks

From `frontend/`, run:

```bash
npm run lint
npm test
npm run build
```

CI also requires the committed `package-lock.json` and runs these scripts. See the [REST API contract](../api/rest-api.md) and [security architecture](../architecture/security.md).
