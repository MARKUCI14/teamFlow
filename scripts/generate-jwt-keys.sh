#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
key_dir="$repo_root/.local/jwt-keys"
private_key="$key_dir/private.pem"
public_key="$key_dir/public.pem"

if ! command -v openssl >/dev/null 2>&1; then
  echo "openssl is required to generate local JWT signing keys." >&2
  exit 1
fi

if [[ -f "$private_key" && -f "$public_key" ]]; then
  echo "Local JWT keys already exist in .local/jwt-keys. Remove that folder to regenerate them."
  exit 0
fi

if [[ -e "$private_key" || -e "$public_key" ]]; then
  echo "Only one JWT key file exists in .local/jwt-keys; remove the folder before regenerating both." >&2
  exit 1
fi

umask 077
mkdir -p "$key_dir"
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out "$private_key"
openssl pkey -in "$private_key" -pubout -out "$public_key"
chmod 600 "$private_key"
chmod 644 "$public_key"

echo "Generated local JWT keys in ignored .local/jwt-keys. Use these keys for local development only."
