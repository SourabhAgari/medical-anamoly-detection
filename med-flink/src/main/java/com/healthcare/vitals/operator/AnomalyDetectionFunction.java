package com.healthcare.vitals.operator;

import com.healthcare.vitals.avro.AlertEvent;
import com.healthcare.vitals.avro.AlertSeverity;
import com.healthcare.vitals.avro.PatientVitals;
import com.healthcare.vitals.avro.VitalRule;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Threshold-based anomaly detection for patient vitals.
 *
 * <p>For each incoming {@link PatientVitals} record, every vital sign is independently
 * evaluated against clinical thresholds. Multiple alerts can fire from a single reading
 * (e.g., simultaneous low SpO2 and high respiratory rate). Null-valued vitals (sensor
 * did not report that reading) are silently skipped — never cause a NullPointerException.
 *
 * <p>Threshold reference: NICE guidelines NG51 / NEWS2 early-warning score.
 *
 * <pre>
 * Vital            | Rule                   | Threshold      | Severity
 * ─────────────────┼────────────────────────┼────────────────┼──────────
 * Heart Rate       | HIGH_HEART_RATE        | > 120 bpm      | HIGH
 *                  | LOW_HEART_RATE         | < 40 bpm       | CRITICAL
 * SpO2             | LOW_SPO2               | < 90 %         | CRITICAL
 * Systolic BP      | HIGH_SYSTOLIC_BP       | > 180 mmHg     | HIGH
 *                  | LOW_SYSTOLIC_BP        | < 80 mmHg      | CRITICAL
 * Temperature      | HIGH_TEMPERATURE       | > 39.5 °C      | MEDIUM
 *                  | LOW_TEMPERATURE        | < 35.0 °C      | HIGH
 * Respiratory Rate | HIGH_RESPIRATORY_RATE  | > 30 /min      | HIGH
 *                  | LOW_RESPIRATORY_RATE   | < 8 /min       | CRITICAL
 * </pre>
 */
