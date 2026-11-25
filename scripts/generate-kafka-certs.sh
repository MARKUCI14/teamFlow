#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

if [[ ! -f .env ]]; then
  echo "Missing .env. Copy .env.example to .env first." >&2
  exit 1
fi

tls_password="${KAFKA_TLS_PASSWORD:-}"
if [[ -z "$tls_password" ]]; then
  tls_password="$(awk -F= '$1 == "KAFKA_TLS_PASSWORD" { sub(/^[^=]*=/, ""); print; exit }' .env)"
fi
if [[ -z "$tls_password" ]]; then
  echo "KAFKA_TLS_PASSWORD must be set in .env or the environment." >&2
  exit 1
fi
if ! command -v keytool >/dev/null 2>&1; then
  echo "keytool is required; install/use the project's Java 21 JDK." >&2
  exit 1
fi

cert_dir="$repo_root/.local/kafka-certs"
if [[ -f "$cert_dir/kafka.keystore.jks" && -f "$cert_dir/kafka.truststore.jks" ]]; then
  echo "Kafka TLS files already exist in .local/kafka-certs. Remove that folder to regenerate them."
  exit 0
fi

mkdir -p "$cert_dir"
chmod 755 "$cert_dir"

# This local self-signed certificate covers both Compose and host-side Kafka addresses.
keytool -genkeypair \
  -alias teamflow-kafka \
  -keyalg RSA \
  -keysize 2048 \
  -validity 365 \
  -storetype JKS \
  -keystore "$cert_dir/kafka.keystore.jks" \
  -storepass "$tls_password" \
  -keypass "$tls_password" \
  -dname "CN=kafka" \
  -ext "SAN=dns:kafka,dns:localhost,ip:127.0.0.1" \
  -noprompt

keytool -exportcert \
  -alias teamflow-kafka \
  -keystore "$cert_dir/kafka.keystore.jks" \
  -storepass "$tls_password" \
  -file "$cert_dir/kafka.crt"

keytool -importcert \
  -alias teamflow-kafka \
  -file "$cert_dir/kafka.crt" \
  -keystore "$cert_dir/kafka.truststore.jks" \
  -storetype JKS \
  -storepass "$tls_password" \
  -noprompt

for credential_file in kafka_keystore_creds kafka_ssl_key_creds kafka_truststore_creds; do
  printf '%s' "$tls_password" > "$cert_dir/$credential_file"
done

# The official Kafka image runs as UID 1000 and must read these local development files.
# They contain only self-signed, local Compose credentials; do not reuse them elsewhere.
chmod 644 "$cert_dir"/*

echo "Generated local Kafka TLS files in ignored .local/kafka-certs."
