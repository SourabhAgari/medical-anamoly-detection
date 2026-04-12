---
type: community
cohesion: 0.12
members: 24
---

# Job Config Builder

**Cohesion:** 0.12 - loosely connected
**Members:** 24 nodes

## Members
- [[.JobConfig()]] - code - med-flink/src/main/java/com/healthcare/vitals/job/JobConfig.java
- [[.autoOffsetReset()]] - code - med-flink/src/main/java/com/healthcare/vitals/job/JobConfig.java
- [[.bootstrapServers()]] - code - med-flink/src/main/java/com/healthcare/vitals/job/JobConfig.java
- [[.build()]] - code - med-flink/src/main/java/com/healthcare/vitals/job/JobConfig.java
- [[.checkpointIntervalMs()]] - code - med-flink/src/main/java/com/healthcare/vitals/job/JobConfig.java
- [[.consumerGroup()]] - code - med-flink/src/main/java/com/healthcare/vitals/job/JobConfig.java
- [[.env()]] - code - med-flink/src/main/java/com/healthcare/vitals/job/JobConfig.java
- [[.fromEnvironment()]] - code - med-flink/src/main/java/com/healthcare/vitals/job/JobConfig.java
- [[.getAutoOffsetReset()]] - code - med-flink/src/main/java/com/healthcare/vitals/job/JobConfig.java
- [[.getBootstrapServers()]] - code - med-flink/src/main/java/com/healthcare/vitals/job/JobConfig.java
- [[.getCheckpointIntervalMs()]] - code - med-flink/src/main/java/com/healthcare/vitals/job/JobConfig.java
- [[.getConsumerGroup()]] - code - med-flink/src/main/java/com/healthcare/vitals/job/JobConfig.java
- [[.getParallelism()]] - code - med-flink/src/main/java/com/healthcare/vitals/job/JobConfig.java
- [[.getSchemaRegistryUrl()]] - code - med-flink/src/main/java/com/healthcare/vitals/job/JobConfig.java
- [[.parallelism()]] - code - med-flink/src/main/java/com/healthcare/vitals/job/JobConfig.java
- [[.requireEnv()]] - code - med-flink/src/main/java/com/healthcare/vitals/job/JobConfig.java
- [[.schemaRegistryUrl()]] - code - med-flink/src/main/java/com/healthcare/vitals/job/JobConfig.java
- [[.toSchemaRegistryProperties()]] - code - med-flink/src/main/java/com/healthcare/vitals/job/JobConfig.java
- [[.toString()]] - code - med-flink/src/main/java/com/healthcare/vitals/job/JobConfig.java
- [[Builder]] - code - med-flink/src/main/java/com/healthcare/vitals/job/JobConfig.java
- [[JobConfig]] - code - med-flink/src/main/java/com/healthcare/vitals/job/JobConfig.java
- [[JobConfig.fromEnvironment()]] - code - med-flink/src/main/java/com/healthcare/vitals/job/JobConfig.java
- [[JobConfig.java]] - code - med-flink/src/main/java/com/healthcare/vitals/job/JobConfig.java
- [[VitalsAnomalyDetectionJob (Job Entry Point)]] - code - med-flink/src/main/java/com/healthcare/vitals/job/VitalsAnomalyDetectionJob.java

## Live Query (requires Dataview plugin)

```dataview
TABLE source_file, type FROM #community/Job_Config_Builder
SORT file.name ASC
```
