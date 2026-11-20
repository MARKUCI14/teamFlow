# ADR-0002: RabbitMQ for commands, Kafka for events

- Status: accepted

## Context
The rubric asks for both a message broker and event streaming. They should each have a natural role rather than duplicate each other.

## Decision
Commands that exactly one service must act on (place/cancel order, send notification) go through RabbitMQ. Facts that many services react to independently (price ticks, executed trades, rejections) go through Kafka.

## Consequences
+ Clear mental model, good tutorial material (compare retries/DLQ vs offsets/consumer groups).
- Two brokers to operate; more containers locally. Mitigated with Compose profiles.
