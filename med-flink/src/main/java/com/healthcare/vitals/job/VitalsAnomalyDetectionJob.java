package com.healthcare.vitals.job;

import com.healthcare.vitals.avro.AlertEvent;
import com.healthcare.vitals.avro.PatientVitals;
import com.healthcare.vitals.config.TopicNames;
import com.healthcare.vitals.operator.AnomalyDetectionFunction;
import com.healthcare.vitals.sink.KafkaSinkFactory;
import com.healthcare.vitals.source.KafkaSourceFactory;
import com.healthcare.vitals.source.WatermarkStrategyFactory;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for the Medical Vitals Anomaly Detection Flink job.
 *
 * <p>Pipeline:
 * <pre>
 *   medical.vitals.raw (Kafka)
 *       │  [Avro deserialize via Schema Registry]
 *       ▼
 *   PatientVitals stream (with event-time watermarks)
 *       │  keyBy(patientId)
 *       ▼
 *   AnomalyDetectionFunction (threshold rules)
 *       │
 *       ▼
 *   AlertEvent stream
 *       │  [Avro serialize via Schema Registry]
 *       ▼
 *   medical.alert.triggered (Kafka)
 * </pre>
 *
 * <p>Configuration is driven entirely by environment variables — see {@link JobConfig}.
 * To run locally: {@code mvn exec:java -Plocal-run -Dexec.mainClass=com.healthcare.vitals.job.VitalsAnomalyDetectionJob}
 */
public class VitalsAnomalyDetectionJob {

    private static final Logger LOG = LoggerFactory.getLogger(VitalsAnomalyDetectionJob.class);

    public static void main(String[] args) throws Exception {
        JobConfig config = JobConfig.fromEnvironment();
        LOG.info("Starting VitalsAnomalyDetectionJob with config: {}", config);

        StreamExecutionEnvironment env = buildEnvironment(config);
        buildPipeline(env, config);

        env.execute("Medical Vitals Anomaly Detection");
    }

    // ── Environment setup ─────────────────────────────────────────────────────

    static StreamExecutionEnvironment buildEnvironment(JobConfig config) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(config.getParallelism());

        // ── Checkpointing ──────────────────────────────────────────────────
        env.enableCheckpointing(config.getCheckpointIntervalMs(), CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(config.getCheckpointIntervalMs() / 2);
        env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);
        // Retain checkpoint on cancellation so job can resume from last good state
        env.getCheckpointConfig().setExternalizedCheckpointCleanup(
            CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);

        // ── State backend (HashMap = in-memory for local; switch to RocksDB in prod) ──
        env.setStateBackend(new HashMapStateBackend());

        // ── Restart strategy: fixed-delay, 3 attempts, 10s between attempts ──
        env.setRestartStrategy(
            RestartStrategies.fixedDelayRestart(3, Time.seconds(10)));

        return env;
    }

    // ── Pipeline assembly ─────────────────────────────────────────────────────

    static void buildPipeline(StreamExecutionEnvironment env, JobConfig config) {
        // ── Source: medical.vitals.raw ────────────────────────────────────
        KafkaSource<PatientVitals> source =
            KafkaSourceFactory.create(config, TopicNames.VITALS_RAW);

        DataStream<PatientVitals> vitalsStream = env
            .fromSource(source, WatermarkStrategyFactory.forVitals(), "Kafka: vitals.raw")
            .name("kafka-source-vitals-raw")
            .uid("kafka-source-vitals-raw");   // stable UID for savepoint compatibility

        // ── Anomaly detection ─────────────────────────────────────────────
        DataStream<AlertEvent> alertStream = vitalsStream
            .keyBy(vitals -> vitals.getPatientId().toString())
            .flatMap(new AnomalyDetectionFunction())
            .name("anomaly-detection")
            .uid("anomaly-detection");

        // ── Sink: medical.alert.triggered ─────────────────────────────────
        KafkaSink<AlertEvent> sink = KafkaSinkFactory.create(config);

        alertStream
            .sinkTo(sink)
            .name("kafka-sink-alert-triggered")
            .uid("kafka-sink-alert-triggered");

        LOG.info("Pipeline assembled: {} → anomaly-detection → {}",
                 TopicNames.VITALS_RAW, TopicNames.ALERT_TRIGGERED);
    }
}