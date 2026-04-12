---
type: community
cohesion: 0.24
members: 11
---

# Fault Tolerant Deserializer

**Cohesion:** 0.24 - loosely connected
**Members:** 11 nodes

## Members
- [[.FaultTolerantDeserializer()]] - code - med-flink/src/main/java/com/healthcare/vitals/source/KafkaSourceFactory.java
- [[.KafkaSourceFactory()]] - code - med-flink/src/main/java/com/healthcare/vitals/source/KafkaSourceFactory.java
- [[.create()]] - code - med-flink/src/main/java/com/healthcare/vitals/source/KafkaSourceFactory.java
- [[.createDlqProducer()]] - code - med-flink/src/main/java/com/healthcare/vitals/source/KafkaSourceFactory.java
- [[.deserialize()]] - code - med-flink/src/main/java/com/healthcare/vitals/source/KafkaSourceFactory.java
- [[.getProducedType()]] - code - med-flink/src/main/java/com/healthcare/vitals/source/KafkaSourceFactory.java
- [[.sendToDlq()]] - code - med-flink/src/main/java/com/healthcare/vitals/source/KafkaSourceFactory.java
- [[.truncate()]] - code - med-flink/src/main/java/com/healthcare/vitals/source/KafkaSourceFactory.java
- [[FaultTolerantDeserializer]] - code - med-flink/src/main/java/com/healthcare/vitals/source/KafkaSourceFactory.java
- [[KafkaSourceFactory]] - code - med-flink/src/main/java/com/healthcare/vitals/source/KafkaSourceFactory.java
- [[KafkaSourceFactory.java]] - code - med-flink/src/main/java/com/healthcare/vitals/source/KafkaSourceFactory.java

## Live Query (requires Dataview plugin)

```dataview
TABLE source_file, type FROM #community/Fault_Tolerant_Deserializer
SORT file.name ASC
```
