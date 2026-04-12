package com.healthcare.vitals.source;

import com.healthcare.vitals.avro.PatientVitals;
import com.healthcare.vitals.config.TopicNames;
import com.healthcare.vitals.job.JobConfig;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.formats.avro.registry.confluent.ConfluentRegistryAvroDeserializationSchema;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Builds a type-safe {@link KafkaSource} for {@link PatientVitals} Avro records.
 *
 * <p>Wraps the Confluent Avro deserializer in a fault-tolerant
 * {@link KafkaRecordDeserializationSchema} that catches deserialization errors
 * (e.g., plain-JSON records, wrong schema, corrupt bytes) and routes the bad
 * record to the DLQ topic ({@value TopicNames#VITALS_DLQ}) with error context
 * attached as Kafka headers — instead of crashing the entire Flink job.
 */
public final class KafkaSourceFactory {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaSourceFactory.class);

    private KafkaSourceFactory() {}

    public static KafkaSource<PatientVitals> create(JobConfig config, String topic) {
        ConfluentRegistryAvroDeserializationSchema<PatientVitals> avroDeserializer =
            ConfluentRegistryAvroDeserializationSchema.forSpecific(
                PatientVitals.class,
                config.getSchemaRegistryUrl(),
                config.toSchemaRegistryProperties()
            );

        OffsetsInitializer startOffset = "earliest".equalsIgnoreCase(config.getAutoOffsetReset())
            ? OffsetsInitializer.earliest()
            : OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST);

        return KafkaSource.<PatientVitals>builder()
            .setBootstrapServers(config.getBootstrapServers())
            .setTopics(topic)
            .setGroupId(config.getConsumerGroup())
            .setStartingOffsets(startOffset)
            .setDeserializer(new FaultTolerantDeserializer(avroDeserializer, config.getBootstrapServers()))
            .build();
    }

    // ── Inner deserializer ────────────────────────────────────────────────────

    /**
     * Wraps the Avro deserializer with DLQ-on-error semantics.
     *
     * <p>When deserialization fails the raw record bytes are forwarded to
     * {@value TopicNames#VITALS_DLQ} with the following Kafka headers so the
     * record can be identified and replayed without grepping logs:
     * <ul>
     *   <li>{@code source.topic} — original topic</li>
     *   <li>{@code source.partition} — original partition</li>
     *   <li>{@code source.offset} — original offset</li>
     *   <li>{@code error.class} — exception class name</li>
     *   <li>{@code error.message} — exception message (truncated to 500 chars)</li>
     * </ul>
     *
     * <p>The DLQ producer is lazily initialised on first error to avoid any
     * connection overhead on the happy path. It uses {@code acks=1} so a single
     * broker acknowledgement is sufficient — DLQ writes are best-effort and
     * should never block or crash the main pipeline.
     *
     * <p>Package-private (not private) to allow unit-testing with an injected producer.
     */
    static class FaultTolerantDeserializer
            implements KafkaRecordDeserializationSchema<PatientVitals> {

        private static final long serialVersionUID = 1L;

        private final ConfluentRegistryAvroDeserializationSchema<PatientVitals> delegate;
        private final String bootstrapServers;

        /** Lazily initialised; {@code transient} so it is not serialised by Flink. */
        private transient KafkaProducer<byte[], byte[]> dlqProducer;

        FaultTolerantDeserializer(
                ConfluentRegistryAvroDeserializationSchema<PatientVitals> delegate,
                String bootstrapServers) {
            this(delegate, bootstrapServers, null);
        }

        /** Package-private test constructor — accepts a pre-built producer to avoid real Kafka. */
        FaultTolerantDeserializer(
                ConfluentRegistryAvroDeserializationSchema<PatientVitals> delegate,
                String bootstrapServers,
                KafkaProducer<byte[], byte[]> dlqProducer) {
            this.delegate = delegate;
            this.bootstrapServers = bootstrapServers;
            this.dlqProducer = dlqProducer;
        }

        @Override
        public void deserialize(ConsumerRecord<byte[], byte[]> record,
                                Collector<PatientVitals> out) throws IOException {
            MDC.put("kafka.topic", record.topic());
            MDC.put("kafka.partition", String.valueOf(record.partition()));
            MDC.put("kafka.offset", String.valueOf(record.offset()));
            try {
                delegate.deserialize(record.value(), out);
            } catch (Exception e) {
                LOG.error(
                    "DESERIALIZATION FAILED — routing to DLQ. " +
                    "topic={} partition={} offset={} key={} valueBytes={} error={}",
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.key() != null ? new String(record.key(), StandardCharsets.UTF_8) : "null",
                    record.value() != null ? record.value().length : 0,
                    e.getMessage()
                );
                sendToDlq(record, e);
                // Not emitting to out — record is excluded from the main pipeline
            } finally {
                MDC.clear();
            }
        }

        private void sendToDlq(ConsumerRecord<byte[], byte[]> record, Exception cause) {
            if (dlqProducer == null) {
                dlqProducer = createDlqProducer();
            }
            try {
                ProducerRecord<byte[], byte[]> dlqRecord = new ProducerRecord<>(
                    TopicNames.VITALS_DLQ,
                    null,           // partition — let Kafka decide
                    record.key(),   // preserve original message key
                    record.value()  // raw bytes as-is for offline replay
                );
                dlqRecord.headers()
                    .add("source.topic",     record.topic().getBytes(StandardCharsets.UTF_8))
                    .add("source.partition", String.valueOf(record.partition()).getBytes(StandardCharsets.UTF_8))
                    .add("source.offset",    String.valueOf(record.offset()).getBytes(StandardCharsets.UTF_8))
                    .add("error.class",      cause.getClass().getName().getBytes(StandardCharsets.UTF_8))
                    .add("error.message",    truncate(cause.getMessage()).getBytes(StandardCharsets.UTF_8));

                dlqProducer.send(dlqRecord, (meta, ex) -> {
                    if (ex != null) {
                        LOG.error("Failed to write bad record to DLQ. " +
                                  "source.topic={} source.partition={} source.offset={} dlqError={}",
                                  record.topic(), record.partition(), record.offset(), ex.getMessage());
                    } else {
                        LOG.debug("Bad record forwarded to DLQ. dlq.partition={} dlq.offset={}",
                                  meta.partition(), meta.offset());
                    }
                });
            } catch (Exception ex) {
                LOG.error("Unexpected error while routing bad record to DLQ: {}", ex.getMessage());
            }
        }

        private KafkaProducer<byte[], byte[]> createDlqProducer() {
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
            props.put(ProducerConfig.ACKS_CONFIG, "1");
            props.put(ProducerConfig.RETRIES_CONFIG, "3");
            props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, "5000"); // don't stall the pipeline on broker issues
            return new KafkaProducer<>(props);
        }

        @Override
        public TypeInformation<PatientVitals> getProducedType() {
            return TypeInformation.of(PatientVitals.class);
        }

        private static String truncate(String s) {
            if (s == null) return "null";
            return s.length() > 500 ? s.substring(0, 500) : s;
        }
    }
}