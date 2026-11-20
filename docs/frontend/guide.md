# Frontend Implementation Guide

This guide describes planned frontend packages and what each important file owns. The frontend is a single npm-workspace repository containing one shell, three independently built remotes, and a shared contract package. Files listed below are to be created as frontend milestones are implemented.

## Workspace layout

```text
frontend/
├── package.json                    # Workspace root commands and common tooling.
├── package-lock.json               # Reproducible dependency lock.
├── tsconfig.base.json              # Shared strict TypeScript settings.
├── eslint.config.js                # Shared lint rules.
├── prettier.config.js               # Shared formatter rules.
├── shell/                           # Host: auth, navigation, routing, notifications.
├── market/                          # Remote: market overview and price charts.
├── trading/                         # Remote: orders, portfolio, alerts, reports.
├── admin/                           # Remote: symbol and market-mode administration.
└── shared/                          # API, auth, domain types, events, UI primitives.
```

## Root-level files

| File | Responsibility |
|---|---|
| `package.json` | Declares npm workspaces and aggregate `dev`, `build`, `lint`, `typecheck`, and test scripts. |
| `package-lock.json` | Pins the exact dependency graph; update with npm, not manual edits. |
| `tsconfig.base.json` | Enables strict typing, path aliases, and shared compiler targets. |
| `eslint.config.js` | Enforces React hooks, accessibility, import, and TypeScript rules across packages. |
| `prettier.config.js` | Defines consistent formatting. |
| `.env.example` | Documents non-secret frontend build/runtime variables; never stores tokens or keys. |

## Package file conventions

Each app has a `package.json` for its own dependencies/scripts, `index.html` as the Vite document, `vite.config.ts` for dev server/federation settings, `tsconfig.json` extending the shared config, and `src/` for application code. App entry files mount React; `App.tsx` provides top-level providers and routes; `src/styles/` contains app-level styles; `src/test/` contains setup and test helpers.

### Shell (`frontend/shell/`)

| File or directory | Responsibility |
|---|---|
| `vite.config.ts` | Host federation config, remote URLs, dev port 5173, and API/WebSocket proxy in development. |
| `src/main.tsx` | Creates the root DOM mount and renders the shell. |
| `src/App.tsx` | Composes auth, query, router, and notification providers. |
| `src/router.tsx` | Declares public login/register routes and lazy routes for market/trading/admin remotes. |
| `src/layout/AppLayout.tsx` | Shared authenticated page frame, navigation, account menu, and responsive layout. |
| `src/pages/LoginPage.tsx` | Login form and validation; delegates requests to shared auth API. |
| `src/pages/RegisterPage.tsx` | Registration form and validation. |
| `src/components/RemoteErrorBoundary.tsx` | Displays a useful fallback when a remote fails to load. |
| `src/realtime/NotificationConnection.tsx` | Owns the single STOMP connection and routes incoming toasts/events to shared state. |
| `src/components/ToastRegion.tsx` | Accessible display of notification messages. |
| `src/styles/` | Shell theme, layout, and global resets. |
| `src/test/` | Shell route/auth/remote error boundary tests. |

### Shared package (`frontend/shared/`)

| File or directory | Responsibility |
|---|---|
| `src/api/httpClient.ts` | Fetch wrapper for `/api`, bearer token attachment, structured errors, and refresh/retry behavior. |
| `src/api/generated/` | Generated TypeScript endpoint types from service OpenAPI; do not edit by hand. |
| `src/api/authApi.ts` | Registration, login, refresh, logout, and current-user calls. |
| `src/api/marketApi.ts` | Typed symbols, quote, history, and provider-mode calls. |
| `src/api/orderApi.ts` | Typed place/list/cancel order calls. |
| `src/api/portfolioApi.ts` | Portfolio, trade history, and report status/download calls. |
| `src/api/alertApi.ts` | Alert CRUD calls. |
| `src/auth/AuthProvider.tsx` | Holds access token and user state in memory; coordinates refresh and logout. |
| `src/auth/useAuth.ts` | Hook exposing auth state and actions to apps. |
| `src/realtime/NotificationProvider.tsx` | Shared connection state and notification/event subscription API. |
| `src/realtime/stompClient.ts` | Builds/configures STOMP client, auth CONNECT headers, reconnect, heartbeat, and cleanup. |
| `src/events/eventBus.ts` | Typed in-browser event contracts for loosely coupled remotes. |
| `src/types/` | Shared UI-facing types not generated from OpenAPI (notification, chart view model). |
| `src/components/` | Small accessible primitives shared by apps, not domain-specific screens. |
| `src/test/` | Mock server, auth/realtime test helpers, and shared package tests. |

