package com.data_management;

import java.io.*;
import java.nio.file.*;

public class FileDataReader implements DataReader {
    private String outputDirectory;

    public FileDataReader(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        File dir = new File(outputDirectory);
        for (File file : dir.listFiles()) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                // Format: "Patient ID: 1, Timestamp: 1700000000000, Label: HeartRate, Data: 75.0"
                String[] parts = line.split(", ");
                int patientId = Integer.parseInt(parts[0].split(": ")[1]);
                long timestamp = Long.parseLong(parts[1].split(": ")[1]);
                String label = parts[2].split(": ")[1];
                double data = Double.parseDouble(parts[3].split(": ")[1]);
                dataStorage.addPatientData(patientId, data, label, timestamp);
            }
            reader.close();
        }
    }
}
