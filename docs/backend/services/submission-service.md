# Submission Service

Owns stakeholder suggestions and bug reports, their author, project, description, triage status, and manager response. Emits `SubmissionCreated` and triage events to Kafka. A project manager can accept, reject, request clarification, or create linked issues through issue-service. Submission visibility follows project policy and author ownership.
