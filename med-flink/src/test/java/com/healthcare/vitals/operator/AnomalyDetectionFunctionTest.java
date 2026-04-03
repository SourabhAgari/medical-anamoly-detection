package com.healthcare.vitals.operator;

import com.healthcare.vitals.avro.AlertEvent;
import com.healthcare.vitals.avro.AlertSeverity;
import com.healthcare.vitals.avro.PatientVitals;
import com.healthcare.vitals.avro.VitalRule;
import org.apache.flink.util.Collector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnomalyDetectionFunctionTest {

    private AnomalyDetectionFunction function;
    private ListCollector<AlertEvent> collector;

    @BeforeEach
    void setUp() {
        function  = new AnomalyDetectionFunction();
        collector = new ListCollector<>();
    }

    // ── Heart rate ─────────────────────────────────────────────────────────

    @Test
    void highHeartRate_firesHighAlert() throws Exception {
        PatientVitals vitals = buildVitals(v -> v.setHeartRate(125.0));
        function.flatMap(vitals, collector);

        assertThat(collector.items).hasSize(1);
        AlertEvent alert = collector.items.get(0);
        assertThat(alert.getRuleTriggered()).isEqualTo(VitalRule.HIGH_HEART_RATE);
        assertThat(alert.getSeverity()).isEqualTo(AlertSeverity.HIGH);
        assertThat(alert.getObservedValue()).isEqualTo(125.0);
    }

    @Test
    void lowHeartRate_firesCriticalAlert() throws Exception {
        PatientVitals vitals = buildVitals(v -> v.setHeartRate(35.0));
        function.flatMap(vitals, collector);

        assertThat(collector.items).hasSize(1);
        assertThat(collector.items.get(0).getRuleTriggered()).isEqualTo(VitalRule.LOW_HEART_RATE);
        assertThat(collector.items.get(0).getSeverity()).isEqualTo(AlertSeverity.CRITICAL);
    }

    @Test
    void normalHeartRate_noAlert() throws Exception {
        PatientVitals vitals = buildVitals(v -> v.setHeartRate(75.0));
        function.flatMap(vitals, collector);
        assertThat(collector.items).isEmpty();
    }

    @Test
    void nullHeartRate_noAlert() throws Exception {
        PatientVitals vitals = buildVitals(v -> v.setHeartRate(null));
        function.flatMap(vitals, collector);
        assertThat(collector.items).isEmpty();
    }

    // ── SpO2 ──────────────────────────────────────────────────────────────

    @Test
    void criticallyLowSpO2_firesCriticalAlert() throws Exception {
        PatientVitals vitals = buildVitals(v -> v.setSpo2(85.0));
        function.flatMap(vitals, collector);

        assertThat(collector.items).hasSize(1);
        assertThat(collector.items.get(0).getRuleTriggered()).isEqualTo(VitalRule.LOW_SPO2);
        assertThat(collector.items.get(0).getSeverity()).isEqualTo(AlertSeverity.CRITICAL);
    }

    @Test
    void normalSpO2_noAlert() throws Exception {
        PatientVitals vitals = buildVitals(v -> v.setSpo2(98.0));
        function.flatMap(vitals, collector);
        assertThat(collector.items).isEmpty();
    }

    // ── Blood pressure ────────────────────────────────────────────────────

    @Test
    void highSystolicBP_firesHighAlert() throws Exception {
        PatientVitals vitals = buildVitals(v -> v.setSystolicBp(190.0));
        function.flatMap(vitals, collector);

        assertThat(collector.items).hasSize(1);
        assertThat(collector.items.get(0).getRuleTriggered()).isEqualTo(VitalRule.HIGH_SYSTOLIC_BP);
        assertThat(collector.items.get(0).getSeverity()).isEqualTo(AlertSeverity.HIGH);
    }

    @Test
    void lowSystolicBP_firesCriticalAlert() throws Exception {
        PatientVitals vitals = buildVitals(v -> v.setSystolicBp(70.0));
        function.flatMap(vitals, collector);

        assertThat(collector.items).hasSize(1);
        assertThat(collector.items.get(0).getRuleTriggered()).isEqualTo(VitalRule.LOW_SYSTOLIC_BP);
        assertThat(collector.items.get(0).getSeverity()).isEqualTo(AlertSeverity.CRITICAL);
    }

    // ── Temperature ───────────────────────────────────────────────────────

    @Test
    void highTemperature_firesMediumAlert() throws Exception {
        PatientVitals vitals = buildVitals(v -> v.setTemperatureCelsius(40.1));
        function.flatMap(vitals, collector);

        assertThat(collector.items).hasSize(1);
        assertThat(collector.items.get(0).getRuleTriggered()).isEqualTo(VitalRule.HIGH_TEMPERATURE);
        assertThat(collector.items.get(0).getSeverity()).isEqualTo(AlertSeverity.MEDIUM);
    }

    @Test
    void lowTemperature_firesHighAlert() throws Exception {
        PatientVitals vitals = buildVitals(v -> v.setTemperatureCelsius(34.0));
        function.flatMap(vitals, collector);

        assertThat(collector.items).hasSize(1);
        assertThat(collector.items.get(0).getRuleTriggered()).isEqualTo(VitalRule.LOW_TEMPERATURE);
        assertThat(collector.items.get(0).getSeverity()).isEqualTo(AlertSeverity.HIGH);
    }

    // ── Respiratory rate ──────────────────────────────────────────────────

    @Test
    void highRespiratoryRate_firesHighAlert() throws Exception {
        PatientVitals vitals = buildVitals(v -> v.setRespiratoryRate(35.0));
        function.flatMap(vitals, collector);

        assertThat(collector.items).hasSize(1);
        assertThat(collector.items.get(0).getRuleTriggered()).isEqualTo(VitalRule.HIGH_RESPIRATORY_RATE);
        assertThat(collector.items.get(0).getSeverity()).isEqualTo(AlertSeverity.HIGH);
    }

    @Test
    void lowRespiratoryRate_firesCriticalAlert() throws Exception {
        PatientVitals vitals = buildVitals(v -> v.setRespiratoryRate(5.0));
        function.flatMap(vitals, collector);

        assertThat(collector.items).hasSize(1);
        assertThat(collector.items.get(0).getRuleTriggered()).isEqualTo(VitalRule.LOW_RESPIRATORY_RATE);
        assertThat(collector.items.get(0).getSeverity()).isEqualTo(AlertSeverity.CRITICAL);
    }

    // ── Multi-alert ───────────────────────────────────────────────────────

    @Test
    void multipleAbnormalVitals_firesMultipleAlerts() throws Exception {
        PatientVitals vitals = buildVitals(v -> {
            v.setHeartRate(130.0);   // HIGH_HEART_RATE
            v.setSpo2(88.0);         // LOW_SPO2
            v.setRespiratoryRate(35.0); // HIGH_RESPIRATORY_RATE
        });
        function.flatMap(vitals, collector);

        assertThat(collector.items).hasSize(3);
        assertThat(collector.items)
            .extracting(AlertEvent::getRuleTriggered)
            .containsExactlyInAnyOrder(
                VitalRule.HIGH_HEART_RATE,
                VitalRule.LOW_SPO2,
                VitalRule.HIGH_RESPIRATORY_RATE);
    }

    @Test
    void allNullVitals_noAlerts() throws Exception {
        PatientVitals vitals = buildVitals(v -> {});
        function.flatMap(vitals, collector);
        assertThat(collector.items).isEmpty();
    }

    // ── Alert fields ──────────────────────────────────────────────────────

    @Test
    void alert_hasCorrectPatientAndVitalId() throws Exception {
        PatientVitals vitals = buildVitals(v -> v.setSpo2(80.0));
        function.flatMap(vitals, collector);

        AlertEvent alert = collector.items.get(0);
        assertThat(alert.getPatientId().toString()).isEqualTo("patient-001");
        assertThat(alert.getVitalId().toString()).isEqualTo("vital-xyz");
        assertThat(alert.getWardId().toString()).isEqualTo("ward-ICU");
        assertThat(alert.getAlertId().toString()).isNotBlank();
        assertThat(alert.getMessage().toString()).contains("patient-001");
    }

    // ── Test helpers ──────────────────────────────────────────────────────

    @FunctionalInterface
    interface VitalsCustomizer {
        void customize(PatientVitals.Builder builder);
    }

    private PatientVitals buildVitals(VitalsCustomizer customizer) {
        PatientVitals.Builder builder = PatientVitals.newBuilder()
            .setVitalId("vital-xyz")
            .setPatientId("patient-001")
            .setWardId("ward-ICU")
            .setDeviceId("device-42")
            .setMeasuredAt(Instant.now())
            .setHeartRate(null)
            .setSpo2(null)
            .setSystolicBp(null)
            .setDiastolicBp(null)
            .setTemperatureCelsius(null)
            .setRespiratoryRate(null);
        customizer.customize(builder);
        return builder.build();
    }

    /** Simple in-memory collector for testing FlatMapFunction outputs. */
    static class ListCollector<T> implements Collector<T> {
        final List<T> items = new ArrayList<>();

        @Override public void collect(T record) { items.add(record); }
        @Override public void close() {}
    }
}
