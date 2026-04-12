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

# AlertEvent Avro Schema (output)

## Connections
- [[AnomalyDetectionFunction (RichFlatMapFunction)]] - `shares_data_with` [EXTRACTED]
- [[AnomalyDetectionFunction.buildAlert()]] - `shares_data_with` [EXTRACTED]
- [[KafkaSinkFactory.create()]] - `shares_data_with` [EXTRACTED]
- [[Topic medical.alert.triggered]] - `shares_data_with` [INFERRED]

#graphify/document #graphify/EXTRACTED #community/Core_Pipeline_Concepts