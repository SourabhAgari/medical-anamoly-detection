---
type: community
cohesion: 0.12
members: 25
---

# Core Pipeline Concepts

**Cohesion:** 0.12 - loosely connected
**Members:** 25 nodes

## Members
- [[AlertEvent Avro Schema (output)]] - document - CLAUDE.md
- [[AnomalyDetectionFunction (RichFlatMapFunction)]] - code - med-flink/src/main/java/com/healthcare/vitals/operator/AnomalyDetectionFunction.java
- [[Architectural Decision DLQ on Deserialize Error]] - document - CLAUDE.md
- [[Architectural Decision Event-time on measuredAt]] - document - CLAUDE.md
- [[Architectural Decision Nullable Vital Fields in Avro]] - document - CLAUDE.md
- [[Architectural Decision Stateless Anomaly Detection]] - document - CLAUDE.md
- [[FaultTolerantDeserializer (inner class)]] - code - med-flink/src/main/java/com/healthcare/vitals/source/KafkaSourceFactory.java
- [[FaultTolerantDeserializerTest (40+ unit tests)]] - code - med-flink/src/test/java/com/healthcare/vitals/source/FaultTolerantDeserializerTest.java
- [[JobConfig (Environment-based Configuration)]] - code - med-flink/src/main/java/com/healthcare/vitals/job/JobConfig.java
- [[JobConfig.toSchemaRegistryProperties()]] - code - med-flink/src/main/java/com/healthcare/vitals/job/JobConfig.java
- [[KafkaSinkFactory_1]] - code - med-flink/src/main/java/com/healthcare/vitals/sink/KafkaSinkFactory.java
- [[KafkaSinkFactory.create()]] - code - med-flink/src/main/java/com/healthcare/vitals/sink/KafkaSinkFactory.java
- [[KafkaSourceFactory_1]] - code - med-flink/src/main/java/com/healthcare/vitals/source/KafkaSourceFactory.java
- [[KafkaSourceFactory.create()]] - code - med-flink/src/main/java/com/healthcare/vitals/source/KafkaSourceFactory.java
- [[Late Data Side Output Missing (30s records silently dropped)]] - document - notes/prod_conisderations.txt
- [[NICE NG51  NEWS2 Clinical Thresholds]] - document - CLAUDE.md
- [[PatientVitals Avro Schema (input)]] - document - CLAUDE.md
- [[Sink Key Strategy patientId bytes]] - code - med-flink/src/main/java/com/healthcare/vitals/sink/KafkaSinkFactory.java
- [[Topic medical.alert.triggered]] - code - med-flink/src/main/java/com/healthcare/vitals/config/TopicNames.java
- [[Topic medical.vitals.dlq]] - code - med-flink/src/main/java/com/healthcare/vitals/config/TopicNames.java
- [[Topic medical.vitals.raw]] - code - med-flink/src/main/java/com/healthcare/vitals/config/TopicNames.java
- [[TopicNames (Config Class)]] - code - med-flink/src/main/java/com/healthcare/vitals/config/TopicNames.java
- [[VitalsAnomalyDetectionJob.buildPipeline()]] - code - med-flink/src/main/java/com/healthcare/vitals/job/VitalsAnomalyDetectionJob.java
- [[WatermarkStrategyFactory_1]] - code - med-flink/src/main/java/com/healthcare/vitals/source/WatermarkStrategyFactory.java
- [[WatermarkStrategyFactory.forVitals()]] - code - med-flink/src/main/java/com/healthcare/vitals/source/WatermarkStrategyFactory.java

## Live Query (requires Dataview plugin)

```dataview
TABLE source_file, type FROM #community/Core_Pipeline_Concepts
SORT file.name ASC
```

## Connections to other communities
- 4 edges to [[_COMMUNITY_Anomaly Detection Logic]]
- 1 edge to [[_COMMUNITY_DLQ Error Routing Flow]]

## Top bridge nodes
- [[KafkaSinkFactory.create()]] - degree 7, connects to 1 community
- [[AnomalyDetectionFunction (RichFlatMapFunction)]] - degree 6, connects to 1 community
- [[AlertEvent Avro Schema (output)]] - degree 4, connects to 1 community
- [[Topic medical.vitals.dlq]] - degree 3, connects to 1 community
- [[Late Data Side Output Missing (30s records silently dropped)]] - degree 2, connects to 1 community