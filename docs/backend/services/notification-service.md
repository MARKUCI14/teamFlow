# Notification Service

Consumes project, submission, issue, work-log, and report events and delivers private updates over authenticated STOMP WebSockets. Redis pub/sub fans events across replicas so a user's connected instance receives events consumed elsewhere. Bind destinations to token principals; clients cannot choose recipient IDs.
