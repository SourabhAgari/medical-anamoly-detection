#!/bin/bash
# =============================================================================
# Kafka Cluster Health Verification Script
# Run after docker compose up to validate the full stack
# =============================================================================

set -euo pipefail

# When run inside the kafka-net Docker network use internal hostnames;
# when run from the host use localhost ports.
if getent hosts broker-1 &>/dev/null; then
  BOOTSTRAP="broker-1:29092,broker-2:29092,broker-3:29092"
  SCHEMA_REGISTRY="http://schema-registry:8081"
  KAFKA_UI="http://kafka-ui:8080"
else
  BOOTSTRAP="localhost:9092"
  SCHEMA_REGISTRY="http://localhost:8081"
  KAFKA_UI="http://localhost:8080"
fi

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

pass() { echo -e "${GREEN}[PASS]${NC} $1"; }
fail() { echo -e "${RED}[FAIL]${NC} $1"; FAILED=1; }
info() { echo -e "${YELLOW}[INFO]${NC} $1"; }

FAILED=0

echo "============================================================"
echo "  Medical Kafka Cluster — Health Check"
echo "============================================================"

# ── 1. Broker connectivity ────────────────────────────────────────────────
info "Checking broker connectivity..."
if getent hosts broker-1 &>/dev/null; then
  BROKER_ADDRS=("broker-1:29092" "broker-2:29092" "broker-3:29092")
else
  BROKER_ADDRS=("localhost:9092" "localhost:9093" "localhost:9094")
fi
for addr in "${BROKER_ADDRS[@]}"; do
  if kafka-broker-api-versions --bootstrap-server "$addr" &>/dev/null 2>&1; then
    pass "Broker $addr is reachable"
  else
    fail "Broker $addr is NOT reachable"
  fi
done

# ── 2. Cluster metadata ───────────────────────────────────────────────────
info "Checking cluster metadata (expecting 3 brokers)..."
BROKER_COUNT=$(kafka-broker-api-versions --bootstrap-server "$BOOTSTRAP" 2>/dev/null | grep -c "^broker" || true)
if [ "$BROKER_COUNT" -eq 3 ]; then
  pass "Cluster has 3 brokers"
else
  fail "Cluster broker count mismatch: found $BROKER_COUNT"
fi

# ── 3. Topics ─────────────────────────────────────────────────────────────
info "Checking required topics..."
REQUIRED_TOPICS=(
  "medical.vitals.raw"
  "medical.vitals.normalised"
  "medical.anomaly.detected"
  "medical.alert.triggered"
  "medical.vitals.dlq"
  "medical.audit.events"
  "medical.patient.state"
)

for topic in "${REQUIRED_TOPICS[@]}"; do
  if kafka-topics --bootstrap-server "$BOOTSTRAP" --describe --topic "$topic" &>/dev/null 2>&1; then
    pass "Topic exists: $topic"
  else
    fail "Topic MISSING: $topic"
  fi
done

# ── 4. Replication factor verification ────────────────────────────────────
info "Verifying replication factor on medical.vitals.raw..."
RF=$(kafka-topics --bootstrap-server "$BOOTSTRAP" --describe --topic "medical.vitals.raw" 2>/dev/null \
  | grep "ReplicationFactor" | awk -F'ReplicationFactor: ' '{print $2}' | awk '{print $1}')
if [ "$RF" = "3" ]; then
  pass "medical.vitals.raw has RF=3"
else
  fail "medical.vitals.raw RF mismatch: got $RF (expected 3)"
fi

# ── 5. Under-replicated partitions ────────────────────────────────────────
info "Checking for under-replicated partitions..."
URP=$(kafka-topics --bootstrap-server "$BOOTSTRAP" --describe --under-replicated-partitions 2>/dev/null | wc -l | tr -d ' ')
if [ "$URP" -eq 0 ]; then
  pass "No under-replicated partitions"
else
  fail "$URP under-replicated partition(s) found"
fi

# ── 6. Schema Registry ───────────────────────────────────────────────────
info "Checking Schema Registry..."
SR_STATUS=$(curl -s "$SCHEMA_REGISTRY/subjects" -o /dev/null -w "%{http_code}" 2>/dev/null)
if [ "$SR_STATUS" = "200" ]; then
  pass "Schema Registry is up ($SCHEMA_REGISTRY)"
else
  fail "Schema Registry not reachable — HTTP $SR_STATUS"
fi

# ── 7. Kafka UI ───────────────────────────────────────────────────────────
info "Checking Kafka UI..."
UI_STATUS=$(curl -s "$KAFKA_UI/admin/health" -o /dev/null -w "%{http_code}" 2>/dev/null)
if [ "$UI_STATUS" = "200" ]; then
  pass "Kafka UI is up ($KAFKA_UI)"
else
  fail "Kafka UI not reachable — HTTP $UI_STATUS"
fi

# ── 8. Produce + Consume round-trip test ─────────────────────────────────
# Strategy: start consumer listening at latest offset BEFORE producing,
# then produce, then wait for consumer to capture the fresh message.
info "Running produce/consume round-trip test on medical.vitals.raw..."
TEST_MSG="health-check-$(date +%s)"
CONSUMED_FILE=$(mktemp)

# Start consumer in background. Fresh group + no --from-beginning =>
# auto.offset.reset=latest, so it only reads messages produced after it joins.
kafka-console-consumer \
  --bootstrap-server "$BOOTSTRAP" \
  --topic "medical.vitals.raw" \
  --group "health-check-$$" \
  --consumer-property auto.offset.reset=latest \
  --max-messages 1 \
  --timeout-ms 20000 > "$CONSUMED_FILE" 2>/dev/null &
CONSUMER_PID=$!

sleep 5  # let consumer join all partitions and commit initial offsets

echo "$TEST_MSG" | kafka-console-producer \
  --bootstrap-server "$BOOTSTRAP" \
  --topic "medical.vitals.raw" \
  --producer-property acks=all \
  --producer-property enable.idempotence=true \
  &>/dev/null

wait $CONSUMER_PID || true
CONSUMED=$(tr -d '\n' < "$CONSUMED_FILE")
rm -f "$CONSUMED_FILE"

if [ "$CONSUMED" = "$TEST_MSG" ]; then
  pass "Produce/consume round-trip OK"
else
  fail "Round-trip FAILED — consumed: '$CONSUMED' expected: '$TEST_MSG'"
fi

# ── Summary ───────────────────────────────────────────────────────────────
echo ""
echo "============================================================"
if [ "$FAILED" -eq 0 ]; then
  echo -e "${GREEN}All checks passed. Cluster is healthy.${NC}"
else
  echo -e "${RED}One or more checks failed. Review output above.${NC}"
  exit 1
fi
echo "============================================================"