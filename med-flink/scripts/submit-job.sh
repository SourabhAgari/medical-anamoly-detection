#!/bin/bash
# =============================================================================
# Submit the Medical Anomaly Detection Flink job to the local cluster.
# Requires: the Flink cluster to be up (docker compose up -d in med-flink/)
# =============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
JAR="$PROJECT_DIR/target/med-flink-1.0.0-SNAPSHOT-shaded.jar"
JM_REST="http://localhost:8082"
MAIN_CLASS="com.healthcare.vitals.job.VitalsAnomalyDetectionJob"

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; NC='\033[0m'
pass() { echo -e "${GREEN}[PASS]${NC} $1"; }
fail() { echo -e "${RED}[FAIL]${NC} $1"; exit 1; }
info() { echo -e "${YELLOW}[INFO]${NC} $1"; }

echo "============================================================"
echo "  Medical Anomaly Detection — Flink Job Submission"
echo "============================================================"

# ── Preflight checks ──────────────────────────────────────────────────────────
[ -f "$JAR" ] || fail "Fat jar not found at $JAR — run: mvn clean package -Plocal-run"

info "Checking JobManager at $JM_REST ..."
JM_STATUS=$(curl -s "$JM_REST/overview" -o /dev/null -w "%{http_code}")
[ "$JM_STATUS" = "200" ] || fail "JobManager not reachable (HTTP $JM_STATUS). Is the Flink cluster up?"
pass "JobManager is reachable"

TM_COUNT=$(curl -s "$JM_REST/taskmanagers" | python3 -c "import sys,json; d=json.load(sys.stdin); print(len(d.get('taskmanagers', [])))")
[ "$TM_COUNT" -ge 1 ] || fail "No TaskManagers registered. Start the cluster first."
pass "$TM_COUNT TaskManager(s) registered"

# ── Upload jar ────────────────────────────────────────────────────────────────
info "Uploading fat jar to JobManager..."
UPLOAD_RESPONSE=$(curl -sf -X POST \
  -H "Expect:" \
  -F "jarfile=@$JAR" \
  "$JM_REST/jars/upload")
JAR_ID=$(echo "$UPLOAD_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin)['filename'].split('/')[-1])")
[ -n "$JAR_ID" ] || fail "Jar upload failed. Response: $UPLOAD_RESPONSE"
pass "Jar uploaded — ID: $JAR_ID"

# ── Submit job ────────────────────────────────────────────────────────────────
info "Submitting job: $MAIN_CLASS"
SUBMIT_RESPONSE=$(curl -sf -X POST \
  -H "Content-Type: application/json" \
  -d "{
    \"entryClass\": \"$MAIN_CLASS\",
    \"parallelism\": 1,
    \"programArgsList\": []
  }" \
  "$JM_REST/jars/$JAR_ID/run")

JOB_ID=$(echo "$SUBMIT_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin).get('jobid',''))")
[ -n "$JOB_ID" ] || fail "Job submission failed. Response: $SUBMIT_RESPONSE"
pass "Job submitted — Job ID: $JOB_ID"

echo ""
echo "============================================================"
echo -e "${GREEN}Job is running!${NC}"
echo "  Flink UI     : $JM_REST/#/job/$JOB_ID"
echo "  Kafka UI     : http://localhost:8080"
echo "  Job ID       : $JOB_ID"
echo ""
echo "  Watch alerts : docker run --rm --network med-kafka-net \\"
echo "    confluentinc/cp-kafka:7.7.1 \\"
echo "    kafka-console-consumer --bootstrap-server broker-1:29092 \\"
echo "      --topic medical.alert.triggered --from-beginning"
echo "============================================================"