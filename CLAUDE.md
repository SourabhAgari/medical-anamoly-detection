# Medical Anomaly Detection — CLAUDE.md

## Project Overview

Real-time patient vital sign anomaly detection system built on Apache Flink and Kafka. Bedside devices publish vital readings to Kafka; a Flink streaming job evaluates each reading against NICE NG51 / NEWS2 clinical thresholds and emits structured alerts to clinical staff.

**Two sub-projects:**
- `med-infra/` — Kafka infrastructure (3-broker KRaft cluster + Schema Registry)
- `med-flink/` — Flink streaming job (anomaly detection pipeline)

---

## Repository Layout

```
medical-anamoly-detection/
├── med-infra/                          # Kafka + Schema Registry infrastructure
│   ├── docker-compose.yml             # 3-broker KRaft cluster + Schema Registry + Redpanda UI
│   ├── .env                           # Cluster IDs, replication factors, retention settings
│   └── kafka/scripts/
│       ├── create-topics.sh           # Idempotent topic provisioning (7 topics)
│       ├── health-check.sh            # Broker + topic + Schema Registry validation
│       └── register-schemas.sh        # Avro schema registration (FULL_TRANSITIVE)
│
└── med-flink/                         # Flink streaming job
    ├── src/main/java/com/healthcare/vitals/
    │   ├── job/
    │   │   ├── VitalsAnomalyDetectionJob.java   # Main entry point, pipeline assembly
    │   │   └── JobConfig.java                   # Config from env vars (builder pattern)
    │   ├── operator/
    │   │   └── AnomalyDetectionFunction.java    # Core detection logic (RichFlatMapFunction)
    │   ├── source/
    │   │   ├── KafkaSourceFactory.java          # Kafka source + FaultTolerantDeserializer
    │   │   └── WatermarkStrategyFactory.java    # Event-time watermarks (measuredAt field)
    │   ├── sink/
    │   │   └── KafkaSinkFactory.java            # Kafka sink with Avro serialization
    │   └── config/
    │       └── TopicNames.java                  # Topic name constants
    ├── src/main/avro/
    │   ├── PatientVitals.avsc                   # Input schema
    │   └── AlertEvent.avsc                      # Output schema
    ├── src/test/java/com/healthcare/vitals/
    │   ├── AnomalyDetectionFunctionTest.java    # 35+ unit tests for detection logic
    │   └── FaultTolerantDeserializerTest.java   # 40+ unit tests for DLQ routing
    ├── docker-compose.yml             # Flink JobManager + TaskManager
    ├── flink-conf/config.yaml         # Flink runtime settings
    ├── pom.xml                        # Maven build (Java 11, Flink 1.19.1)
    └── scripts/
        ├── submit-job.sh              # Deploy fat JAR to running cluster
        └── produce-test-vitals.sh     # Generate test vital sign records
```

---

## Key Technologies & Versions

| Component | Version |
|---|---|
| Java | 11 |
| Apache Flink | 1.19.1 |
| Confluent Platform | 7.7.1 |
| Flink Kafka Connector | 3.2.0-1.19 |
| Apache Avro | 1.11.3 |
| Log4j | 2.23.1 (SLF4J bridge) |
| JUnit | 5.10.2 |
| Mockito | 5.11.0 |
| AssertJ | 3.25.3 |

---

## Build & Run

### Prerequisites
- Docker + Docker Compose
- Java 11
- Maven 3.x

### Start infrastructure (run once)
```bash
cd med-infra
docker compose up -d
bash kafka/scripts/health-check.sh        # Wait until all brokers and SR are healthy
bash kafka/scripts/register-schemas.sh    # Register Avro schemas
```

### Build the Flink job
```bash
cd med-flink
mvn clean package
# Output: target/med-flink-1.0.0-SNAPSHOT-shaded.jar
```

### Start Flink cluster and submit job
```bash
cd med-flink
docker compose up -d
./scripts/submit-job.sh
```

### Generate test data
```bash
cd med-flink
./scripts/produce-test-vitals.sh
```

### Run tests
```bash
mvn test                                                         # All tests
mvn test -Dtest=AnomalyDetectionFunctionTest                    # Detection logic only
mvn test -Dtest=FaultTolerantDeserializerTest                   # DLQ routing only
mvn test -Dtest=AnomalyDetectionFunctionTest#testHighHeartRate  # Single test
```

