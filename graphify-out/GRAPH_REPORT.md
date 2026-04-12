# Graph Report - .  (2026-04-12)

## Corpus Check
- Corpus is ~7,055 words - fits in a single context window. You may not need a graph.

## Summary
- 180 nodes · 259 edges · 19 communities detected
- Extraction: 96% EXTRACTED · 4% INFERRED · 0% AMBIGUOUS · INFERRED: 10 edges (avg confidence: 0.84)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Anomaly Detection Logic|Anomaly Detection Logic]]
- [[_COMMUNITY_Core Pipeline Concepts|Core Pipeline Concepts]]
- [[_COMMUNITY_Anomaly Detection Tests|Anomaly Detection Tests]]
- [[_COMMUNITY_Job Config Builder|Job Config Builder]]
- [[_COMMUNITY_DLQ Deserializer Tests|DLQ Deserializer Tests]]
- [[_COMMUNITY_Fault Tolerant Deserializer|Fault Tolerant Deserializer]]
- [[_COMMUNITY_AnomalyDetectionFunction Class|AnomalyDetectionFunction Class]]
- [[_COMMUNITY_DLQ Error Routing Flow|DLQ Error Routing Flow]]
- [[_COMMUNITY_Job Entry Point|Job Entry Point]]
- [[_COMMUNITY_Watermark Strategy|Watermark Strategy]]
- [[_COMMUNITY_Kafka Sink Factory|Kafka Sink Factory]]
- [[_COMMUNITY_Topic Names Config|Topic Names Config]]
- [[_COMMUNITY_Project Structure|Project Structure]]
- [[_COMMUNITY_Schema Registry Config|Schema Registry Config]]
- [[_COMMUNITY_Kafka KRaft Cluster|Kafka KRaft Cluster]]
- [[_COMMUNITY_Pipeline Architecture Docs|Pipeline Architecture Docs]]
- [[_COMMUNITY_Kafka Topics|Kafka Topics]]
- [[_COMMUNITY_Fault Tolerance Design|Fault Tolerance Design]]
- [[_COMMUNITY_Flink Runtime|Flink Runtime]]

## God Nodes (most connected - your core abstractions)
1. `AnomalyDetectionFunctionTest` - 18 edges
2. `FaultTolerantDeserializerTest` - 17 edges
3. `JobConfig` - 13 edges
4. `Production Readiness TODO List` - 11 edges
5. `AnomalyDetectionFunction.buildAlert()` - 10 edges
6. `Builder` - 9 edges
7. `AnomalyDetectionFunction` - 9 edges
8. `Production Changes Made (HIPAA + MDC + Observability)` - 8 edges
9. `FaultTolerantDeserializer` - 7 edges
10. `AnomalyDetectionFunction.flatMap()` - 7 edges

## Surprising Connections (you probably didn't know these)
- `Production Upgrade Checklist` --semantically_similar_to--> `Production Readiness TODO List`  [INFERRED] [semantically similar]
  CLAUDE.md → notes/prod_conisderations.txt
- `NICE NG51 / NEWS2 Clinical Thresholds` --conceptually_related_to--> `AnomalyDetectionFunction (RichFlatMapFunction)`  [EXTRACTED]
  CLAUDE.md → med-flink/src/main/java/com/healthcare/vitals/operator/AnomalyDetectionFunction.java
- `Architectural Decision: Stateless Anomaly Detection` --rationale_for--> `AnomalyDetectionFunction (RichFlatMapFunction)`  [EXTRACTED]
  CLAUDE.md → med-flink/src/main/java/com/healthcare/vitals/operator/AnomalyDetectionFunction.java
- `Architectural Decision: Event-time on measuredAt` --rationale_for--> `WatermarkStrategyFactory.forVitals()`  [EXTRACTED]
  CLAUDE.md → med-flink/src/main/java/com/healthcare/vitals/source/WatermarkStrategyFactory.java
- `Production Changes Made (HIPAA + MDC + Observability)` --references--> `MDC vitalId/wardId Context in flatMap()`  [EXTRACTED]
  notes/prod_changes_made.txt → med-flink/src/main/java/com/healthcare/vitals/operator/AnomalyDetectionFunction.java

## Hyperedges (group relationships)
- **End-to-End Flink Pipeline: Source to Sink** — kafkasourcefactory_create, watermarkstrategyfactory_forvitals, anomalydetectionfunction_class, kafkasinkfactory_create, topicnames_vitals_raw, topicnames_alert_triggered [EXTRACTED 0.98]
- **DLQ Fault Tolerance: Deserialize Error Routing** — faulttolerantdeserializer_deserialize, faulttolerantdeserializer_sendtodlq, faulttolerantdeserializer_dlq_producer, topicnames_vitals_dlq [EXTRACTED 0.97]
- **Observability Stack: Prometheus + Loki + Grafana** — notes_prometheus_reporter, notes_loki_promtail, notes_grafana_dashboards, notes_dockerfile_flink, notes_log4j2_json_template [EXTRACTED 0.95]

## Communities

### Community 0 - "Anomaly Detection Logic"
Cohesion: 0.1
Nodes (28): AnomalyDetectionFunction.buildAlert(), AnomalyDetectionFunction.checkHeartRate(), AnomalyDetectionFunction.checkRespiratoryRate(), AnomalyDetectionFunction.checkSpO2(), AnomalyDetectionFunction.checkSystolicBP(), AnomalyDetectionFunction.checkTemperature(), AnomalyDetectionFunction.flatMap(), MDC vitalId/wardId Context in flatMap() (+20 more)

