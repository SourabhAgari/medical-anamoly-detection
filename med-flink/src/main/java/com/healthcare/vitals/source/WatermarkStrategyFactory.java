package com.healthcare.vitals.source;

import com.healthcare.vitals.avro.PatientVitals;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;

import java.time.Duration;

/**
 * Watermark strategy for the vitals stream.
 *
 * <p>Out-of-orderness: 30 seconds — covers network jitter and buffering from bedside devices
 * that typically send readings every 5–30 seconds.
 *
 * <p>Idleness timeout: 5 minutes — prevents a silent partition (e.g., no patients in a ward)
 * from blocking the global watermark and stalling time-based operations downstream.
 */
public final class WatermarkStrategyFactory {

    private static final Duration OUT_OF_ORDERNESS = Duration.ofSeconds(30);
    private static final Duration IDLENESS_TIMEOUT  = Duration.ofMinutes(5);

    private WatermarkStrategyFactory() {}

    public static WatermarkStrategy<PatientVitals> forVitals() {
        return WatermarkStrategy
            .<PatientVitals>forBoundedOutOfOrderness(OUT_OF_ORDERNESS)
            // measuredAt is a timestamp-millis long (epoch ms) — extract directly
            .withTimestampAssigner((vitals, recordTs) -> vitals.getMeasuredAt().toEpochMilli())
            .withIdleness(IDLENESS_TIMEOUT);
    }
}