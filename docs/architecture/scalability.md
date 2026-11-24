# Scalability

## Assessment implementation

The assessment awards one scalability point for load balancers **or** scalable WebSockets. TeamFlow uses nginx to load-balance two stateless API-gateway replicas. This is the planned scalability demonstration; the notification-service starts as one instance.

nginx also proxies the authenticated `/ws` route to notification-service. Kafka and RabbitMQ remain part of their separate assessment items, not additional scaling requirements. Keep the gateway stateless so either replica can handle a request.
