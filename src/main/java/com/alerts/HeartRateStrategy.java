package com.alerts;

import com.data_management.Patient;
import com.data_management.PatientRecord;
import java.util.List;

public class HeartRateStrategy implements AlertStrategy {

    private final AlertFactory alertFactory = new ECGAlertFactory();

    @Override
    public void checkAlert(Patient patient) {
        int patientId = patient.getPatientID();
        long current = System.currentTimeMillis();

        List<PatientRecord> ecgRecords = patient.getRecords(0, current).stream()
                .filter(r -> r.getRecordType().equals("ECG")).toList();

        if (ecgRecords.size() > 10) {
            double avg = ecgRecords.stream()
                    .mapToDouble(PatientRecord::getMeasurementValue).average().orElse(0);
            for (PatientRecord r : ecgRecords) {
                if (r.getMeasurementValue() > avg * 1.5) {
                    Alert alert = alertFactory.createAlert(String.valueOf(patientId), "AbnormalECGPeak",
                            r.getTimestamp());
                    break;
                }
            }
        }

        List<PatientRecord> buttonRecords = patient.getRecords(0, current).stream()
                .filter(r -> r.getRecordType().equals("Alert")).toList();

        for (PatientRecord r : buttonRecords) {
            if (r.getMeasurementValue() == 1) {
                Alert alert = alertFactory.createAlert(String.valueOf(patientId), "EmergencyButtonCall",
                        r.getTimestamp());
            }
        }

    }

}
