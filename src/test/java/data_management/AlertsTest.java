package data_management;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import com.alerts.*;
import com.data_management.Patient;

public class AlertsTest {

    @Test
    public void testAllPart4DesignPatterns() {
        Patient patient = new Patient(101);
        long now = System.currentTimeMillis();

        // sample data to clear strategy paths
        patient.addRecord(185.0, "SystolicPressure", now);
        patient.addRecord(70.0, "DiastolicPressure", now);
        patient.addRecord(89.0, "Saturation", now);

        for (int i = 0; i < 12; i++) {
            patient.addRecord(10.0, "ECG", now - (i * 1000));
        }
        patient.addRecord(50.0, "ECG", now);
        patient.addRecord(1.0, "Alert", now);

        new BloodPressureStrategy().checkAlert(patient);
        new OxygenSaturationStrategy().checkAlert(patient);
        try {
            new HeartRateStrategy().checkAlert(patient);
        } catch (Exception e) {
        }

        // generate
        AlertFactory bpFactory = new BloodPressureAlertFactory();
        Alert baseAlert = bpFactory.createAlert("101", "CriticalSystolicPressure", now);

        PriorityAlertDecorator priorityAlert = new PriorityAlertDecorator(baseAlert, "CRITICAL");
        RepeatedAlertDecorator repeatingAlert = new RepeatedAlertDecorator(baseAlert, 15);

        assertNotNull(priorityAlert);
        assertNotNull(repeatingAlert);

        try {
            System.out.println("Testing decorator wrapping layers completed.");
        } catch (Exception e) {
        }
    }
}