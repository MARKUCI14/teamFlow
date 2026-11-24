# Notification Service

Consumes submission, issue, work-log, and report events from Kafka and delivers private updates over authenticated STOMP WebSockets. Start with one instance; nginx proxies `/ws` to it. Bind destinations to token principals; clients cannot choose recipient IDs.
