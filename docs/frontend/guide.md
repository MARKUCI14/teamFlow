# Frontend Guide

Planned frontend: React + strict TypeScript, Vite, Module Federation, and a small shared contract package. The shell composes two remotes.

```text
frontend/
  shell/       authentication, navigation, route composition, notifications
  stakeholder/ submission form and submission status
  team/        PM project/submission management; developer issues and work logs; admin users and roles
  shared/      auth, API client, event types, UI primitives
```

The shell owns one authenticated STOMP connection and role-aware navigation. The two remotes share API/auth contracts through `shared` and never import one another. Backend authorization is authoritative; hiding a route is not security. Provide loading, empty, error, and disconnected states. See [API](../api/rest-api.md) and [C4](../architecture/c4-model.md).
