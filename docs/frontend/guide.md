# Frontend Guide

Planned frontend: React + strict TypeScript, Vite, Module Federation, and a shared contract package.

```text
frontend/
  shell/       authentication, navigation, route composition, notifications
  projects/    project list, detail, membership and activity
  stakeholder/ submission form and submission status
  developer/   assigned issues, issue updates, comments, work logs
  admin/       users, roles, project administration
  shared/      auth, API client, event types, UI primitives
```

The shell owns one authenticated STOMP connection and role-aware navigation. Remotes use shared API/auth contracts and never import one another. Backend authorization is authoritative; hiding a route is not security. Provide loading, empty, error, and disconnected states. See [API](../api/rest-api.md) and [C4](../architecture/c4-model.md).
