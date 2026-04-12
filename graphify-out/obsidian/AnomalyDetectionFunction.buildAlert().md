---
source_file: "med-flink/src/main/java/com/healthcare/vitals/operator/AnomalyDetectionFunction.java"
type: "code"
community: "Anomaly Detection Logic"
location: "line 173"
tags:
  - graphify/code
  - graphify/EXTRACTED
  - community/Anomaly_Detection_Logic
---

# AnomalyDetectionFunction.buildAlert()

## Connections
- [[Alert Time Uses Wall Clock Instead of Event Time]] - `references` [EXTRACTED]
- [[AlertEvent Avro Schema (output)]] - `shares_data_with` [EXTRACTED]
- [[AlertSeverity Enum (LOW, MEDIUM, HIGH, CRITICAL)]] - `references` [EXTRACTED]
- [[AnomalyDetectionFunction.checkHeartRate()]] - `calls` [EXTRACTED]
- [[AnomalyDetectionFunction.checkRespiratoryRate()]] - `calls` [EXTRACTED]
- [[AnomalyDetectionFunction.checkSpO2()]] - `calls` [EXTRACTED]
- [[AnomalyDetectionFunction.checkSystolicBP()]] - `calls` [EXTRACTED]
- [[AnomalyDetectionFunction.checkTemperature()]] - `calls` [EXTRACTED]
- [[Non-deterministic alertId Deduplication Issue]] - `references` [EXTRACTED]
- [[VitalRule Enum (HIGH_HEART_RATE, LOW_SPO2, etc.)]] - `references` [EXTRACTED]

#graphify/code #graphify/EXTRACTED #community/Anomaly_Detection_Logic