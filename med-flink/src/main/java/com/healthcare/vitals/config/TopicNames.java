package com.healthcare.vitals.config;

/** Single source of truth for all Kafka topic names used by this application. */
public final class TopicNames {

    public static final String VITALS_RAW       = "medical.vitals.raw";
    public static final String ALERT_TRIGGERED  = "medical.alert.triggered";
    public static final String VITALS_DLQ       = "medical.vitals.dlq";

    private TopicNames() {}
}
