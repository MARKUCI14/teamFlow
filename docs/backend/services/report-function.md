# Report Function

Stateless Spring Cloud Function that accepts an authorized project report snapshot and returns a PDF summary of issue activity and logged time. report-service owns request status and coordinates generation; work-management-service checks project membership and prepares the scoped snapshot. Run as a local container in Compose and as an OpenFaaS function in Kubernetes.
