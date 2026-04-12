---
type: community
cohesion: 0.24
members: 10
---

# DLQ Error Routing Flow

**Cohesion:** 0.24 - loosely connected
**Members:** 10 nodes

## Members
- [[DLQ KafkaProducer (lazy-initialized)]] - code - med-flink/src/main/java/com/healthcare/vitals/source/KafkaSourceFactory.java
- [[Dockerfile.flink (custom Flink image with Prometheus plugin)]] - document - notes/prod_changes_made.txt
- [[FaultTolerantDeserializer.deserialize()]] - code - med-flink/src/main/java/com/healthcare/vitals/source/KafkaSourceFactory.java
- [[FaultTolerantDeserializer.sendToDlq()]] - code - med-flink/src/main/java/com/healthcare/vitals/source/KafkaSourceFactory.java
- [[Grafana Dashboards (ops-health + diagnostics)]] - document - notes/prod_changes_made.txt
- [[Loki + Promtail Log Aggregation]] - document - notes/prod_changes_made.txt
- [[MDC Kafka Metadata Context in deserialize()]] - code - med-flink/src/main/java/com/healthcare/vitals/source/KafkaSourceFactory.java
- [[Production Changes Made (HIPAA + MDC + Observability)]] - document - notes/prod_changes_made.txt
- [[Prometheus Metrics Reporter (observability)]] - document - notes/prod_changes_made.txt
- [[log4j2 JsonTemplateLayout with Async Loggers]] - document - notes/prod_changes_made.txt

## Live Query (requires Dataview plugin)

```dataview
TABLE source_file, type FROM #community/DLQ_Error_Routing_Flow
SORT file.name ASC
```

## Connections to other communities
- 3 edges to [[_COMMUNITY_Anomaly Detection Logic]]
- 1 edge to [[_COMMUNITY_Core Pipeline Concepts]]

## Top bridge nodes
- [[Production Changes Made (HIPAA + MDC + Observability)]] - degree 8, connects to 1 community
- [[MDC Kafka Metadata Context in deserialize()]] - degree 3, connects to 1 community
- [[FaultTolerantDeserializer.sendToDlq()]] - degree 3, connects to 1 community