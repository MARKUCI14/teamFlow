# Report Service

## Purpose

Owns report request handling, job status, and result metadata. It accepts report requests from a project manager or admin, then asks work-management-service to check project membership and prepare a minimal snapshot before coordinating PDF generation.

## Communication

The report-service sends the `reports.generate` command through RabbitMQ and invokes the stateless [report function](report-function.md) with the authorized snapshot. After completion, it records the result and publishes a versioned report lifecycle event to Kafka.

## Data ownership

Report job and result metadata belong to this service. The report function has no user database credentials and does not persist job state.
