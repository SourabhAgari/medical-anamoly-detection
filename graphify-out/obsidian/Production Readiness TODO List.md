---
source_file: "notes/prod_conisderations.txt"
type: "document"
community: "Anomaly Detection Logic"
tags:
  - graphify/document
  - graphify/EXTRACTED
  - community/Anomaly_Detection_Logic
---

# Production Readiness TODO List

## Connections
- [[AT_LEAST_ONCE Sink Duplicate Alert Risk]] - `references` [EXTRACTED]
- [[Alert Time Uses Wall Clock Instead of Event Time]] - `references` [EXTRACTED]
- [[Checkpoint Storage on Local Docker Volume Risk]] - `references` [EXTRACTED]
- [[Double.NaN Vital Test Gap (sensor malfunction masking)]] - `references` [EXTRACTED]
- [[HIPAA PHI Disclosure Risk (patientId in logs)]] - `references` [EXTRACTED]
- [[Kafka TLSSSL Missing (cleartext vitals)]] - `references` [EXTRACTED]
- [[Late Data Side Output Missing (30s records silently dropped)]] - `references` [EXTRACTED]
- [[Non-deterministic alertId Deduplication Issue]] - `references` [EXTRACTED]
- [[Production Changes Made (HIPAA + MDC + Observability)]] - `conceptually_related_to` [INFERRED]
- [[Production Upgrade Checklist]] - `semantically_similar_to` [INFERRED]
- [[RocksDB State Backend Upgrade (scalability)]] - `references` [EXTRACTED]

#graphify/document #graphify/EXTRACTED #community/Anomaly_Detection_Logic