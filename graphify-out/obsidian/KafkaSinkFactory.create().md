---
source_file: "med-flink/src/main/java/com/healthcare/vitals/sink/KafkaSinkFactory.java"
type: "code"
community: "Core Pipeline Concepts"
location: "line 31"
tags:
  - graphify/code
  - graphify/EXTRACTED
  - community/Core_Pipeline_Concepts
---

# KafkaSinkFactory.create()

## Connections
- [[AT_LEAST_ONCE Delivery Guarantee (Sink)]] - `references` [EXTRACTED]
- [[AlertEvent Avro Schema (output)]] - `shares_data_with` [EXTRACTED]
- [[JobConfig (Environment-based Configuration)]] - `shares_data_with` [EXTRACTED]
- [[KafkaSinkFactory_1]] - `references` [EXTRACTED]
- [[Sink Key Strategy patientId bytes]] - `references` [EXTRACTED]
- [[Topic medical.alert.triggered]] - `references` [EXTRACTED]
- [[VitalsAnomalyDetectionJob.buildPipeline()]] - `calls` [EXTRACTED]

#graphify/code #graphify/EXTRACTED #community/Core_Pipeline_Concepts