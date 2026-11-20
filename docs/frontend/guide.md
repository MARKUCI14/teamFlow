# Frontend Guide

## Stack
React 18+ with TypeScript (strict), Vite, Module Federation (`@module-federation/vite`), React Router, TanStack Query, `@stomp/stompjs`, a charting library (e.g. `lightweight-charts` or `recharts`), Vitest + Testing Library, ESLint + Prettier. Verify the federation plugin's current Vite support when you install it, and pin versions.

## Structure (npm workspaces)
```
frontend/
├── package.json            # workspaces: shell, market, trading, admin, shared
├── shell/                  # host app: auth, layout, routing, loads remotes
├── market/                 # remote: live prices, charts, symbol list
├── trading/                # remote: order form, orders, portfolio, alerts, reports
├── admin/                  # remote: symbols, simulation controls, users
└── shared/                 # types, API client, auth store, event bus, UI primitives
```

## Micro frontend contract
- **shell** is the only host. Remotes are loaded lazily by route (`/market/*`, `/trading/*`, `/admin/*`).
- Each remote exposes `./App` (its routes) and optionally widgets (e.g. `./PriceTicker`). Remotes also run standalone in dev on their own port.
- **Shared singletons** (`react`, `react-dom`, `react-router-dom`, `@tanstack/react-query`, `shared`) declared in federation config to avoid duplicate instances.
- Remotes **never import each other**. Cross-MFE communication goes through `shared`:
  - auth state: `useAuth()` (access token in memory, refresh through httpOnly cookie or refresh endpoint),
  - events: a typed event bus (e.g. `bus.emit('order:placed', {...})`, `bus.on(...)`),
  - realtime: a single `useNotifications()` WebSocket connection owned by the shell.
- Remotes are built and deployed independently (separate Docker images or separate static folders under nginx).

## Ports (dev)
shell 5173, market 5174, trading 5175, admin 5176.

## API access
- Generate types from each service's OpenAPI: `npx openapi-typescript <url> -o shared/src/api/<service>.ts`.
- Thin typed client in `shared/src/api` (fetch wrapper that adds the bearer token and handles 401 → refresh → retry).
- Base URL is `/api` (relative; nginx or the Vite proxy forwards it).

## Real-time notifications
```ts
const client = new Client({
  brokerURL: `${location.protocol === 'https:' ? 'wss' : 'ws'}://${location.host}/ws`,
  connectHeaders: { Authorization: `Bearer ${token}` },
  reconnectDelay: 3000, heartbeatIncoming: 10000, heartbeatOutgoing: 10000,
  onConnect: () => client.subscribe('/user/queue/notifications', m => handle(JSON.parse(m.body))),
});
client.activate();
```
Price streaming: either subscribe to `/topic/prices` (broadcast by notification-service from Kafka ticks) or poll `/api/market/prices`; prefer the topic and show a "reconnecting" indicator on drops.

## Screens
| Remote | Screens |
|---|---|
| shell | Login, Register, layout/nav, notification toasts |
| market | Symbol list with live prices, symbol detail chart |
| trading | Order form (market/limit), open orders, order history, portfolio, alerts, reports |
| admin | Symbol CRUD, simulation controls (speed, volatility, pause), user list |

## Quality
- Strict TS, ESLint, Prettier in CI. Components tested with Vitest + Testing Library; one Playwright happy path (optional).
- Handle loading, empty, error, and disconnected states on every data view.
- Never store tokens in `localStorage`; keep access token in memory.

## Build and serve
Each package has `npm run dev` and `npm run build`. Production: static assets served by nginx (shell container), remotes published at `/remotes/<name>/remoteEntry.js`.
