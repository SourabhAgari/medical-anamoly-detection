#!/bin/bash
# =============================================================================
# Schema Registry — Avro Schema Registration Script
# Idempotent: safe to run multiple times; skips already-registered schemas.
# Requires: curl, jq
# =============================================================================

set -euo pipefail

# ── Auto-detect Schema Registry URL ─────────────────────────────────────────
if getent hosts schema-registry &>/dev/null; then
  SCHEMA_REGISTRY="http://schema-registry:8081"
  AVRO_BASE="/avro"      # mounted path when run inside Docker
else
  SCHEMA_REGISTRY="http://localhost:8081"
  # Resolve relative to this script's location
  SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
  AVRO_BASE="$(cd "$SCRIPT_DIR/../../../med-flink/src/main/avro" && pwd)"
fi

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; NC='\033[0m'
pass() { echo -e "${GREEN}[PASS]${NC} $1"; }
fail() { echo -e "${RED}[FAIL]${NC} $1"; exit 1; }
info() { echo -e "${YELLOW}[INFO]${NC} $1"; }

# ── Wait for Schema Registry ─────────────────────────────────────────────────
wait_for_registry() {
  info "Waiting for Schema Registry at $SCHEMA_REGISTRY ..."
  for i in $(seq 1 30); do
    STATUS=$(curl -s "$SCHEMA_REGISTRY/subjects" -o /dev/null -w "%{http_code}" 2>/dev/null)
    [ "$STATUS" = "200" ] && { info "Schema Registry is ready."; return 0; }
    echo "  Attempt $i/30 — HTTP $STATUS — retrying in 5s..."
    sleep 5
  done
  fail "Schema Registry did not become ready in time."
}

# ── Register one schema ───────────────────────────────────────────────────────
# Usage: register_schema <subject> <path-to-.avsc-file>
register_schema() {
  local subject="$1"
  local avsc_file="$2"

  if [ ! -f "$avsc_file" ]; then
    fail "Schema file not found: $avsc_file"
  fi

  # Check if schema is already registered (idempotent)
  EXISTING=$(curl -s "$SCHEMA_REGISTRY/subjects/$subject/versions" -o /dev/null -w "%{http_code}")
  if [ "$EXISTING" = "200" ]; then
    info "Subject '$subject' already has versions — checking compatibility..."
    # POST to /compatibility to validate before registering
    COMPAT=$(curl -s -X POST \
      -H "Content-Type: application/vnd.schemaregistry.v1+json" \
      --data "$(jq -Rs '{"schema": .}' < "$avsc_file")" \
      "$SCHEMA_REGISTRY/compatibility/subjects/$subject/versions/latest" | jq -r '.is_compatible // false')
    if [ "$COMPAT" != "true" ]; then
      fail "Schema in $avsc_file is NOT compatible with registered subject '$subject'. Aborting."
    fi
    info "Compatible. Registering new version for '$subject'..."
  else
    info "Registering new subject '$subject'..."
  fi

  # Register / evolve the schema
  RESPONSE=$(curl -s -X POST \
    -H "Content-Type: application/vnd.schemaregistry.v1+json" \
    --data "$(jq -Rs '{"schema": .}' < "$avsc_file")" \
    "$SCHEMA_REGISTRY/subjects/$subject/versions")

  SCHEMA_ID=$(echo "$RESPONSE" | jq -r '.id // empty')
  if [ -n "$SCHEMA_ID" ]; then
    pass "Registered '$subject' — schema ID: $SCHEMA_ID"
  else
    fail "Registration failed for '$subject'. Response: $RESPONSE"
  fi
}

# ── Main ─────────────────────────────────────────────────────────────────────
echo "============================================================"
echo "  Medical Anomaly Detection — Schema Registration"
echo "  Registry : $SCHEMA_REGISTRY"
echo "  Avro dir : $AVRO_BASE"
echo "============================================================"

wait_for_registry

# Register using Confluent TopicNameStrategy: <topic>-value
register_schema "medical.vitals.raw-value"        "$AVRO_BASE/PatientVitals.avsc"
register_schema "medical.alert.triggered-value"   "$AVRO_BASE/AlertEvent.avsc"

echo ""
info "Registered subjects:"
curl -s "$SCHEMA_REGISTRY/subjects" | jq -r '.[]' | sort

echo ""
echo "============================================================"
echo -e "${GREEN}Schema registration complete.${NC}"
echo "============================================================"
