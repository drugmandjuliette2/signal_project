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
        File[] files = dir.listFiles();
        if (files == null) {
            throw new IOException("Invalid directory: " + outputDirectory);
        }

        for (File file : files) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    try {
                        String[] parts = line.split(", ");
                        int patientId  = Integer.parseInt(parts[0].split(": ")[1].trim());
                        long timestamp = Long.parseLong(parts[1].split(": ")[1].trim());
                        String label   = parts[2].split(": ")[1].trim();
                        String raw     = parts[3].split(": ")[1].trim().replace("%", "");
                        double data    = raw.equalsIgnoreCase("triggered") ? 1.0
                                       : raw.equalsIgnoreCase("resolved")  ? 0.0
                                       : Double.parseDouble(raw);
                        dataStorage.addPatientData(patientId, data, label, timestamp);
                    } catch (Exception e) {
                        System.err.println("Skipping malformed line: " + line);
                    }
                }
            }
        }
    }
}
