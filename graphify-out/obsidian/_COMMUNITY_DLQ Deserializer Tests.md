---
type: community
cohesion: 0.22
members: 21
---

# DLQ Deserializer Tests

**Cohesion:** 0.22 - loosely connected
**Members:** 21 nodes

## Members
- [[.badBytes()]] - code - med-flink/src/test/java/com/healthcare/vitals/source/FaultTolerantDeserializerTest.java
- [[.capturedDlqRecord()]] - code - med-flink/src/test/java/com/healthcare/vitals/source/FaultTolerantDeserializerTest.java
- [[.close()]] - code - med-flink/src/test/java/com/healthcare/vitals/source/FaultTolerantDeserializerTest.java
- [[.collect()]] - code - med-flink/src/test/java/com/healthcare/vitals/source/FaultTolerantDeserializerTest.java
- [[.consumerRecord()]] - code - med-flink/src/test/java/com/healthcare/vitals/source/FaultTolerantDeserializerTest.java
- [[.corruptRecord_dlqHeaderContainsErrorClassAndMessage()]] - code - med-flink/src/test/java/com/healthcare/vitals/source/FaultTolerantDeserializerTest.java
- [[.corruptRecord_dlqHeaderContainsSourcePartitionAndOffset()]] - code - med-flink/src/test/java/com/healthcare/vitals/source/FaultTolerantDeserializerTest.java
- [[.corruptRecord_dlqHeaderContainsSourceTopic()]] - code - med-flink/src/test/java/com/healthcare/vitals/source/FaultTolerantDeserializerTest.java
- [[.corruptRecord_isNotEmittedToMainStream()]] - code - med-flink/src/test/java/com/healthcare/vitals/source/FaultTolerantDeserializerTest.java
- [[.corruptRecord_rawBytesAreSentToDlqTopic()]] - code - med-flink/src/test/java/com/healthcare/vitals/source/FaultTolerantDeserializerTest.java
- [[.dlqProducerThrows_exceptionIsSwallowedAndJobContinues()]] - code - med-flink/src/test/java/com/healthcare/vitals/source/FaultTolerantDeserializerTest.java
- [[.header()]] - code - med-flink/src/test/java/com/healthcare/vitals/source/FaultTolerantDeserializerTest.java
- [[.longErrorMessage_isTruncatedTo500CharsInHeader()]] - code - med-flink/src/test/java/com/healthcare/vitals/source/FaultTolerantDeserializerTest.java
- [[.mixedStream_validRecordsPassThroughAndCorruptGoToDlq()]] - code - med-flink/src/test/java/com/healthcare/vitals/source/FaultTolerantDeserializerTest.java
- [[.nullRecordValue_isRoutedToDlq()]] - code - med-flink/src/test/java/com/healthcare/vitals/source/FaultTolerantDeserializerTest.java
- [[.setUp()]] - code - med-flink/src/test/java/com/healthcare/vitals/source/FaultTolerantDeserializerTest.java
- [[.validRecord_doesNotTouchDlqProducer()]] - code - med-flink/src/test/java/com/healthcare/vitals/source/FaultTolerantDeserializerTest.java
- [[.validRecord_isEmittedToMainStream()]] - code - med-flink/src/test/java/com/healthcare/vitals/source/FaultTolerantDeserializerTest.java
- [[FaultTolerantDeserializerTest]] - code - med-flink/src/test/java/com/healthcare/vitals/source/FaultTolerantDeserializerTest.java
- [[FaultTolerantDeserializerTest.java]] - code - med-flink/src/test/java/com/healthcare/vitals/source/FaultTolerantDeserializerTest.java
- [[ListCollector]] - code - med-flink/src/test/java/com/healthcare/vitals/source/FaultTolerantDeserializerTest.java

## Live Query (requires Dataview plugin)

```dataview
TABLE source_file, type FROM #community/DLQ_Deserializer_Tests
SORT file.name ASC
```

## Connections to other communities
- 1 edge to [[_COMMUNITY_Anomaly Detection Logic]]

## Top bridge nodes
- [[ListCollector]] - degree 4, connects to 1 community