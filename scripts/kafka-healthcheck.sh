#!/usr/bin/env bash
set -euo pipefail

properties_file="$(mktemp)"
trap 'rm -f "$properties_file"' EXIT

cat > "$properties_file" <<EOF
security.protocol=SASL_SSL
sasl.mechanism=PLAIN
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="${KAFKA_USERNAME}" password="${KAFKA_PASSWORD}";
ssl.truststore.location=/etc/kafka/secrets/kafka.truststore.jks
ssl.truststore.password=${KAFKA_TLS_PASSWORD}
EOF

/opt/kafka/bin/kafka-broker-api-versions.sh \
  --bootstrap-server localhost:9092 \
  --command-config "$properties_file" >/dev/null
