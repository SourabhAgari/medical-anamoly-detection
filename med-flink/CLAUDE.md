# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Build fat JAR (target/med-flink-1.0.0-SNAPSHOT-shaded.jar)
mvn clean package

# Run unit tests
mvn test

# Run a single test class
mvn test -Dtest=AnomalyDetectionFunctionTest

# Run a single test method
mvn test -Dtest=AnomalyDetectionFunctionTest#testHighHeartRateAlert
```

## Local Development with Docker

```bash
# Start the Flink cluster (requires Kafka cluster on med-kafka-net to be running first)
docker compose up -d

# Submit the job to the running cluster
./scripts/submit-job.sh

# Produce test PatientVitals records to trigger various alert scenarios
./scripts/produce-test-vitals.sh

# Flink Web UI
open http://localhost:8082
```

## Architecture Overview

This is an Apache Flink 1.19.1 streaming job that reads patient vital signs from Kafka, evaluates them against clinical thresholds, and emits alert events.

**Pipeline (defined in `VitalsAnomalyDetectionJob.java`)**:
```
Kafka (medical.vitals.raw) → Watermark → keyBy(patientId) → AnomalyDetectionFunction → Kafka (medical.alert.triggered)
```

**Key classes**:
- `VitalsAnomalyDetectionJob` — Flink job entry point; wires together source, operator, and sink
- `AnomalyDetectionFunction` — Core logic; stateless `RichFlatMapFunction` that evaluates each vital against thresholds and emits 0–N `AlertEvent`s per reading
- `JobConfig` — Loads all configuration from environment variables; centralizes defaults for local dev
- `TopicNames` — Single source of truth for Kafka topic names
- `KafkaSourceFactory` — Builds the Kafka source with `FaultTolerantDeserializer` (skips bad records instead of crashing)
- `KafkaSinkFactory` — Builds the Kafka sink; keys by `patientId` for per-patient partition affinity
- `WatermarkStrategyFactory` — Event-time from `measuredAt`; 30s out-of-orderness, 5m idleness timeout

**Avro schemas** (in `src/main/avro/`):
- `PatientVitals.avsc` — Input: vitals readings from bedside devices (heartRate, spo2, systolicBp, temperatureCelsius, respiratoryRate — all optional/nullable)
- `AlertEvent.avsc` — Output: includes `VitalRule` enum (e.g. `HIGH_HEART_RATE`) and `AlertSeverity` enum (LOW/MEDIUM/HIGH/CRITICAL)

Java classes are generated from Avro schemas at build time by the Avro Maven plugin.

## Clinical Thresholds (NICE NG51 / NEWS2)

| Vital | Rule | Threshold | Severity |
|-------|------|-----------|----------|
| Heart Rate | HIGH | > 120 bpm | HIGH |
| Heart Rate | LOW | < 40 bpm | CRITICAL |
| SpO2 | LOW | < 90% | CRITICAL |
| Systolic BP | HIGH | > 180 mmHg | HIGH |
| Systolic BP | LOW | < 80 mmHg | CRITICAL |
| Temperature | HIGH | > 39.5°C | MEDIUM |
| Temperature | LOW | < 35°C | HIGH |
| Respiratory Rate | HIGH | > 30/min | HIGH |
| Respiratory Rate | LOW | < 8/min | CRITICAL |

## Infrastructure Dependencies

The Flink cluster connects to an external Kafka cluster (3 brokers + Schema Registry) via Docker network `med-kafka-net`. This Kafka cluster is expected to be managed separately (sibling `med-infra` project).

Environment variables controlling runtime behavior (see `JobConfig.java` for all defaults):
- `KAFKA_BOOTSTRAP` — broker list (default: `broker-1:29092,broker-2:29092,broker-3:29092`)
- `SCHEMA_REGISTRY_URL` — Confluent Schema Registry (default: `http://schema-registry:8081`)
- `KAFKA_CONSUMER_GROUP` — (default: `vitals-anomaly-detector`)
- `FLINK_PARALLELISM` — (default: 1)
- `FLINK_CHECKPOINT_INTERVAL_MS` — (default: 30000)

## Production Considerations (noted in code comments)

- Switch `HashMapStateBackend` → `RocksDB` for large state
- Switch sink delivery guarantee from `AT_LEAST_ONCE` → `EXACTLY_ONCE` with a transactional ID
- Add Schema Registry authentication (`SCHEMA_REGISTRY_AUTH` placeholder exists in `JobConfig`)
- `medical.vitals.dlq` topic for dead-letter queue is defined but not yet implemented