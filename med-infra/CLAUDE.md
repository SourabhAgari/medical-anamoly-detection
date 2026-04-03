# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

`med-infra` is the Kafka infrastructure layer for the medical anomaly detection platform. It provisions a 3-broker KRaft (ZooKeeper-less) Kafka cluster with Confluent Schema Registry, creates all required topics, and registers Avro schemas. The sibling project `med-flink` contains the Flink streaming job that consumes from this cluster.

## Common Commands

```bash
# Start the full Kafka cluster (brokers + Schema Registry + Kafka UI)
docker compose up -d

# Stop the cluster
docker compose down

# Verify cluster health (brokers, topic replication, Schema Registry)
bash kafka/scripts/health-check.sh

# Create all topics (idempotent — safe to re-run)
bash kafka/scripts/create-topics.sh

# Register Avro schemas with Schema Registry
bash kafka/scripts/register-schemas.sh
```

**Startup order**: brokers must be healthy before registering schemas. `health-check.sh` polls for readiness.

## Kafka Topics

| Topic | Partitions | RF | Retention | Notes |
|-------|------------|----|-----------|-------|
| `medical.vitals.raw` | 6 | 3 | 7 days | Device readings ingestion |
| `medical.vitals.normalised` | 6 | 3 | 7 days | Validated/normalized vitals (future) |
| `medical.anomaly.detected` | 6 | 3 | 30 days | Raw anomaly stream |
| `medical.alert.triggered` | 6 | 3 | 30 days | Clinician-facing alerts |
| `medical.vitals.dlq` | 3 | 3 | 30 days | Dead-letter queue for bad records |
| `medical.patient.state` | 6 | 3 | compacted | Flink state changelog |
| `medical.audit.events` | 3 | 3 | 365 days | HIPAA audit trail (also compacted) |

## Architecture

```
Bedside Devices
      │
      ▼
medical.vitals.raw   (Kafka, 6 partitions, RF=3)
      │  [Avro via Schema Registry :8081]
      ▼
Apache Flink (med-flink project)
      │  AnomalyDetectionFunction
      ▼
medical.alert.triggered   (Kafka, keyed by patientId)
      │
      ▼
Notification Service / EHR
```

### Network

All services share Docker network `med-kafka-net`. The Flink cluster (in `med-flink`) joins this same external network to reach Kafka. Clients outside Docker reach brokers on `localhost:9092–9094`.

| Port | Service |
|------|---------|
| 9092–9094 | Kafka brokers (external client access) |
| 8081 | Confluent Schema Registry |
| 8080 | Redpanda Console (Kafka UI) |
| 9101–9103 | JMX metrics per broker |

Internal inter-broker communication uses port 29092; controller traffic uses 29093.

### Cluster Configuration (`.env`)

- Confluent Platform 7.7.1, KRaft mode (no ZooKeeper)
- Replication factor: 3, `min.insync.replicas`: 2
- Auto topic creation: **disabled** — all topics created via `create-topics.sh`
- Log retention: 7 days default (audit topic overrides to 365 days)

## Avro Schemas

Schemas are under `kafka/schemas/` (referenced by `register-schemas.sh`) and also live in the sibling `med-flink/src/main/avro/` for Java code generation:

- `PatientVitals.avsc` — input from bedside devices; all vital fields are nullable (sensor may not report every metric)
- `AlertEvent.avsc` — output alert; includes `VitalRule` enum and `AlertSeverity` enum (LOW/MEDIUM/HIGH/CRITICAL)

After modifying schemas, re-run `register-schemas.sh` and rebuild the Flink fat JAR (`mvn clean package` in `med-flink`). Schema Registry enforces backward compatibility by default.