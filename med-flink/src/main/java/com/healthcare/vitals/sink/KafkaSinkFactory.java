package com.healthcare.vitals.sink;

import com.healthcare.vitals.avro.AlertEvent;
import com.healthcare.vitals.config.TopicNames;
import com.healthcare.vitals.job.JobConfig;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.formats.avro.registry.confluent.ConfluentRegistryAvroSerializationSchema;

import java.nio.charset.StandardCharsets;

/**
 * Builds a type-safe {@link KafkaSink} that serializes {@link AlertEvent} records
 * to Avro with Confluent Schema Registry wire format and writes to the alert topic.
 *
 * <p>Key strategy: patientId bytes — all alerts for the same patient land on the same
 * partition, preserving per-patient ordering for downstream notification services.
 *
 * <p>Delivery guarantee: AT_LEAST_ONCE for local dev.
 * Switch to EXACTLY_ONCE with a transactional ID prefix for production to prevent
 * duplicate alerts during Flink task restarts (requires Kafka transaction support).
 */
public final class KafkaSinkFactory {

    // Subject name follows Confluent TopicNameStrategy: <topic>-value
    private static final String ALERT_SUBJECT = TopicNames.ALERT_TRIGGERED + "-value";

    private KafkaSinkFactory() {}

    public static KafkaSink<AlertEvent> create(JobConfig config) {
        ConfluentRegistryAvroSerializationSchema<AlertEvent> avroSerializer =
            ConfluentRegistryAvroSerializationSchema.forSpecific(
                AlertEvent.class,
                ALERT_SUBJECT,
                config.getSchemaRegistryUrl(),
                config.toSchemaRegistryProperties()
            );

        KafkaRecordSerializationSchema<AlertEvent> recordSerializer =
            KafkaRecordSerializationSchema.<AlertEvent>builder()
                .setTopic(TopicNames.ALERT_TRIGGERED)
                .setValueSerializationSchema(avroSerializer)
                // Key by patientId: per-patient ordering + partition affinity
                .setKeySerializationSchema(
                    alert -> alert.getPatientId().toString().getBytes(StandardCharsets.UTF_8))
                .build();

        return KafkaSink.<AlertEvent>builder()
            .setBootstrapServers(config.getBootstrapServers())
            .setRecordSerializer(recordSerializer)
            // Use AT_LEAST_ONCE locally; switch to EXACTLY_ONCE in prod with transactional ID
            .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
            .build();
    }
}