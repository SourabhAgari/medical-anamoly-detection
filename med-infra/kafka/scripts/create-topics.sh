#!/bin/bash
# =============================================================================
# Topic Provisioning Script — Medical Anomaly Detection
# Run once at cluster bootstrap; idempotent (--if-not-exists)
# =============================================================================

set -euo pipefail

BOOTSTRAP="broker-1:29092,broker-2:29092,broker-3:29092"
RF=3          # replication factor
PARTITIONS=6  # tune per throughput in prod

# Wait until all brokers are visible in metadata
echo "[init] Waiting for Kafka brokers to be ready..."
until kafka-broker-api-versions --bootstrap-server "$BOOTSTRAP" &>/dev/null; do
  echo "[init] Brokers not ready yet — sleeping 5s..."
  sleep 5
done
echo "[init] All brokers reachable. Provisioning topics..."

# ── Helper ────────────────────────────────────────────────────────────────
create_topic() {
  local topic=$1
  local partitions=$2
  local rf=$3
  local extra=${4:-}

  kafka-topics --bootstrap-server "$BOOTSTRAP" \
    --create \
    --if-not-exists \
    --topic "$topic" \
    --partitions "$partitions" \
    --replication-factor "$rf" \
    $extra

  echo "[init] Created (or already exists): $topic  [p=$partitions rf=$rf]"
}

# =============================================================================
# RAW INGESTION
# High-throughput, short-keyed by patient-id for ordering guarantees
# =============================================================================
create_topic "medical.vitals.raw"        $PARTITIONS $RF \
  "--config retention.ms=604800000 \
   --config min.insync.replicas=2 \
   --config message.timestamp.type=LogAppendTime"

# =============================================================================
# NORMALISED / VALIDATED VITALS
# After Flink schema validation + unit normalisation
# =============================================================================
create_topic "medical.vitals.normalised" $PARTITIONS $RF \
  "--config retention.ms=604800000 \
   --config min.insync.replicas=2"

# =============================================================================
# ANOMALY EVENTS
# Output of Flink anomaly-detection job
# =============================================================================
create_topic "medical.anomaly.detected"  $PARTITIONS $RF \
  "--config retention.ms=2592000000 \
   --config min.insync.replicas=2"

# =============================================================================
# ALERT EVENTS
# Downstream of anomaly detection; consumed by notification service
# =============================================================================
create_topic "medical.alert.triggered"   $PARTITIONS $RF \
  "--config retention.ms=2592000000 \
   --config min.insync.replicas=2"

# =============================================================================
# DEAD LETTER QUEUE
# Records that fail validation/processing — never silently dropped
# =============================================================================
create_topic "medical.vitals.dlq"        3 $RF \
  "--config retention.ms=2592000000 \
   --config min.insync.replicas=2"

# =============================================================================
# AUDIT TRAIL
# Immutable log for HIPAA compliance — long retention, compacted+delete
# In prod set retention.ms=-1 (infinite) and archive to cold storage
# =============================================================================
create_topic "medical.audit.events"      3 $RF \
  "--config retention.ms=31536000000 \
   --config cleanup.policy=compact \
   --config min.insync.replicas=2"

# =============================================================================
# PATIENT STATE CHANGELOG (for Flink state store backup)
# Compacted — only latest state per patient key is kept
# =============================================================================
create_topic "medical.patient.state"     $PARTITIONS $RF \
  "--config cleanup.policy=compact \
   --config min.insync.replicas=2 \
   --config delete.retention.ms=86400000"

echo ""
echo "[init] Topic provisioning complete. Current topic list:"
kafka-topics --bootstrap-server "$BOOTSTRAP" --list | grep "^medical\." | sort