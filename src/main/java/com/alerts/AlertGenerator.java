package com.alerts;

import com.data_management.DataStorage;
import com.data_management.Patient;

/**
 * The {@code AlertGenerator} class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met. This class
 * relies on a {@link DataStorage} instance to access patient data and evaluate
 * it against specific health criteria.
 */
public class AlertGenerator {
    private DataStorage dataStorage;

    /**
     * Constructs an {@code AlertGenerator} with a specified {@code DataStorage}.
     * The {@code DataStorage} is used to retrieve patient data that this class
     * will monitor and evaluate.
     *
     * @param dataStorage the data storage system that provides access to patient
     *                    data
     */
    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    /**
     * Evaluates the specified patient's data to determine if any alert conditions
     * are met. If a condition is met, an alert is triggered via the
     * {@link #triggerAlert}
     * method. This method should define the specific conditions under which an
     * alert
     * will be triggered.
     *
     * @param patient the patient data to evaluate for alert conditions
     */
    public void evaluateData(Patient patient) {
        int patientId = patient.getPatientId();
        long now = System.currentTimeMillis();
        long tenMinutesAgo = now - 10 * 60 * 1000;

        List<PatientRecord> systolicRecords = dataStorage.getRecords(patientId, 0, now)
            .stream().filter(r -> r.getRecordType().equals("SystolicPressure")).toList();
        List<PatientRecord> diastolicRecords = dataStorage.getRecords(patientId, 0, now)
            .stream().filter(r -> r.getRecordType().equals("DiastolicPressure")).toList();
        List<PatientRecord> saturationRecords = dataStorage.getRecords(patientId, 0, now)
            .stream().filter(r -> r.getRecordType().equals("Saturation")).toList();
        List<PatientRecord> ecgRecords = dataStorage.getRecords(patientId, 0, now)
            .stream().filter(r -> r.getRecordType().equals("ECG")).toList();
        List<PatientRecord> alertRecords = dataStorage.getRecords(patientId, 0, now)
            .stream().filter(r -> r.getRecordType().equals("Alert")).toList();

        // 1. Blood Pressure Trend Alert
        checkTrend(patient, systolicRecords, "SystolicPressure");
        checkTrend(patient, diastolicRecords, "DiastolicPressure");

        // 2. Blood Pressure Critical Threshold
        for (PatientRecord r : systolicRecords) {
            if (r.getMeasurementValue() > 180 || r.getMeasurementValue() < 90) {
                triggerAlert(new Alert(String.valueOf(patientId), "CriticalSystolicPressure", r.getTimestamp()));
            }
        }
        for (PatientRecord r : diastolicRecords) {
            if (r.getMeasurementValue() > 120 || r.getMeasurementValue() < 60) {
                triggerAlert(new Alert(String.valueOf(patientId), "CriticalDiastolicPressure", r.getTimestamp()));
            }
        }

        // 3. Blood Saturation - Low Saturation Alert
        for (PatientRecord r : saturationRecords) {
            if (r.getMeasurementValue() < 92) {
                triggerAlert(new Alert(String.valueOf(patientId), "LowSaturation", r.getTimestamp()));
            }
        }

        // 4. Blood Saturation - Rapid Drop Alert (5% drop in 10 minutes)
        List<PatientRecord> recentSaturation = dataStorage.getRecords(patientId, tenMinutesAgo, now)
            .stream().filter(r -> r.getRecordType().equals("Saturation")).toList();
        if (recentSaturation.size() >= 2) {
            double first = recentSaturation.get(0).getMeasurementValue();
            double last = recentSaturation.get(recentSaturation.size() - 1).getMeasurementValue();
            if (first - last >= 5) {
                triggerAlert(new Alert(String.valueOf(patientId), "RapidSaturationDrop", now));
            }
        }

        // 5. Combined Hypotensive Hypoxemia Alert
        boolean lowSystolic = systolicRecords.stream().anyMatch(r -> r.getMeasurementValue() < 90);
        boolean lowSaturation = saturationRecords.stream().anyMatch(r -> r.getMeasurementValue() < 92);
        if (lowSystolic && lowSaturation) {
            triggerAlert(new Alert(String.valueOf(patientId), "HypotensiveHypoxemia", now));
        }

        // 6. ECG - Abnormal Peak Detection (sliding window)
        if (ecgRecords.size() > 10) {
            double avg = ecgRecords.stream()
                .mapToDouble(PatientRecord::getMeasurementValue).average().orElse(0);
            for (PatientRecord r : ecgRecords) {
                if (r.getMeasurementValue() > avg * 1.5) {
                    triggerAlert(new Alert(String.valueOf(patientId), "AbnormalECGPeak", r.getTimestamp()));
                    break;
                }
            }
        }

        // 7. Triggered Alert (nurse/patient button)
        for (PatientRecord r : alertRecords) {
            if (r.getMeasurementValue() == 1) {
                triggerAlert(new Alert(String.valueOf(patientId), "ManualAlert", r.getTimestamp()));
            }
        }
    }
    private void checkTrend(Patient patient, List<PatientRecord> records, String type) {
        if (records.size() < 3) return;
        for (int i = records.size() - 3; i < records.size() - 1; i++) {
            double diff1 = records.get(i+1).getMeasurementValue() - records.get(i).getMeasurementValue();
            double diff2 = records.get(i+2).getMeasurementValue() - records.get(i+1).getMeasurementValue();
            if (diff1 > 10 && diff2 > 10) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()), type + "IncreasingTrend", records.get(i+2).getTimestamp()));
            }
            if (diff1 < -10 && diff2 < -10) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()), type + "DecreasingTrend", records.get(i+2).getTimestamp()));
            }
        }
    }

    /**
     * Triggers an alert for the monitoring system. This method can be extended to
     * notify medical staff, log the alert, or perform other actions. The method
     * currently assumes that the alert information is fully formed when passed as
     * an argument.
     *
     * @param alert the alert object containing details about the alert condition
     */
    private void triggerAlert(Alert alert) {
        System.out.println("ALERT: Patient " + alert.getPatientId() +
            " | Condition: " + alert.getCondition() +
            " | Time: " + alert.getTimestamp());
    }
}
