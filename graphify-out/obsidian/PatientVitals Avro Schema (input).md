---
source_file: "CLAUDE.md"
type: "document"
community: "Core Pipeline Concepts"
location: "Data Schemas section"
tags:
  - graphify/document
  - graphify/EXTRACTED
  - community/Core_Pipeline_Concepts
---

# PatientVitals Avro Schema (input)

## Connections
- [[AnomalyDetectionFunction (RichFlatMapFunction)]] - `shares_data_with` [EXTRACTED]
- [[Architectural Decision Nullable Vital Fields in Avro]] - `rationale_for` [EXTRACTED]
- [[Topic medical.vitals.raw]] - `shares_data_with` [INFERRED]
- [[WatermarkStrategyFactory.forVitals()]] - `shares_data_with` [EXTRACTED]

#graphify/document #graphify/EXTRACTED #community/Core_Pipeline_Concepts