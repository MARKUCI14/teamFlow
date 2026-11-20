# REST API Reference (planned)

All paths are public through the gateway under `/api`. All require a Bearer JWT except where marked **public**. Each service publishes OpenAPI at `/v3/api-docs` (Swagger UI `/swagger-ui.html`). Keep this file in sync with the OpenAPI output.

## Auth and users (user-service)
| Method | Path | Description | Auth |
|---|---|---|---|
| POST | `/api/auth/register` | Create account (starts with virtual cash) | public |
| POST | `/api/auth/login` | Returns access + refresh token | public |
| POST | `/api/auth/refresh` | Rotate refresh token | public |
| POST | `/api/auth/logout` | Revoke refresh token | USER |
| GET | `/api/users/me` | Current profile | USER |
| GET | `/api/admin/users` | List users | ADMIN |
| GET | `/.well-known/jwks.json` | Public keys | public |

## Market (market-data-service)
| Method | Path | Description | Auth |
|---|---|---|---|
| GET | `/api/market/symbols` | List active symbols | USER |
| GET | `/api/market/prices` | Latest price per symbol | USER |
| GET | `/api/market/symbols/{ticker}/history?from&to&interval` | Historical candles | USER |
| POST/PUT/DELETE | `/api/admin/market/symbols[/{ticker}]` | Manage symbols | ADMIN |
| PUT | `/api/admin/market/simulation` | `{tickIntervalMs, volatility, paused}` | ADMIN |

## Orders (order-service)
| Method | Path | Description | Auth |
|---|---|---|---|
| POST | `/api/orders` | Place order `{symbol, side, type, quantity, limitPrice?}` → 202 `{orderId, status}` | USER |
| GET | `/api/orders` | My orders (filter by status, paginated) | USER |
| GET | `/api/orders/{id}` | One order | USER (owner) |
| DELETE | `/api/orders/{id}` | Cancel order | USER (owner) |

## Portfolio (portfolio-service)
| Method | Path | Description | Auth |
|---|---|---|---|
| GET | `/api/portfolio` | Cash, holdings, total value, P&L | USER |
| GET | `/api/portfolio/trades` | My executed trades | USER |
| POST | `/api/portfolio/reports` | Request PDF report → 202 `{reportId}` | USER |
| GET | `/api/portfolio/reports` | List my reports | USER |
| GET | `/api/portfolio/reports/{id}/download` | Download PDF | USER (owner) |
| POST | `/internal/reservations` | Reserve funds/shares for an order | internal token |
| DELETE | `/internal/reservations/{orderId}` | Release reservation | internal token |

## Alerts (alert-service)
| Method | Path | Description | Auth |
|---|---|---|---|
| POST | `/api/alerts` | `{symbol, direction: ABOVE or BELOW, threshold}` | USER |
| GET | `/api/alerts` | My alerts | USER |
| DELETE | `/api/alerts/{id}` | Delete alert | USER (owner) |

## WebSocket (notification-service)
| Endpoint | Direction | Description |
|---|---|---|
| `/ws` (STOMP) | connect | JWT in `Authorization` header of CONNECT frame |
| `/user/queue/notifications` | server → client | Per-user notifications `{category, title, body, data, ts}` |
| `/topic/prices` | server → client | Price ticks `{symbol, price, ts}` |

## Error format (RFC 7807)
```json
{ "type": "about:blank", "title": "Insufficient funds", "status": 409,
  "detail": "Required 2003.50, available 1500.00", "instance": "/api/orders" }
```
