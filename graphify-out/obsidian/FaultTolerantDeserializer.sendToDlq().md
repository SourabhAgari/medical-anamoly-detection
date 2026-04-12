---
source_file: "med-flink/src/main/java/com/healthcare/vitals/source/KafkaSourceFactory.java"
type: "code"
community: "DLQ Error Routing Flow"
location: "line 138"
tags:
  - graphify/code
  - graphify/EXTRACTED
  - community/DLQ_Error_Routing_Flow
---

# FaultTolerantDeserializer.sendToDlq()

## Connections
- [[DLQ KafkaProducer (lazy-initialized)]] - `calls` [EXTRACTED]
- [[FaultTolerantDeserializer.deserialize()]] - `calls` [EXTRACTED]
- [[Topic medical.vitals.dlq]] - `references` [EXTRACTED]

#graphify/code #graphify/EXTRACTED #community/DLQ_Error_Routing_Flow