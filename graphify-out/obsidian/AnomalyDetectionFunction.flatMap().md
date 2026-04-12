---
source_file: "med-flink/src/main/java/com/healthcare/vitals/operator/AnomalyDetectionFunction.java"
type: "code"
community: "Anomaly Detection Logic"
location: "line 64"
tags:
  - graphify/code
  - graphify/EXTRACTED
  - community/Anomaly_Detection_Logic
---

# AnomalyDetectionFunction.flatMap()

## Connections
- [[AnomalyDetectionFunction.checkHeartRate()]] - `calls` [EXTRACTED]
- [[AnomalyDetectionFunction.checkRespiratoryRate()]] - `calls` [EXTRACTED]
- [[AnomalyDetectionFunction.checkSpO2()]] - `calls` [EXTRACTED]
- [[AnomalyDetectionFunction.checkSystolicBP()]] - `calls` [EXTRACTED]
- [[AnomalyDetectionFunction.checkTemperature()]] - `calls` [EXTRACTED]
- [[HIPAA PHI Disclosure Risk (patientId in logs)]] - `references` [EXTRACTED]
- [[MDC vitalIdwardId Context in flatMap()]] - `references` [EXTRACTED]

#graphify/code #graphify/EXTRACTED #community/Anomaly_Detection_Logic