public class AnomalyDetectionFunction extends RichFlatMapFunction<PatientVitals, AlertEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(AnomalyDetectionFunction.class);

    // ── Thresholds ───────────────────────────────────────────────────────────
    private static final double HR_HIGH          = 120.0;
    private static final double HR_LOW           = 40.0;
    private static final double SPO2_CRITICAL    = 90.0;
    private static final double SBP_HIGH         = 180.0;
    private static final double SBP_LOW          = 80.0;
    private static final double TEMP_HIGH        = 39.5;
    private static final double TEMP_LOW         = 35.0;
    private static final double RR_HIGH          = 30.0;
    private static final double RR_LOW           = 8.0;

    @Override
    public void open(Configuration parameters) {
        LOG.info("AnomalyDetectionFunction initialised on task slot {}",
                 getRuntimeContext().getIndexOfThisSubtask());
    }

    @Override
    public void flatMap(PatientVitals vitals, Collector<AlertEvent> out) {
        LOG.debug("Processing vitals for patient={} vitalId={}", vitals.getPatientId(), vitals.getVitalId());

        checkHeartRate(vitals).ifPresent(out::collect);
        checkSpO2(vitals).ifPresent(out::collect);
        checkSystolicBP(vitals).ifPresent(out::collect);
        checkTemperature(vitals).ifPresent(out::collect);
        checkRespiratoryRate(vitals).ifPresent(out::collect);
    }

    // ── Individual vital checks ───────────────────────────────────────────────

    private Optional<AlertEvent> checkHeartRate(PatientVitals v) {
        Double hr = v.getHeartRate();
        if (hr == null) return Optional.empty();

        if (hr < HR_LOW) {
            return Optional.of(buildAlert(v, VitalRule.LOW_HEART_RATE, AlertSeverity.CRITICAL,
                hr, HR_LOW,
                String.format("CRITICAL: Heart rate %.1f bpm is below %.1f bpm (bradycardia) for patient %s",
                    hr, HR_LOW, v.getPatientId())));
        }
        if (hr > HR_HIGH) {
            return Optional.of(buildAlert(v, VitalRule.HIGH_HEART_RATE, AlertSeverity.HIGH,
                hr, HR_HIGH,
                String.format("HIGH: Heart rate %.1f bpm exceeds %.1f bpm (tachycardia) for patient %s",
                    hr, HR_HIGH, v.getPatientId())));
        }
        return Optional.empty();
    }

    private Optional<AlertEvent> checkSpO2(PatientVitals v) {
        Double spo2 = v.getSpo2();
        if (spo2 == null) return Optional.empty();

        if (spo2 < SPO2_CRITICAL) {
            return Optional.of(buildAlert(v, VitalRule.LOW_SPO2, AlertSeverity.CRITICAL,
                spo2, SPO2_CRITICAL,
                String.format("CRITICAL: SpO2 %.1f%% is below %.1f%% for patient %s",
                    spo2, SPO2_CRITICAL, v.getPatientId())));
        }
        return Optional.empty();
    }

    private Optional<AlertEvent> checkSystolicBP(PatientVitals v) {
        Double sbp = v.getSystolicBp();
        if (sbp == null) return Optional.empty();

        if (sbp < SBP_LOW) {
            return Optional.of(buildAlert(v, VitalRule.LOW_SYSTOLIC_BP, AlertSeverity.CRITICAL,
                sbp, SBP_LOW,
                String.format("CRITICAL: Systolic BP %.1f mmHg is below %.1f mmHg (hypotension) for patient %s",
                    sbp, SBP_LOW, v.getPatientId())));
        }
        if (sbp > SBP_HIGH) {
            return Optional.of(buildAlert(v, VitalRule.HIGH_SYSTOLIC_BP, AlertSeverity.HIGH,
                sbp, SBP_HIGH,
                String.format("HIGH: Systolic BP %.1f mmHg exceeds %.1f mmHg (hypertensive crisis) for patient %s",
                    sbp, SBP_HIGH, v.getPatientId())));
        }
        return Optional.empty();
    }

    private Optional<AlertEvent> checkTemperature(PatientVitals v) {
        Double temp = v.getTemperatureCelsius();
        if (temp == null) return Optional.empty();

        if (temp < TEMP_LOW) {
            return Optional.of(buildAlert(v, VitalRule.LOW_TEMPERATURE, AlertSeverity.HIGH,
                temp, TEMP_LOW,
                String.format("HIGH: Temperature %.1f°C is below %.1f°C (hypothermia) for patient %s",
                    temp, TEMP_LOW, v.getPatientId())));
        }
        if (temp > TEMP_HIGH) {
            return Optional.of(buildAlert(v, VitalRule.HIGH_TEMPERATURE, AlertSeverity.MEDIUM,
                temp, TEMP_HIGH,
                String.format("MEDIUM: Temperature %.1f°C exceeds %.1f°C (hyperpyrexia) for patient %s",
                    temp, TEMP_HIGH, v.getPatientId())));
        }
        return Optional.empty();
    }

    private Optional<AlertEvent> checkRespiratoryRate(PatientVitals v) {
        Double rr = v.getRespiratoryRate();
        if (rr == null) return Optional.empty();

        if (rr < RR_LOW) {
            return Optional.of(buildAlert(v, VitalRule.LOW_RESPIRATORY_RATE, AlertSeverity.CRITICAL,
                rr, RR_LOW,
                String.format("CRITICAL: Respiratory rate %.1f/min is below %.1f/min (bradypnea) for patient %s",
                    rr, RR_LOW, v.getPatientId())));
        }
        if (rr > RR_HIGH) {
            return Optional.of(buildAlert(v, VitalRule.HIGH_RESPIRATORY_RATE, AlertSeverity.HIGH,
                rr, RR_HIGH,
                String.format("HIGH: Respiratory rate %.1f/min exceeds %.1f/min for patient %s",
                    rr, RR_HIGH, v.getPatientId())));
        }
        return Optional.empty();
    }

    // ── Builder helper ────────────────────────────────────────────────────────

    private AlertEvent buildAlert(PatientVitals vitals,
                                  VitalRule rule,
                                  AlertSeverity severity,
                                  double observed,
                                  double threshold,
                                  String message) {
        AlertEvent alert = AlertEvent.newBuilder()
            .setAlertId(UUID.randomUUID().toString())
            .setVitalId(vitals.getVitalId().toString())
            .setPatientId(vitals.getPatientId().toString())
            .setWardId(vitals.getWardId().toString())
            .setAlertTime(Instant.now())
            .setRuleTriggered(rule)
            .setSeverity(severity)
            .setObservedValue(observed)
            .setThresholdValue(threshold)
            .setMessage(message)
            .build();

        LOG.info("Alert fired: rule={} severity={} patient={} observed={} threshold={}",
                 rule, severity, vitals.getPatientId(), observed, threshold);
        return alert;
    }
}