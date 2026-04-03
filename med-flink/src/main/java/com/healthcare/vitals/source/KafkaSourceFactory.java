package com.healthcare.vitals.source;

import com.healthcare.vitals.avro.PatientVitals;
import com.healthcare.vitals.job.JobConfig;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.formats.avro.registry.confluent.ConfluentRegistryAvroDeserializationSchema;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Builds a type-safe {@link KafkaSource} for {@link PatientVitals} Avro records.
 *
 * <p>Wraps the Confluent Avro deserializer in a fault-tolerant
 * {@link KafkaRecordDeserializationSchema} that catches deserialization errors
 * (e.g., plain-JSON records, wrong schema, corrupt bytes) and skips the bad
 * record with a structured log — instead of crashing the entire Flink job.
 *
 * <p>In production: route bad bytes to the DLQ topic rather than silently skipping.
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
            // Fault-tolerant wrapper — bad records are logged and skipped, not crash the job
            .setDeserializer(new FaultTolerantDeserializer(avroDeserializer))
            .build();
    }

    // ── Inner deserializer ────────────────────────────────────────────────────

    /**
     * Wraps any {@link KafkaRecordDeserializationSchema} with skip-on-error semantics.
     *
     * <p>A record is skipped (not emitted) when deserialization fails. The failure is
     * logged at ERROR level with topic/partition/offset so it can be tracked and replayed
     * from the DLQ if needed.
     *
     * <p>This prevents one malformed record (e.g., plain JSON produced by accident via
     * Redpanda Console or a misconfigured producer) from restarting the entire job.
     */
    private static class FaultTolerantDeserializer
            implements KafkaRecordDeserializationSchema<PatientVitals> {

        private static final long serialVersionUID = 1L;
        private final ConfluentRegistryAvroDeserializationSchema<PatientVitals> delegate;

        FaultTolerantDeserializer(
                ConfluentRegistryAvroDeserializationSchema<PatientVitals> delegate) {
            this.delegate = delegate;
        }

        @Override
        public void deserialize(ConsumerRecord<byte[], byte[]> record,
                                Collector<PatientVitals> out) throws IOException {
            try {
                delegate.deserialize(record.value(), out);
            } catch (Exception e) {
                // Log full context so the bad record can be located and replayed
                LOG.error(
                    "DESERIALIZATION FAILED — skipping record. " +
                    "topic={} partition={} offset={} key={} valueBytes={} error={}",
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.key() != null ? new String(record.key()) : "null",
                    record.value() != null ? record.value().length : 0,
                    e.getMessage()
                );
                // TODO: produce raw bytes to medical.vitals.dlq for replay
                // Not emitting to out = record is skipped, job continues
            }
        }

        @Override
        public TypeInformation<PatientVitals> getProducedType() {
            return TypeInformation.of(PatientVitals.class);
        }
    }
}