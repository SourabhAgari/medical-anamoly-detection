---
type: community
cohesion: 0.14
members: 24
---

# Anomaly Detection Tests

**Cohesion:** 0.14 - loosely connected
**Members:** 24 nodes

## Members
- [[.alert_hasCorrectPatientAndVitalId()]] - code - med-flink/src/test/java/com/healthcare/vitals/operator/AnomalyDetectionFunctionTest.java
- [[.allNullVitals_noAlerts()]] - code - med-flink/src/test/java/com/healthcare/vitals/operator/AnomalyDetectionFunctionTest.java
- [[.buildVitals()]] - code - med-flink/src/test/java/com/healthcare/vitals/operator/AnomalyDetectionFunctionTest.java
- [[.close()_1]] - code - med-flink/src/test/java/com/healthcare/vitals/operator/AnomalyDetectionFunctionTest.java
- [[.collect()_1]] - code - med-flink/src/test/java/com/healthcare/vitals/operator/AnomalyDetectionFunctionTest.java
- [[.criticallyLowSpO2_firesCriticalAlert()]] - code - med-flink/src/test/java/com/healthcare/vitals/operator/AnomalyDetectionFunctionTest.java
- [[.customize()]] - code - med-flink/src/test/java/com/healthcare/vitals/operator/AnomalyDetectionFunctionTest.java
- [[.highHeartRate_firesHighAlert()]] - code - med-flink/src/test/java/com/healthcare/vitals/operator/AnomalyDetectionFunctionTest.java
- [[.highRespiratoryRate_firesHighAlert()]] - code - med-flink/src/test/java/com/healthcare/vitals/operator/AnomalyDetectionFunctionTest.java
- [[.highSystolicBP_firesHighAlert()]] - code - med-flink/src/test/java/com/healthcare/vitals/operator/AnomalyDetectionFunctionTest.java
- [[.highTemperature_firesMediumAlert()]] - code - med-flink/src/test/java/com/healthcare/vitals/operator/AnomalyDetectionFunctionTest.java
- [[.lowHeartRate_firesCriticalAlert()]] - code - med-flink/src/test/java/com/healthcare/vitals/operator/AnomalyDetectionFunctionTest.java
- [[.lowRespiratoryRate_firesCriticalAlert()]] - code - med-flink/src/test/java/com/healthcare/vitals/operator/AnomalyDetectionFunctionTest.java
- [[.lowSystolicBP_firesCriticalAlert()]] - code - med-flink/src/test/java/com/healthcare/vitals/operator/AnomalyDetectionFunctionTest.java
- [[.lowTemperature_firesHighAlert()]] - code - med-flink/src/test/java/com/healthcare/vitals/operator/AnomalyDetectionFunctionTest.java
- [[.multipleAbnormalVitals_firesMultipleAlerts()]] - code - med-flink/src/test/java/com/healthcare/vitals/operator/AnomalyDetectionFunctionTest.java
- [[.normalHeartRate_noAlert()]] - code - med-flink/src/test/java/com/healthcare/vitals/operator/AnomalyDetectionFunctionTest.java
- [[.normalSpO2_noAlert()]] - code - med-flink/src/test/java/com/healthcare/vitals/operator/AnomalyDetectionFunctionTest.java
- [[.nullHeartRate_noAlert()]] - code - med-flink/src/test/java/com/healthcare/vitals/operator/AnomalyDetectionFunctionTest.java
- [[.setUp()_1]] - code - med-flink/src/test/java/com/healthcare/vitals/operator/AnomalyDetectionFunctionTest.java
- [[AnomalyDetectionFunctionTest]] - code - med-flink/src/test/java/com/healthcare/vitals/operator/AnomalyDetectionFunctionTest.java
- [[AnomalyDetectionFunctionTest.java]] - code - med-flink/src/test/java/com/healthcare/vitals/operator/AnomalyDetectionFunctionTest.java
- [[ListCollector_1]] - code - med-flink/src/test/java/com/healthcare/vitals/operator/AnomalyDetectionFunctionTest.java
- [[VitalsCustomizer]] - code - med-flink/src/test/java/com/healthcare/vitals/operator/AnomalyDetectionFunctionTest.java

## Live Query (requires Dataview plugin)

```dataview
TABLE source_file, type FROM #community/Anomaly_Detection_Tests
SORT file.name ASC
```
