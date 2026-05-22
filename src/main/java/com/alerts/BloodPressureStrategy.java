package com.alerts;

import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.List;

public class BloodPressureStrategy implements AlertStrategy {

    private final AlertFactory alertFactory = new BloodPressureAlertFactory();

    @Override
    public void checkAlert(Patient patient) {
        int patientId = patient.getPatientID();

        List<PatientRecord> systolicRecords = patient.getRecords(0, System.currentTimeMillis());
        List<PatientRecord> diastolicRecords = patient.getRecords(0, System.currentTimeMillis());

        for (PatientRecord r : systolicRecords) {
            if (r.getMeasurementValue() > 180 || r.getMeasurementValue() < 90) {
                Alert alert = alertFactory.createAlert(String.valueOf(patientId),
                        "Critical Systolic Pressure (" + r.getMeasurementValue() + ")", r.getTimestamp());
            }
        }
        for (PatientRecord r : diastolicRecords) {
            if (r.getMeasurementValue() > 120 || r.getMeasurementValue() < 60) {
                Alert alert = alertFactory.createAlert(String.valueOf(patientId),
                        "Critical Diastolic Pressure (" + r.getMeasurementValue() + ")",
                        r.getTimestamp());
            }
        }

    }

}
