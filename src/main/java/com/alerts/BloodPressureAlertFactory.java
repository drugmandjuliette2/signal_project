package com.alerts;

public class BloodPressureAlertFactory extends AlertFactory {

    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        // returns an alert for blood pressure anomalies
        return new Alert(patientId, "Blood pressure anomaly: " + condition, timestamp);
    }
}
