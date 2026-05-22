package com.alerts;

import com.data_management.Patient;
import com.data_management.PatientRecord;
import java.util.List;

public class OxygenSaturationStrategy implements AlertStrategy {

    private final AlertFactory alertFactory = new BloodOxygenAlertFactory();

    @Override
    public void checkAlert(Patient patient) {
        int patientId = patient.getPatientID();
        long current = System.currentTimeMillis();
        long tenMinAgo = current - (10 * 60 * 1000);

        List<PatientRecord> saturationRecords = patient.getRecords(0, current);

        // low saturation alert
        for (PatientRecord r : saturationRecords) {
            if (r.getMeasurementValue() < 92) {
                Alert alert = alertFactory.createAlert(String.valueOf(patientId), "LowSaturation", r.getTimestamp());
            }
        }

        // rapid drop (5% in 10 min )
        List<PatientRecord> recentSaturation = saturationRecords.stream()
                .filter(r -> r.getTimestamp() >= tenMinAgo && r.getRecordType().equals("Saturation"))
                .toList();

        if (recentSaturation.size() >= 2) {
            double first = recentSaturation.get(0).getMeasurementValue();
            double last = recentSaturation.get(recentSaturation.size() - 1).getMeasurementValue();
            if (first - last >= 5) {
                Alert alert = alertFactory.createAlert(String.valueOf(patientId), "RapidSaturationDrop", current);
            }
        }

        // combined hypotensive hypoxemia
        // also get systolic records
        List<PatientRecord> systolicRecords = patient.getRecords(0, current);
        boolean lowSystolic = systolicRecords.stream().anyMatch(r -> r.getMeasurementValue() < 90);
        boolean lowSaturation = saturationRecords.stream().anyMatch(r -> r.getMeasurementValue() < 92);

        if (lowSystolic && lowSaturation) {
            Alert alert = alertFactory.createAlert(String.valueOf(patientId), "HypotensiveHypoxemia", current);
        }

    }

}
