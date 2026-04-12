---
source_file: "med-flink/src/test/java/com/healthcare/vitals/source/FaultTolerantDeserializerTest.java"
type: "code"
community: "DLQ Deserializer Tests"
location: "L29"
tags:
  - graphify/code
  - graphify/EXTRACTED
  - community/DLQ_Deserializer_Tests
---

# FaultTolerantDeserializerTest

## Connections
- [[.badBytes()]] - `method` [EXTRACTED]
- [[.capturedDlqRecord()]] - `method` [EXTRACTED]
- [[.consumerRecord()]] - `method` [EXTRACTED]
- [[.corruptRecord_dlqHeaderContainsErrorClassAndMessage()]] - `method` [EXTRACTED]
- [[.corruptRecord_dlqHeaderContainsSourcePartitionAndOffset()]] - `method` [EXTRACTED]
- [[.corruptRecord_dlqHeaderContainsSourceTopic()]] - `method` [EXTRACTED]
- [[.corruptRecord_isNotEmittedToMainStream()]] - `method` [EXTRACTED]
- [[.corruptRecord_rawBytesAreSentToDlqTopic()]] - `method` [EXTRACTED]
- [[.dlqProducerThrows_exceptionIsSwallowedAndJobContinues()]] - `method` [EXTRACTED]
- [[.header()]] - `method` [EXTRACTED]
- [[.longErrorMessage_isTruncatedTo500CharsInHeader()]] - `method` [EXTRACTED]
- [[.mixedStream_validRecordsPassThroughAndCorruptGoToDlq()]] - `method` [EXTRACTED]
- [[.nullRecordValue_isRoutedToDlq()]] - `method` [EXTRACTED]
- [[.setUp()]] - `method` [EXTRACTED]
- [[.validRecord_doesNotTouchDlqProducer()]] - `method` [EXTRACTED]
- [[.validRecord_isEmittedToMainStream()]] - `method` [EXTRACTED]
- [[FaultTolerantDeserializerTest.java]] - `contains` [EXTRACTED]

#graphify/code #graphify/EXTRACTED #community/DLQ_Deserializer_Tests