---

## Pipeline Architecture

```
medical.vitals.raw (Kafka)
        |
        v
FaultTolerantDeserializer
  - Valid records → PatientVitals stream
  - Bad records   → medical.vitals.dlq (with error headers)
        |
        v
WatermarkStrategy (event-time on measuredAt, 30s out-of-orderness)
        |
        v
keyBy(patientId)
        |
        v
AnomalyDetectionFunction (RichFlatMapFunction, stateless)
  - 0..N alerts per vital reading
        |
        v
medical.alert.triggered (Kafka, keyed by patientId)
```

**Checkpointing:** EXACTLY_ONCE, 30s interval, HashMapStateBackend, retained 3 checkpoints  
**Restart strategy:** Fixed delay, 3 attempts, 10s delay  
**Delivery guarantee:** AT_LEAST_ONCE (sink) — upgrade to EXACTLY_ONCE with transactional ID for production

---

## Data Schemas

### PatientVitals (input — `medical.vitals.raw`)
```
vitalId          string         UUID per reading
patientId        string         Hospital patient identifier
wardId           string         Ward/unit location
deviceId         string?        Nullable bedside device ID
measuredAt       timestamp-ms   UTC measurement time (used for event-time)
heartRate        double?        bpm (nullable)
spo2             double?        % oxygen saturation (nullable)
systolicBp       double?        mmHg (nullable)
diastolicBp      double?        mmHg (nullable)
temperatureCelsius double?      °C (nullable)
respiratoryRate  double?        breaths/min (nullable)
```

### AlertEvent (output — `medical.alert.triggered`)
```
alertId          string         UUID per alert
vitalId          string         Source vital reading
patientId        string
wardId           string
alertTime        timestamp-ms   When alert was generated
ruleTriggered    VitalRule      Enum (see thresholds below)
severity         AlertSeverity  LOW | MEDIUM | HIGH | CRITICAL
observedValue    double         Measured value that breached threshold
thresholdValue   double         Threshold that was breached
message          string         Human-readable alert description
```

---

## Clinical Thresholds (NICE NG51 / NEWS2)

| Vital | Rule (VitalRule) | Threshold | Severity |
|---|---|---|---|
| Heart Rate | HIGH_HEART_RATE | > 120 bpm | HIGH |
| Heart Rate | LOW_HEART_RATE | < 40 bpm | CRITICAL |
| SpO2 | LOW_SPO2 | < 90% | CRITICAL |
| Systolic BP | HIGH_SYSTOLIC_BP | > 180 mmHg | HIGH |
| Systolic BP | LOW_SYSTOLIC_BP | < 80 mmHg | CRITICAL |
| Temperature | HIGH_TEMPERATURE | > 39.5°C | MEDIUM |
| Temperature | LOW_TEMPERATURE | < 35°C | HIGH |
| Respiratory Rate | HIGH_RESPIRATORY_RATE | > 30/min | HIGH |
| Respiratory Rate | LOW_RESPIRATORY_RATE | < 8/min | CRITICAL |

One vital reading can produce 0–5 alerts simultaneously. Null vital fields are silently skipped (no exception thrown).

---

## Kafka Topics

| Topic | Partitions | RF | Retention | Purpose |
|---|---|---|---|---|
| medical.vitals.raw | 6 | 3 | 7 days | Raw bedside device input |
| medical.vitals.normalised | 6 | 3 | 7 days | Post-validation vitals (future use) |
| medical.anomaly.detected | 6 | 3 | 30 days | Raw anomaly stream |
| medical.alert.triggered | 6 | 3 | 30 days | Clinician-facing alerts |
| medical.vitals.dlq | 3 | 3 | 30 days | Deserialization failures |
| medical.patient.state | 6 | 3 | compacted | Flink state changelog |
| medical.audit.events | 3 | 3 | 365 days | HIPAA audit trail |

Topic name constants live in `TopicNames.java` — always use those, never inline strings.

---

## Configuration

All runtime config is injected via environment variables. Defaults are local-dev friendly.

| Env Var | Default | Purpose |
|---|---|---|
| `KAFKA_BOOTSTRAP` | `localhost:9092,9093,9094` | Kafka broker list |
| `SCHEMA_REGISTRY_URL` | `http://localhost:8081` | Confluent Schema Registry |
| `KAFKA_CONSUMER_GROUP` | `vitals-anomaly-detector` | Consumer group ID |
| `KAFKA_AUTO_OFFSET_RESET` | `latest` | Offset reset policy |
| `FLINK_PARALLELISM` | `1` | Job parallelism |
| `FLINK_CHECKPOINT_INTERVAL_MS` | `30000` | Checkpoint interval |

