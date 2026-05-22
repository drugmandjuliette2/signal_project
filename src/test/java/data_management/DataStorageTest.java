package data_management;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import com.alerts.AlertGenerator;
import com.data_management.DataReader;

import java.util.List;
import java.io.IOException;

class DataStorageTest {

    void setUp() {
        // to clear the data before each test runs
        DataStorage.getInstance().getAllPatients().clear();
    }

    @Test
    void testAddAndGetRecords() {
        // TODO Perhaps you can implement a mock data reader to mock the test data?
        DataReader reader = new MyDataReader();
        DataStorage storage = DataStorage.getInstance();
        storage.addPatientData(1, 100.0, "WhiteBloodCells", 1714376789050L);
        storage.addPatientData(1, 200.0, "WhiteBloodCells", 1714376789051L);

        List<PatientRecord> records = storage.getRecords(1, 1714376789050L, 1714376789051L);
        assertEquals(2, records.size()); // Check if two records are retrieved
        assertEquals(100.0, records.get(0).getMeasurementValue()); // Validate first record
    }

    @Test
    void testBloodPressureThresholdAlert() {
        DataStorage storage = DataStorage.getInstance();
        AlertGenerator generator = new AlertGenerator(storage);

        // test the upper limit
        storage.addPatientData(99, 185.0, "Systolic Pressure", System.currentTimeMillis());
        // test the lower limit
        storage.addPatientData(99, 55.0, "Diastolic Pressure", System.currentTimeMillis());

        // just a temporary patient
        Patient patient = new Patient(99);
        assertDoesNotThrow(() -> generator.evaluateData(patient));
    }

    @Test
    void testBloodSaturationAlert() {
        DataStorage storage = DataStorage.getInstance();
        AlertGenerator generator = new AlertGenerator(storage);

        long current = System.currentTimeMillis();
        // convert min to ms
        storage.addPatientData(88, 98.0, "Saturation", current - (2 * 60 * 1000));
        storage.addPatientData(88, 91.0, "Saturation", current);

        Patient patient = new Patient(88);
        assertDoesNotThrow(() -> generator.evaluateData(patient));
    }

    @Test
    void testHypotensiceHypoxemiaCombinedAlert() {
        DataStorage storage = DataStorage.getInstance();
        AlertGenerator generator = new AlertGenerator(storage);

        storage.addPatientData(77, 85.0, "Systolic Pressure", System.currentTimeMillis());
        storage.addPatientData(77, 90.0, "Saturation", System.currentTimeMillis());

        Patient patient = new Patient(77);
        assertDoesNotThrow(() -> generator.evaluateData(patient));
    }

    // heart monitor and emergency call button
    @Test
    void testECGAndManualButtonAlert() {
        DataStorage storage = DataStorage.getInstance();
        AlertGenerator generator = new AlertGenerator(storage);
        long current = System.currentTimeMillis();

        // track sliding window
        for (int i = 0; i < 11; i++) {
            storage.addPatientData(66, 1.2, "ECG", current - (15000 - (i * 10000)));
        }
    }

    class MyDataReader implements DataReader {
        @Override
        public void readData(DataStorage storage) throws IOException {
            // Simulation de lecture de données
        }
    }
}
