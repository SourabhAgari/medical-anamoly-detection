package com.healthcare.vitals.job;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Centralised job configuration loaded from environment variables.
 *
 * <p>Defaults are tuned for local development against the Docker Compose cluster.
 * In production override via the environment; no code changes required.
 */
public final class JobConfig {

    // ── Kafka ────────────────────────────────────────────────────────────────
    private final String bootstrapServers;
    private final String consumerGroup;
    private final String autoOffsetReset;

    // ── Schema Registry ──────────────────────────────────────────────────────
    private final String schemaRegistryUrl;

    // ── Flink ────────────────────────────────────────────────────────────────
    private final int    parallelism;
    private final long   checkpointIntervalMs;

    private JobConfig(Builder b) {
        this.bootstrapServers    = b.bootstrapServers;
        this.consumerGroup       = b.consumerGroup;
        this.autoOffsetReset     = b.autoOffsetReset;
        this.schemaRegistryUrl   = b.schemaRegistryUrl;
        this.parallelism         = b.parallelism;
        this.checkpointIntervalMs = b.checkpointIntervalMs;
    }

    /** Load from environment variables; throw on missing required vars. */
    public static JobConfig fromEnvironment() {
        return new Builder()
            .bootstrapServers(requireEnv("KAFKA_BOOTSTRAP",
                    "localhost:9092,localhost:9093,localhost:9094"))
            .schemaRegistryUrl(env("SCHEMA_REGISTRY_URL", "http://localhost:8081"))
            .consumerGroup(env("KAFKA_CONSUMER_GROUP", "vitals-anomaly-detector"))
            .autoOffsetReset(env("KAFKA_AUTO_OFFSET_RESET", "latest"))
            .parallelism(Integer.parseInt(env("FLINK_PARALLELISM", "1")))
            .checkpointIntervalMs(Long.parseLong(
                    env("FLINK_CHECKPOINT_INTERVAL_MS", "30000")))
            .build();
    }

    /**
     * Returns the Map that Confluent's Schema Registry client expects.
     * Extend this method to add auth when connecting to Confluent Cloud.
     */
    public Map<String, String> toSchemaRegistryProperties() {
        Map<String, String> props = new HashMap<>();
        props.put("schema.registry.url", schemaRegistryUrl);
        // Confluent Cloud auth (uncomment in prod):
        // props.put("basic.auth.credentials.source", "USER_INFO");
        // props.put("basic.auth.user.info", System.getenv("SR_API_KEY") + ":" + System.getenv("SR_API_SECRET"));
        return props;
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public String getBootstrapServers()     { return bootstrapServers; }
    public String getConsumerGroup()        { return consumerGroup; }
    public String getAutoOffsetReset()      { return autoOffsetReset; }
    public String getSchemaRegistryUrl()    { return schemaRegistryUrl; }
    public int    getParallelism()          { return parallelism; }
    public long   getCheckpointIntervalMs() { return checkpointIntervalMs; }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private static String env(String name, String defaultValue) {
        String val = System.getenv(name);
        return (val != null && !val.isBlank()) ? val : defaultValue;
    }

    /**
     * Read an env var with a fallback default.
     * Use the no-default variant to make a variable truly required
     * by passing {@code null} as default (will throw if absent).
     */
    private static String requireEnv(String name, String defaultValue) {
        String val = System.getenv(name);
        if (val != null && !val.isBlank()) return val;
        if (defaultValue != null) return defaultValue;
        throw new IllegalStateException("Required environment variable not set: " + name);
    }

    @Override
    public String toString() {
        return "JobConfig{" +
               "bootstrapServers='" + bootstrapServers + '\'' +
               ", schemaRegistryUrl='" + schemaRegistryUrl + '\'' +
               ", consumerGroup='" + consumerGroup + '\'' +
               ", autoOffsetReset='" + autoOffsetReset + '\'' +
               ", parallelism=" + parallelism +
               ", checkpointIntervalMs=" + checkpointIntervalMs +
               '}';
    }

    // ── Builder ───────────────────────────────────────────────────────────────
    public static final class Builder {
        private String bootstrapServers;
        private String consumerGroup       = "vitals-anomaly-detector";
        private String autoOffsetReset     = "latest";
        private String schemaRegistryUrl   = "http://localhost:8081";
        private int parallelism         = 1;
        private long checkpointIntervalMs = 30_000L;

        public Builder bootstrapServers(String v)     { this.bootstrapServers    = Objects.requireNonNull(v); return this; }
        public Builder consumerGroup(String v)        { this.consumerGroup       = v; return this; }
        public Builder autoOffsetReset(String v)      { this.autoOffsetReset     = v; return this; }
        public Builder schemaRegistryUrl(String v)    { this.schemaRegistryUrl   = v; return this; }
        public Builder parallelism(int v)             { this.parallelism         = v; return this; }
        public Builder checkpointIntervalMs(long v)   { this.checkpointIntervalMs = v; return this; }
        public JobConfig build()                      { return new JobConfig(this); }
    }
}