Do not persist access tokens in localStorage. Remotes must not import one another; cross-remote coordination uses the shell or documented shared contracts.

### Market remote (`frontend/market/`)

| File or directory | Responsibility |
|---|---|
| `vite.config.ts` | Exposes `./App` through Module Federation; dev port 5174. |
| `src/remote.tsx` | Standalone dev mount for local work without the shell. |
| `src/App.tsx` | Market remote route composition. |
| `src/routes/MarketRoutes.tsx` | Symbol list and symbol-detail route definitions. |
| `src/pages/MarketOverviewPage.tsx` | Searchable symbol list with current prices and source/mode status. |
| `src/pages/SymbolDetailPage.tsx` | Historical chart and current quote for a selected symbol. |
| `src/components/PriceTable.tsx` | Sortable price rows with loading/empty/error states. |
| `src/components/PriceChart.tsx` | Historical chart with time range and clear stale/disconnected state. |
| `src/hooks/usePriceStream.ts` | Consumes shared price updates and merges them with fetched quotes. |
| `src/test/` | Price rendering, reconnection, and empty/error state tests. |

### Trading remote (`frontend/trading/`)

| File or directory | Responsibility |
|---|---|
| `vite.config.ts` | Exposes `./App`; dev port 5175. |
| `src/remote.tsx` | Standalone dev mount. |
| `src/App.tsx` | Trading route composition. |
| `src/routes/TradingRoutes.tsx` | Order, portfolio, alerts, and reports routes. |
| `src/pages/OrderEntryPage.tsx` | Validated market/limit buy/sell form and async acceptance feedback. |
| `src/pages/OrdersPage.tsx` | Open/history orders and cancellation actions. |
| `src/pages/PortfolioPage.tsx` | Cash, holdings, valuation, and P&L views. |
| `src/pages/AlertsPage.tsx` | Create/list/delete the current user's price alerts. |
| `src/pages/ReportsPage.tsx` | Request report, show pending/ready/failed status, and download ready PDF. |
| `src/components/OrderStatusBadge.tsx` | Consistent status labels and accessible visual states. |
| `src/hooks/` | Query/mutation hooks connecting pages to typed shared API calls. |
| `src/test/` | Order validation, owner-facing UI states, and report lifecycle tests. |

### Admin remote (`frontend/admin/`)

| File or directory | Responsibility |
|---|---|
| `vite.config.ts` | Exposes `./App`; dev port 5176. |
| `src/remote.tsx` | Standalone dev mount. |
| `src/App.tsx` | Admin route composition and role guard. |
| `src/routes/AdminRoutes.tsx` | Admin symbols, provider, simulation, and user routes. |
| `src/pages/SymbolManagementPage.tsx` | Create/edit/deactivate symbols. |
| `src/pages/MarketModePage.tsx` | Select simulated or real provider and show configured provider health/staleness without revealing secrets. |
| `src/pages/SimulationSettingsPage.tsx` | Change simulator tick interval, volatility, and pause state. |
| `src/pages/UserManagementPage.tsx` | Admin user listing, if included in the backend API scope. |
| `src/components/AdminOnly.tsx` | Blocks or hides admin UI for non-admin accounts; server authorization remains authoritative. |
| `src/test/` | Role guard and provider/simulation form tests. |

## Runtime and package contracts

- The shell lazily loads remote `./App` modules by route; every remote also runs standalone in development.
- Configure React, React DOM, router, query client, and shared package as compatible federation singletons.
- Use shared OpenAPI-generated types and API wrappers rather than duplicating endpoint contracts in remotes.
- Keep backend URLs relative (`/api`) in production; nginx routes `/api`, `/ws`, static shell, and remote assets.
- Display loading, empty, error, and disconnected/stale states on data-driven views.
- Use semantic HTML, keyboard-accessible controls, labels, and visible focus.

## Development commands

Run `npm run dev --workspace=shell` (or the corresponding workspace) for one app; root scripts can run all workspaces. Build each app independently to verify its remote artifact. See [getting started](../development/getting-started.md) and [deployment](../deployment/compose.md).
