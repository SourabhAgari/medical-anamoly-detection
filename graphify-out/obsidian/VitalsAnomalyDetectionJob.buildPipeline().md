---
source_file: "med-flink/src/main/java/com/healthcare/vitals/job/VitalsAnomalyDetectionJob.java"
type: "code"
community: "Core Pipeline Concepts"
location: "line 86"
tags:
  - graphify/code
  - graphify/EXTRACTED
  - community/Core_Pipeline_Concepts
---

# VitalsAnomalyDetectionJob.buildPipeline()

## Connections
- [[AnomalyDetectionFunction (RichFlatMapFunction)]] - `calls` [EXTRACTED]
- [[KafkaSinkFactory.create()]] - `calls` [EXTRACTED]
- [[KafkaSourceFactory.create()]] - `calls` [EXTRACTED]
- [[Sink Key Strategy patientId bytes]] - `semantically_similar_to` [INFERRED]
- [[Topic medical.alert.triggered]] - `references` [EXTRACTED]
- [[Topic medical.vitals.raw]] - `references` [EXTRACTED]
- [[WatermarkStrategyFactory.forVitals()]] - `calls` [EXTRACTED]

#graphify/code #graphify/EXTRACTED #community/Core_Pipeline_Concepts