//changing space to 2
package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;

public class FileOutputStrategy implements OutputStrategy {
// Google Style: Variable names should be lowerCamelCase (changed BaseDirectory to baseDirectory)
    private String baseDirectory;
    // Google Style: No underscores in variable names (changed file_map to fileMap)
  // Also changed to private to follow best practices for encapsulation
    private final ConcurrentHashMap<String, String> filemap = new ConcurrentHashMap<>();

    public FileOutputStrategy(String baseDirectory) {
        //Correction base directory
        this.baseDirectory = baseDirectory;
    }

    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        try {
            // Create the directory and correction baseDirectory
            Files.createDirectories(Paths.get(baseDirectory));
        } catch (IOException e) {
            System.err.println("Error creating base directory: " + e.getMessage());
            return;
        }
        // Set the FilePath variable
        // Google Style: Variable names must start with lowercase (changed FilePath to filePath)
        // Also corrected the call to the renamed fileMap
        String filePath = fileMap.computeIfAbsent(label, k -> Paths.get(baseDirectory, label + ".txt").toString());

        // Write the data to the file
        try (PrintWriter out = new PrintWriter(
                Files.newBufferedWriter(Paths.get(filePath), StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n", patientId, timestamp, label, data);
        } catch (Exception e) {
            System.err.println("Error writing to file " + FilePath + ": " + e.getMessage());
        }
    }
}