### Community 1 - "Core Pipeline Concepts"
Cohesion: 0.12
Nodes (24): AnomalyDetectionFunction (RichFlatMapFunction), AlertEvent Avro Schema (output), PatientVitals Avro Schema (input), Architectural Decision: DLQ on Deserialize Error, Architectural Decision: Event-time on measuredAt, Architectural Decision: Nullable Vital Fields in Avro, Architectural Decision: Stateless Anomaly Detection, NICE NG51 / NEWS2 Clinical Thresholds (+16 more)

### Community 2 - "Anomaly Detection Tests"
Cohesion: 0.14
Nodes (3): AnomalyDetectionFunctionTest, ListCollector, VitalsCustomizer

### Community 3 - "Job Config Builder"
Cohesion: 0.12
Nodes (4): Builder, JobConfig.fromEnvironment(), JobConfig, VitalsAnomalyDetectionJob (Job Entry Point)

### Community 4 - "DLQ Deserializer Tests"
Cohesion: 0.22
Nodes (2): FaultTolerantDeserializerTest, ListCollector

### Community 5 - "Fault Tolerant Deserializer"
Cohesion: 0.24
Nodes (2): FaultTolerantDeserializer, KafkaSourceFactory

### Community 6 - "AnomalyDetectionFunction Class"
Cohesion: 0.42
Nodes (1): AnomalyDetectionFunction

### Community 7 - "DLQ Error Routing Flow"
Cohesion: 0.24
Nodes (10): FaultTolerantDeserializer.deserialize(), DLQ KafkaProducer (lazy-initialized), MDC Kafka Metadata Context in deserialize(), FaultTolerantDeserializer.sendToDlq(), Dockerfile.flink (custom Flink image with Prometheus plugin), Grafana Dashboards (ops-health + diagnostics), log4j2 JsonTemplateLayout with Async Loggers, Loki + Promtail Log Aggregation (+2 more)

### Community 8 - "Job Entry Point"
Cohesion: 0.6
Nodes (1): VitalsAnomalyDetectionJob

### Community 9 - "Watermark Strategy"
Cohesion: 0.5
Nodes (1): WatermarkStrategyFactory

### Community 10 - "Kafka Sink Factory"
Cohesion: 0.5
Nodes (1): KafkaSinkFactory

### Community 11 - "Topic Names Config"
Cohesion: 0.67
Nodes (1): TopicNames

### Community 12 - "Project Structure"
Cohesion: 0.67
Nodes (3): med-flink Subproject (Flink Streaming Job), med-infra Subproject (Kafka Infrastructure), Medical Anomaly Detection Project

### Community 13 - "Schema Registry Config"
Cohesion: 1.0
Nodes (2): Architectural Decision: FULL_TRANSITIVE Schema Compatibility, Confluent Schema Registry

### Community 14 - "Kafka KRaft Cluster"
Cohesion: 1.0
Nodes (2): Apache Kafka Cluster (3-broker KRaft), Architectural Decision: KRaft (no ZooKeeper)

### Community 15 - "Pipeline Architecture Docs"
Cohesion: 1.0
Nodes (1): Flink Pipeline Architecture

### Community 16 - "Kafka Topics"
Cohesion: 1.0
Nodes (1): Kafka Topic Configuration

### Community 17 - "Fault Tolerance Design"
Cohesion: 1.0
Nodes (1): Fault Tolerance and DLQ Design

### Community 18 - "Flink Runtime"
Cohesion: 1.0
Nodes (1): Apache Flink 1.19.1 Runtime

## Knowledge Gaps
- **25 isolated node(s):** `med-infra Subproject (Kafka Infrastructure)`, `med-flink Subproject (Flink Streaming Job)`, `Flink Pipeline Architecture`, `NICE NG51 / NEWS2 Clinical Thresholds`, `Kafka Topic Configuration` (+20 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **Thin community `Schema Registry Config`** (2 nodes): `Architectural Decision: FULL_TRANSITIVE Schema Compatibility`, `Confluent Schema Registry`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Kafka KRaft Cluster`** (2 nodes): `Apache Kafka Cluster (3-broker KRaft)`, `Architectural Decision: KRaft (no ZooKeeper)`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Pipeline Architecture Docs`** (1 nodes): `Flink Pipeline Architecture`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Kafka Topics`** (1 nodes): `Kafka Topic Configuration`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Fault Tolerance Design`** (1 nodes): `Fault Tolerance and DLQ Design`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Flink Runtime`** (1 nodes): `Apache Flink 1.19.1 Runtime`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `AnomalyDetectionFunctionTest (35+ unit tests)` connect `Anomaly Detection Logic` to `Core Pipeline Concepts`?**
  _High betweenness centrality (0.095) - this node is a cross-community bridge._
- **Why does `ListCollector Test Helper (in-memory output collector)` connect `Anomaly Detection Logic` to `DLQ Deserializer Tests`?**
  _High betweenness centrality (0.082) - this node is a cross-community bridge._
- **Why does `ListCollector` connect `DLQ Deserializer Tests` to `Anomaly Detection Logic`?**
  _High betweenness centrality (0.080) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `Production Readiness TODO List` (e.g. with `Production Upgrade Checklist` and `Production Changes Made (HIPAA + MDC + Observability)`) actually correct?**
  _`Production Readiness TODO List` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `med-infra Subproject (Kafka Infrastructure)`, `med-flink Subproject (Flink Streaming Job)`, `Flink Pipeline Architecture` to the rest of the system?**
  _25 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Anomaly Detection Logic` be split into smaller, more focused modules?**
  _Cohesion score 0.1 - nodes in this community are weakly interconnected._
- **Should `Core Pipeline Concepts` be split into smaller, more focused modules?**
  _Cohesion score 0.12 - nodes in this community are weakly interconnected._