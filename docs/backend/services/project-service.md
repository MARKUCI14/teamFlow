# Project Service

Owns projects and their memberships. Exposes project CRUD, membership administration, and project-scoped authorization lookups. Roles on a project are project manager, stakeholder, or developer; system admin remains global. Other services call its internal membership contract and never query its tables.
