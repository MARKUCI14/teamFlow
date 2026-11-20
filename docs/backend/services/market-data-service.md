# Market Data Service Guide

## Purpose
Owns tradable symbols and the source of current/historical market prices. It supports a simulated provider for deterministic, offline demonstrations and a real-data adapter behind the same normalized provider contract. All downstream consumers receive the same `PriceTick` schema regardless of source.

## Planned module files

| Path (under `backend/market-data-service/`) | Responsibility |
|---|---|
| `build.gradle.kts` | MVC, JPA, PostgreSQL/Flyway, Kafka producer, Redis, validation, Actuator, tests. |
| `MarketDataApplication.java` | Spring Boot entry point and scheduled-task support. |
| `api/MarketController.java` | Public symbol, latest price, and historical price queries. |
| `api/AdminMarketController.java` | Admin symbol and provider/simulation controls. |
| `api/dto/` | Symbol, quote, candle, provider-selection, and simulation DTOs. |
| `application/MarketQueryService.java` | Returns normalized prices/history from owned persistence and cache. |
| `application/MarketTickOrchestrator.java` | Schedules provider reads/generation, normalizes updates, and coordinates cache/publish operations. |
| `application/SymbolService.java` | Symbol CRUD and active-symbol rules. |
| `domain/MarketDataProvider.java` | Provider port returning normalized quotes/ticks. |
| `domain/SimulatedMarketDataProvider.java` | Generates prices via the configured random-walk model. |
| `domain/RealMarketDataProvider.java` | Adapter-facing port implementation for the selected external provider. |
| `domain/PriceTick.java` | Internal normalized price, symbol, volume, and timestamp value. |
| `infrastructure/provider/<Vendor>MarketDataClient.java` | HTTP client for the eventually selected provider; isolates vendor response formats. |
| `infrastructure/provider/ProviderResponseMapper.java` | Maps vendor responses and errors into internal types. |
| `infrastructure/persistence/` | Symbol metadata and optional historical snapshots; owns `marketdb`. |
| `infrastructure/cache/LatestPriceCache.java` | Stores and reads normalized latest values using Redis keys `price:<symbol>`. |
| `infrastructure/messaging/PriceTickPublisher.java` | Publishes versioned `market.ticks` events keyed by symbol. |
| `config/ProviderConfig.java` | Selects simulated or real adapter and configures provider limits/timeouts. |
| `config/SecurityConfig.java` | User read access and ADMIN-only controls. |
| `resources/application.yml` | Provider mode, secrets placeholders, tick schedule, Kafka, Redis, DB, and actuator settings. |
| `resources/db/migration/V1__symbols.sql` | Creates symbol metadata tables. |
| `src/test/...` | Simulator, mapping, provider selection, cache, API authorization, and Kafka contract tests. |
| `Dockerfile` | Non-root runtime image. |

## Provider rules
Provider credentials are environment/secrets only. Normalize timestamps to UTC and prices to the same numeric representation. Respect the selected provider's licensing, attribution, caching, and rate limits. When the external API is unavailable, expose stale-data status and allow the admin/user to switch back to simulation; never silently claim simulated values are real.

See also: [market API](../../api/rest-api.md), [messaging](../../architecture/messaging.md), [roadmap](../../project/roadmap.md).
