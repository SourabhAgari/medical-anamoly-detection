package com.healthcare.vitals.source;

import com.healthcare.vitals.avro.PatientVitals;
import com.healthcare.vitals.config.TopicNames;
import org.apache.flink.formats.avro.registry.confluent.ConfluentRegistryAvroDeserializationSchema;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FaultTolerantDeserializerTest {

    @Mock
    ConfluentRegistryAvroDeserializationSchema<PatientVitals> delegate;

    @Mock
    KafkaProducer<byte[], byte[]> dlqProducer;

    KafkaSourceFactory.FaultTolerantDeserializer deserializer;
    ListCollector<PatientVitals> collector;

    @BeforeEach
    void setUp() {
        deserializer = new KafkaSourceFactory.FaultTolerantDeserializer(delegate, "localhost:9092", dlqProducer);
        collector = new ListCollector<>();
    }

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    void validRecord_isEmittedToMainStream() throws Exception {
        byte[] validBytes = "valid-avro-bytes".getBytes(StandardCharsets.UTF_8);
        ConsumerRecord<byte[], byte[]> record = consumerRecord("medical.vitals.raw", 0, 42L, null, validBytes);
        PatientVitals vitals = mock(PatientVitals.class);

        doAnswer(inv -> {
            Collector<PatientVitals> out = inv.getArgument(1);
            out.collect(vitals);
            return null;
        }).when(delegate).deserialize(eq(validBytes), any());

        deserializer.deserialize(record, collector);

        assertThat(collector.items).containsExactly(vitals);
    }

    @Test
    void validRecord_doesNotTouchDlqProducer() throws Exception {
        byte[] validBytes = "valid".getBytes(StandardCharsets.UTF_8);
        ConsumerRecord<byte[], byte[]> record = consumerRecord("medical.vitals.raw", 0, 1L, null, validBytes);

        deserializer.deserialize(record, collector);

        verifyNoInteractions(dlqProducer);
    }

    // ── DLQ routing ───────────────────────────────────────────────────────────

    @Test
    void corruptRecord_isNotEmittedToMainStream() throws Exception {
        byte[] badBytes = "not-avro".getBytes(StandardCharsets.UTF_8);
        ConsumerRecord<byte[], byte[]> record = consumerRecord("medical.vitals.raw", 2, 99L, null, badBytes);
        doThrow(new IOException("bad magic byte")).when(delegate).deserialize(eq(badBytes), any());

        deserializer.deserialize(record, collector);

        assertThat(collector.items).isEmpty();
    }

    @Test
    void corruptRecord_rawBytesAreSentToDlqTopic() throws Exception {
        byte[] badBytes = "corrupt-payload".getBytes(StandardCharsets.UTF_8);
        byte[] key = "patient-001".getBytes(StandardCharsets.UTF_8);
        ConsumerRecord<byte[], byte[]> record = consumerRecord("medical.vitals.raw", 1, 7L, key, badBytes);
        doThrow(new RuntimeException("schema mismatch")).when(delegate).deserialize(eq(badBytes), any());

        deserializer.deserialize(record, collector);

        ProducerRecord<byte[], byte[]> dlqRecord = capturedDlqRecord();
        assertThat(dlqRecord.topic()).isEqualTo(TopicNames.VITALS_DLQ);
        assertThat(dlqRecord.value()).isEqualTo(badBytes);
        assertThat(dlqRecord.key()).isEqualTo(key);
    }

    @Test
    void corruptRecord_dlqHeaderContainsSourceTopic() throws Exception {
        ConsumerRecord<byte[], byte[]> record = consumerRecord("medical.vitals.raw", 3, 55L, null, badBytes());
        doThrow(new IllegalArgumentException("unknown field")).when(delegate).deserialize(any(), any());

        deserializer.deserialize(record, collector);

        assertThat(header(capturedDlqRecord(), "source.topic")).isEqualTo("medical.vitals.raw");
    }

    @Test
    void corruptRecord_dlqHeaderContainsSourcePartitionAndOffset() throws Exception {
        ConsumerRecord<byte[], byte[]> record = consumerRecord("medical.vitals.raw", 3, 55L, null, badBytes());
        doThrow(new IllegalArgumentException("bad")).when(delegate).deserialize(any(), any());

        deserializer.deserialize(record, collector);

        ProducerRecord<byte[], byte[]> dlqRecord = capturedDlqRecord();
        assertThat(header(dlqRecord, "source.partition")).isEqualTo("3");
        assertThat(header(dlqRecord, "source.offset")).isEqualTo("55");
    }

    @Test
    void corruptRecord_dlqHeaderContainsErrorClassAndMessage() throws Exception {
        ConsumerRecord<byte[], byte[]> record = consumerRecord("medical.vitals.raw", 0, 1L, null, badBytes());
        doThrow(new IllegalArgumentException("unknown field")).when(delegate).deserialize(any(), any());

        deserializer.deserialize(record, collector);

        ProducerRecord<byte[], byte[]> dlqRecord = capturedDlqRecord();
        assertThat(header(dlqRecord, "error.class")).isEqualTo(IllegalArgumentException.class.getName());
        assertThat(header(dlqRecord, "error.message")).isEqualTo("unknown field");
    }

    @Test
    void longErrorMessage_isTruncatedTo500CharsInHeader() throws Exception {
        String longMessage = "x".repeat(600);
        ConsumerRecord<byte[], byte[]> record = consumerRecord("medical.vitals.raw", 0, 1L, null, badBytes());
        doThrow(new RuntimeException(longMessage)).when(delegate).deserialize(any(), any());

        deserializer.deserialize(record, collector);

        assertThat(header(capturedDlqRecord(), "error.message")).hasSize(500);
    }

    @Test
    void nullRecordValue_isRoutedToDlq() throws Exception {
        ConsumerRecord<byte[], byte[]> record = consumerRecord("medical.vitals.raw", 0, 10L, null, null);
        doThrow(new NullPointerException("null value")).when(delegate).deserialize(isNull(), any());

        deserializer.deserialize(record, collector);

        assertThat(collector.items).isEmpty();
        verify(dlqProducer).send(any(), any());
    }

    // ── DLQ producer failure resilience ───────────────────────────────────────

    @Test
    void dlqProducerThrows_exceptionIsSwallowedAndJobContinues() throws Exception {
        ConsumerRecord<byte[], byte[]> record = consumerRecord("medical.vitals.raw", 0, 1L, null, badBytes());
        doThrow(new IOException("avro error")).when(delegate).deserialize(any(), any());
        doThrow(new RuntimeException("broker down")).when(dlqProducer).send(any(), any());

        // Must not propagate — a DLQ failure should never kill the main pipeline
        deserializer.deserialize(record, collector);

        assertThat(collector.items).isEmpty();
    }

    // ── Multiple records ──────────────────────────────────────────────────────

    @Test
    void mixedStream_validRecordsPassThroughAndCorruptGoToDlq() throws Exception {
        byte[] goodBytes = "good".getBytes(StandardCharsets.UTF_8);
        byte[] badBytes  = "bad".getBytes(StandardCharsets.UTF_8);
        PatientVitals vitals = mock(PatientVitals.class);

        doAnswer(inv -> { ((Collector<PatientVitals>) inv.getArgument(1)).collect(vitals); return null; })
            .when(delegate).deserialize(eq(goodBytes), any());
        doThrow(new IOException("corrupt")).when(delegate).deserialize(eq(badBytes), any());

        deserializer.deserialize(consumerRecord("medical.vitals.raw", 0, 1L, null, goodBytes), collector);
        deserializer.deserialize(consumerRecord("medical.vitals.raw", 0, 2L, null, badBytes),  collector);
        deserializer.deserialize(consumerRecord("medical.vitals.raw", 0, 3L, null, goodBytes), collector);

        assertThat(collector.items).containsExactly(vitals, vitals);
        verify(dlqProducer, times(1)).send(any(), any());
    }

    // ── Test helpers ──────────────────────────────────────────────────────────

    private static byte[] badBytes() {
        return "bad-bytes".getBytes(StandardCharsets.UTF_8);
    }

    private static ConsumerRecord<byte[], byte[]> consumerRecord(
            String topic, int partition, long offset, byte[] key, byte[] value) {
        return new ConsumerRecord<>(topic, partition, offset, key, value);
    }

    @SuppressWarnings("unchecked")
    private ProducerRecord<byte[], byte[]> capturedDlqRecord() {
        ArgumentCaptor<ProducerRecord<byte[], byte[]>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(dlqProducer).send(captor.capture(), any());
        return captor.getValue();
    }

    private static String header(ProducerRecord<?, ?> record, String key) {
        Header h = record.headers().lastHeader(key);
        assertThat(h).as("header '%s' not found in DLQ record", key).isNotNull();
        return new String(h.value(), StandardCharsets.UTF_8);
    }

    static class ListCollector<T> implements Collector<T> {
        final List<T> items = new ArrayList<>();
        @Override public void collect(T record) { items.add(record); }
        @Override public void close() {}
    }
}
