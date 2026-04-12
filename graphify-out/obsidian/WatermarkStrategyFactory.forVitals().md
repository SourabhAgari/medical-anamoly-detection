---
source_file: "med-flink/src/main/java/com/healthcare/vitals/source/WatermarkStrategyFactory.java"
type: "code"
community: "Core Pipeline Concepts"
location: "line 24"
tags:
  - graphify/code
  - graphify/EXTRACTED
  - community/Core_Pipeline_Concepts
---

# WatermarkStrategyFactory.forVitals()

## Connections
- [[Architectural Decision Event-time on measuredAt]] - `rationale_for` [EXTRACTED]
- [[Late Data Side Output Missing (30s records silently dropped)]] - `references` [EXTRACTED]
- [[PatientVitals Avro Schema (input)]] - `shares_data_with` [EXTRACTED]
- [[VitalsAnomalyDetectionJob.buildPipeline()]] - `calls` [EXTRACTED]
- [[WatermarkStrategyFactory_1]] - `references` [EXTRACTED]

#graphify/code #graphify/EXTRACTED #community/Core_Pipeline_Concepts