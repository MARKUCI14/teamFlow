# Report Service

## Purpose

Owns report request handling, job status, and result metadata. It accepts report requests from PMs or admins, checks project membership and prepares a minimal snapshot through work-management-service, then coordinates PDF generation.

## Communication

The report-service sends the `reports.generate` command through RabbitMQ and invokes the stateless [report function](report-function.md) with the authorized snapshot. After completion, it records the result and publishes a versioned report lifecycle event to Kafka.

## Data ownership

Report job and result metadata belong to this service. The report function has no user database credentials and does not persist job state.
