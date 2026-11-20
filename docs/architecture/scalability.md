# Scalability

## What scales and how

| Component | Strategy |
|---|---|
| api-gateway | 2+ stateless replicas behind nginx (least_conn); rate limit counters in Redis |
| order-service | 3 replicas; load-balanced by Compose DNS / Kubernetes Service; no local state |
| notification-service | 2+ replicas; nginx WebSocket upstream; Redis pub/sub for cross-instance delivery |
| portfolio-service / alert-service | Replicas share a Kafka consumer group (partition split) |
| matching-service | Single active instance in v1 (see below); scale-out design documented |
| report-function | OpenFaaS autoscaling (0..n) |
| Kafka | 3 partitions per topic, consumer groups |
| RabbitMQ | competing consumers on queues |

## nginx (edge load balancer)

```nginx
upstream gateway       { least_conn; server api-gateway-1:8080; server api-gateway-2:8080; }
upstream notifications { ip_hash;    server notification-service-1:8087; server notification-service-2:8087; }

server {
  listen 80;
  location /api/ { proxy_pass http://gateway; proxy_set_header X-Request-Id $request_id; }
  location /ws   {
    proxy_pass http://notifications;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
    proxy_read_timeout 1h;
  }
  location /     { proxy_pass http://shell:80; }
}
```
In Kubernetes the same role is played by an nginx Ingress plus `Service` objects.

## Scalable WebSockets with Redis
Problem: user A's WebSocket is on instance 1, but the Kafka partition holding A's trade event is consumed by instance 2.
Solution: whichever instance receives an event publishes `{userId, message}` to the Redis channel `notifications`. **Every** instance subscribes and delivers to sessions it holds locally (`convertAndSendToUser`). Instances need no knowledge of each other. The same mechanism (channel `prices`) fans live price ticks out to every instance for `/topic/prices`. This is the core of the tutorial option "scaling server-side notifications".

## matching-service scaling note
Limit-order matching needs a consistent view of the book per symbol. v1 runs one instance. Scale-out option: shard by symbol using the RabbitMQ consistent-hash exchange (`x-consistent-hash`) so each instance owns a subset of symbols, or key by symbol on Kafka partitions. Documented, optional to implement.

## Demonstrating it
- `docker compose up --scale order-service=3`
- k6 script in `scripts/load/` hitting `POST /api/orders`, observing instance distribution (log an `X-Served-By` header).
- Kubernetes HPA on order-service and notification-service (CPU 60%).
- Kill one notification-service instance during a session; the client reconnects (STOMP heartbeat + reconnect delay) and keeps receiving messages.
