# UML and Workflow Diagrams

## Stakeholder submission to developer issue

```mermaid
sequenceDiagram
  actor S as Stakeholder
  actor PM as Project Manager
  actor D as Developer
  participant G as API Gateway
  participant WM as Work Management Service
  participant K as Kafka
  participant N as Notification Service
  S->>G: POST /api/projects/{id}/submissions (JWT; project Stakeholder)
  G->>WM: validate membership and create submission
  WM-->>S: 201 Created
  WM->>K: submissions.events: SubmissionCreated
  K->>N: event
  N-->>PM: private submission notification
  PM->>G: PATCH /api/submissions/{id} (project Project Manager; triage)
  G->>WM: authorize and update triage
  PM->>G: POST /api/submissions/{id}/issues (project Project Manager)
  G->>WM: validate membership; create and assign linked issue
  WM->>K: issues.events: IssueCreated
  K->>N: event
  N-->>D: private assignment notification
```

## Developer work log

```mermaid
sequenceDiagram
  actor D as Developer
  participant G as Gateway
  participant WM as Work Management Service
  participant K as Kafka
  participant N as Notification Service
  D->>G: PATCH /api/issues/{id} status
  G->>WM: authorize assignment and update
  WM->>K: IssueStatusChanged
  D->>G: POST /api/issues/{id}/worklogs
  G->>WM: create duration entry
  WM->>K: WorklogCreated
  K->>N: relevant project event
  N-->>D: live update
  N-->>PM: live update
```

## Issue entity

```mermaid
classDiagram
  class Project { +UUID id +String name }
  class Membership { +UUID projectId +UUID userId +ProjectRole role }
  class Submission { +UUID id +UUID projectId +UUID authorId +SubmissionType type +SubmissionStatus status }
  class Issue { +UUID id +UUID projectId +UUID submissionId +UUID assigneeId +IssueStatus status +Priority priority }
  class Comment { +UUID id +UUID issueId +UUID authorId +String body }
  class WorkLog { +UUID id +UUID issueId +UUID userId +int minutes +LocalDate workDate }
  Project "1" --> "*" Membership
  Project "1" --> "*" Submission
  Submission "0..1" --> "*" Issue : source
  Project "1" --> "*" Issue
  Issue "1" --> "*" Comment
  Issue "1" --> "*" WorkLog
```
