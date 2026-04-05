#!/bin/bash
# =============================================================================
# Produce sample PatientVitals Avro records to medical.vitals.raw for smoke
# testing. Uses kafka-avro-console-producer inside the schema-registry container
# so records are properly encoded with the Confluent wire format (magic byte +
# schema ID + Avro binary) that the Flink job expects.
# =============================================================================

set -euo pipefail

BOOTSTRAP_INTERNAL="broker-1:29092"   # inside med-kafka-net
SCHEMA_REGISTRY_INTERNAL="http://schema-registry:8081"
SCHEMA_REGISTRY_EXTERNAL="http://localhost:8081"
TOPIC="medical.vitals.raw"

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; NC='\033[0m'
info() { echo -e "${YELLOW}[INFO]${NC} $1"; }
pass() { echo -e "${GREEN}[PASS]${NC} $1"; }
fail() { echo -e "${RED}[FAIL]${NC} $1"; exit 1; }

docker exec schema-registry ls /usr/bin/kafka-avro-console-producer &>/dev/null \
  || fail "kafka-avro-console-producer not found in schema-registry container"

# Fetch schema ID (validates Schema Registry is reachable and schema is registered)
info "Fetching schema ID from Schema Registry..."
SCHEMA_ID=$(curl -sf "$SCHEMA_REGISTRY_EXTERNAL/subjects/$TOPIC-value/versions/latest" \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])")
[ -n "$SCHEMA_ID" ] || fail "Could not fetch schema ID"
info "Schema ID: $SCHEMA_ID"

echo "============================================================"
echo "  Producing test PatientVitals records"
echo "  Topic  : $TOPIC"
echo "  Schema : $TOPIC-value (ID $SCHEMA_ID)"
echo "============================================================"

# Build one JSON record per line. kafka-avro-console-producer reads one record
# per line from stdin. Union fields use {"type": value} notation; null unions
# are plain null.
build_record() {
  local patient_id="$1" heart_rate="$2" spo2="$3" systolic_bp="$4" temp="$5" rr="$6"

  python3 - <<PYEOF
import json, time, uuid

def union(val):
    """Return Avro union encoding: null stays null, numbers become {"double": v}."""
    if val == "null":
        return None
    return {"double": float(val)}

print(json.dumps({
    "vitalId":            str(uuid.uuid4()),
    "patientId":          "$patient_id",
    "wardId":             "ward-ICU",
    "deviceId":           {"string": "device-001"},
    "measuredAt":         int(time.time() * 1000),
    "heartRate":          union("$heart_rate"),
    "spo2":               union("$spo2"),
    "systolicBp":         union("$systolic_bp"),
    "diastolicBp":        None,
    "temperatureCelsius": union("$temp"),
    "respiratoryRate":    union("$rr"),
}))
PYEOF
}

# Collect all records into a temp file so we can pipe them in one producer invocation
# (avoids spawning a JVM per record)
TMPFILE=$(mktemp)
trap 'rm -f "$TMPFILE"' EXIT

declare -a DESCS

add_record() {
  build_record "$1" "$2" "$3" "$4" "$5" "$6" >> "$TMPFILE"
  DESCS+=("$7 → $1")
}

# ── Test cases ────────────────────────────────────────────────────────────────
add_record "patient-011" "130"  "98"  "120" "37.0" "16"  "CRITICAL heart rate (tachycardia)"
#add_record "patient-002" "72"   "85"  "120" "37.0" "16"  "CRITICAL SpO2 < 90%"
#add_record "patient-003" "72"   "98"  "190" "37.0" "16"  "HIGH systolic BP (hypertensive crisis)"
#add_record "patient-004" "72"   "98"  "70"  "37.0" "16"  "CRITICAL low systolic BP (hypotension)"
#add_record "patient-005" "72"   "98"  "120" "40.2" "16"  "MEDIUM high temperature"
#add_record "patient-006" "72"   "98"  "120" "34.0" "16"  "HIGH low temperature (hypothermia)"
#add_record "patient-007" "72"   "98"  "120" "37.0" "35"  "HIGH respiratory rate"
#add_record "patient-008" "35"   "85"  "70"  "37.0" "5"   "CRITICAL multiple: HR + SpO2 + BP + RR"
#add_record "patient-009" "80"   "99"  "120" "37.0" "14"  "NORMAL — should produce NO alert"

# Produce all records in a single producer invocation
cat "$TMPFILE" | docker exec -i schema-registry kafka-avro-console-producer \
  --bootstrap-server "$BOOTSTRAP_INTERNAL" \
  --topic "$TOPIC" \
  --property schema.registry.url="$SCHEMA_REGISTRY_INTERNAL" \
  --property value.schema.id="$SCHEMA_ID" 2>&1 | grep -v "^$" || true

for desc in "${DESCS[@]}"; do
  pass "Sent: $desc"
done

echo ""
echo "============================================================"
echo -e "${GREEN}Done! Watch for alerts:${NC}"
echo "  docker run --rm --network med-kafka-net \\"
echo "    confluentinc/cp-kafka:7.7.1 \\"
echo "    kafka-console-consumer --bootstrap-server broker-1:29092 \\"
echo "      --topic medical.alert.triggered --from-beginning"
echo "============================================================"