Docker Compose injects broker-internal addresses (`broker-1:29092` etc.). In tests, use `JobConfig.builder()` directly.

---

## Monitoring

| Interface | URL | Purpose |
|---|---|---|
| Flink Web UI | http://localhost:8082 | Job graph, checkpoints, task metrics |
| Kafka UI (Redpanda) | http://localhost:8080 | Topics, consumer groups, Schema Registry |
| Schema Registry | http://localhost:8081 | Avro schema browser |

Watch alerts live:
```bash
docker run --rm --network med-kafka-net confluentinc/cp-kafka:7.7.1 \
  kafka-console-consumer \
  --bootstrap-server broker-1:29092 \
  --topic medical.alert.triggered \
  --from-beginning
```

---

## Fault Tolerance & DLQ

`FaultTolerantDeserializer` (inner class of `KafkaSourceFactory`) wraps the Confluent Avro deserializer:
- On any deserialization error (corrupt bytes, schema mismatch, wrong format), the raw bytes are routed to `medical.vitals.dlq` with Kafka headers: `source.topic`, `source.partition`, `source.offset`, `error.class`, `error.message` (truncated to 500 chars).
- The DLQ producer is lazy-initialized on the first error (not on the happy path).
- DLQ producer config: `acks=1`, 3 retries, 5s max block time — non-blocking to main pipeline.
- If the DLQ producer itself fails, the error is logged and the record is dropped — the main pipeline never crashes.

---

## Code Conventions

- **Avro generated classes** live under `target/generated-sources/avro/`. Never edit them directly; edit `.avsc` files in `src/main/avro/`.
- **Null safety**: All vital fields in `PatientVitals` are nullable. Always use the `Optional`-style null checks already established in `AnomalyDetectionFunction` before comparing doubles.
- **Topic names**: Use constants from `TopicNames.java`, never hardcode strings.
- **Tests**: Use the `buildVitals()` builder pattern from `AnomalyDetectionFunctionTest` and `ListCollector` to capture output. Follow the existing JUnit 5 + AssertJ + Mockito style.
- **Logging**: SLF4J only — no `System.out`. Log4j 2 is the backend (configured by Flink).
- **JobConfig**: Use `JobConfig.fromEnvironment()` in production, `JobConfig.builder()` in tests.

---

## Production Upgrade Checklist

These items are noted in code comments but not yet activated:

- [ ] **State backend**: Switch `HashMapStateBackend` → `RocksDBStateBackend` for large patient populations
- [ ] **Sink delivery**: Switch `AT_LEAST_ONCE` → `EXACTLY_ONCE` with a transactional producer ID
- [ ] **Schema Registry auth**: Uncomment basic-auth config in `JobConfig.toSchemaRegistryProperties()`
- [ ] **Checkpoint storage**: Replace local filesystem with S3/GCS/NFS mount in `flink-conf/config.yaml`
- [ ] **DLQ consumer**: Implement a replay/alerting service to consume `medical.vitals.dlq`
- [ ] **TLS**: Enable `security.ssl.enabled` + keystore/truststore paths in Flink config
- [ ] **Log retention**: Extend `medical.vitals.raw` retention from 7 → 365 days for HIPAA compliance
- [ ] **Metrics**: Switch to Prometheus metrics reporter for production observability

---

## Architectural Decisions

| Decision | Rationale |
|---|---|
| Stateless anomaly detection | No keyed state; scales horizontally without state rebalancing |
| keyBy(patientId) before operator | Per-patient ordering + partition affinity for alert consumers |
| Event-time on `measuredAt` | Use device timestamp, not Kafka ingestion time, for clinical accuracy |
| FULL_TRANSITIVE schema compatibility | Safe rolling upgrades; Confluent enforces this on Schema Registry |
| KRaft (no ZooKeeper) | Simplified operations; fewer moving parts in clinical environments |
| Nullable vital fields in Avro | Devices may omit sensors not present; null = sensor absent, not zero |
| DLQ on deserialize error | Pipeline never stops for corrupt records; bad data is preserved for investigation |