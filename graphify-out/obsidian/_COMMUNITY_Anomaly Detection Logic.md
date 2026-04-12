---
type: community
cohesion: 0.10
members: 28
---

# Anomaly Detection Logic

**Cohesion:** 0.10 - loosely connected
**Members:** 28 nodes

## Members
- [[AT_LEAST_ONCE Delivery Guarantee (Sink)]] - code - med-flink/src/main/java/com/healthcare/vitals/sink/KafkaSinkFactory.java
- [[AT_LEAST_ONCE Sink Duplicate Alert Risk]] - document - notes/prod_conisderations.txt
- [[Alert Time Uses Wall Clock Instead of Event Time]] - document - notes/prod_conisderations.txt
- [[AlertSeverity Enum (LOW, MEDIUM, HIGH, CRITICAL)]] - code - med-flink/src/main/java/com/healthcare/vitals/operator/AnomalyDetectionFunction.java
- [[AnomalyDetectionFunction.buildAlert()]] - code - med-flink/src/main/java/com/healthcare/vitals/operator/AnomalyDetectionFunction.java
- [[AnomalyDetectionFunction.checkHeartRate()]] - code - med-flink/src/main/java/com/healthcare/vitals/operator/AnomalyDetectionFunction.java
- [[AnomalyDetectionFunction.checkRespiratoryRate()]] - code - med-flink/src/main/java/com/healthcare/vitals/operator/AnomalyDetectionFunction.java
- [[AnomalyDetectionFunction.checkSpO2()]] - code - med-flink/src/main/java/com/healthcare/vitals/operator/AnomalyDetectionFunction.java
- [[AnomalyDetectionFunction.checkSystolicBP()]] - code - med-flink/src/main/java/com/healthcare/vitals/operator/AnomalyDetectionFunction.java
- [[AnomalyDetectionFunction.checkTemperature()]] - code - med-flink/src/main/java/com/healthcare/vitals/operator/AnomalyDetectionFunction.java
- [[AnomalyDetectionFunction.flatMap()]] - code - med-flink/src/main/java/com/healthcare/vitals/operator/AnomalyDetectionFunction.java
- [[AnomalyDetectionFunctionTest (35+ unit tests)]] - code - med-flink/src/test/java/com/healthcare/vitals/operator/AnomalyDetectionFunctionTest.java
- [[Checkpoint Storage on Local Docker Volume Risk]] - document - notes/prod_conisderations.txt
- [[Double.NaN Vital Test Gap (sensor malfunction masking)]] - document - notes/prod_conisderations.txt
- [[EXACTLY_ONCE Checkpointing Configuration]] - code - med-flink/src/main/java/com/healthcare/vitals/job/VitalsAnomalyDetectionJob.java
- [[Fixed Delay Restart Strategy (3 attempts, 10s)]] - code - med-flink/src/main/java/com/healthcare/vitals/job/VitalsAnomalyDetectionJob.java
- [[HIPAA PHI Disclosure Risk (patientId in logs)]] - document - notes/prod_conisderations.txt
- [[HashMapStateBackend (in-memory, dev)]] - code - med-flink/src/main/java/com/healthcare/vitals/job/VitalsAnomalyDetectionJob.java
- [[Kafka TLSSSL Missing (cleartext vitals)]] - document - notes/prod_conisderations.txt
- [[ListCollector Test Helper (in-memory output collector)]] - code - med-flink/src/test/java/com/healthcare/vitals/operator/AnomalyDetectionFunctionTest.java
- [[MDC vitalIdwardId Context in flatMap()]] - code - med-flink/src/main/java/com/healthcare/vitals/operator/AnomalyDetectionFunction.java
- [[Non-deterministic alertId Deduplication Issue]] - document - notes/prod_conisderations.txt
- [[Production Readiness TODO List]] - document - notes/prod_conisderations.txt
- [[Production Upgrade Checklist]] - document - CLAUDE.md
- [[RocksDB State Backend Upgrade (scalability)]] - document - notes/prod_conisderations.txt
- [[VitalRule Enum (HIGH_HEART_RATE, LOW_SPO2, etc.)]] - code - med-flink/src/main/java/com/healthcare/vitals/operator/AnomalyDetectionFunction.java
- [[VitalsAnomalyDetectionJob.buildEnvironment()]] - code - med-flink/src/main/java/com/healthcare/vitals/job/VitalsAnomalyDetectionJob.java
- [[buildVitals() Builder Pattern (test helper)]] - code - med-flink/src/test/java/com/healthcare/vitals/operator/AnomalyDetectionFunctionTest.java

## Live Query (requires Dataview plugin)

```dataview
TABLE source_file, type FROM #community/Anomaly_Detection_Logic
SORT file.name ASC
```

## Connections to other communities
- 4 edges to [[_COMMUNITY_Core Pipeline Concepts]]
- 3 edges to [[_COMMUNITY_DLQ Error Routing Flow]]
- 1 edge to [[_COMMUNITY_DLQ Deserializer Tests]]

## Top bridge nodes
- [[Production Readiness TODO List]] - degree 11, connects to 2 communities
- [[AnomalyDetectionFunction.buildAlert()]] - degree 10, connects to 1 community
- [[AnomalyDetectionFunctionTest (35+ unit tests)]] - degree 6, connects to 1 community
- [[MDC vitalIdwardId Context in flatMap()]] - degree 3, connects to 1 community
- [[AT_LEAST_ONCE Delivery Guarantee (Sink)]] - degree 3, connects to 1 community