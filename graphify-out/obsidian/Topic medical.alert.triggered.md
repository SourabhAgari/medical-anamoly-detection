---
source_file: "med-flink/src/main/java/com/healthcare/vitals/config/TopicNames.java"
type: "code"
community: "Core Pipeline Concepts"
location: "line 7"
tags:
  - graphify/code
  - graphify/EXTRACTED
  - community/Core_Pipeline_Concepts
---

# Topic: medical.alert.triggered

## Connections
- [[AlertEvent Avro Schema (output)]] - `shares_data_with` [INFERRED]
- [[KafkaSinkFactory.create()]] - `references` [EXTRACTED]
- [[TopicNames (Config Class)]] - `references` [EXTRACTED]
- [[VitalsAnomalyDetectionJob.buildPipeline()]] - `references` [EXTRACTED]

#graphify/code #graphify/EXTRACTED #community/Core_Pipeline_Concepts