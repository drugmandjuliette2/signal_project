package com.cardio_generator;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.cardio_generator.generators.AlertGenerator;
import com.cardio_generator.generators.BloodPressureDataGenerator;
import com.cardio_generator.generators.BloodSaturationDataGenerator;
import com.cardio_generator.generators.BloodLevelsDataGenerator;
import com.cardio_generator.generators.ECGDataGenerator;
import com.cardio_generator.outputs.ConsoleOutputStrategy;
import com.cardio_generator.outputs.FileOutputStrategy;
import com.cardio_generator.outputs.OutputStrategy;
import com.cardio_generator.outputs.TcpOutputStrategy;
import com.cardio_generator.outputs.WebSocketOutputStrategy;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * This is the main engine of the simulator. It coordinates everything:
 * it reads the user's settings, sets up the virtual patients, and
 * schedules all the different health data generators to run on a timer.
 */
public class HealthDataSimulator {

    private static HealthDataSimulator instance;

    private int patientCount = 50;

    private ScheduledExecutorService scheduler;
    private OutputStrategy outputStrategy = new ConsoleOutputStrategy();
    private final Random random = new Random();

    private HealthDataSimulator() {
    }

    public static synchronized HealthDataSimulator getInstance() {
        if (instance == null) {
            instance = new HealthDataSimulator();
        }
        return instance;
    }

    /**
     * The starting point of the application. It kicks off the process of
     * parsing inputs and starting the background tasks for each patient.
     *
     * @param args The settings passed in from the terminal.
     * @throws IOException If something goes wrong when trying to create folders for
     *                     file output.
     */
    public static void main(String[] args) throws IOException {
        HealthDataSimulator simulator = HealthDataSimulator.getInstance();
        simulator.parseArguments(args);
        simulator.startSimulation();
    }

    public void startSimulation() {
        // handle multiple patients running at the same time
        this.scheduler = Executors.newScheduledThreadPool(this.patientCount * 4);

        List<Integer> patientIds = initializePatientIds(this.patientCount);
        Collections.shuffle(patientIds); // not handling patients at the exact same time

        scheduleTasksForPatients(patientIds);
    }

    /**
     * Look through the terminal arguments to see how many patients we need
     * and where the data should be sent (console, file, etc.).
     *
     * @param args The raw strings from the command line.
     * @throws IOException Needed if we are setting up a local directory for
     *                     storage.
     */
    private void parseArguments(String[] args) throws IOException {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-h":
                    System.out.println("Help requested. ");
                    System.exit(0);
                    break;
                case "--patient-count":
                    if (i + 1 < args.length) {
                        try {
                            this.patientCount = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Input wasn't a number. Sticking with the default: " + patientCount);
                        }
                    }
                    break;
                case "--output":
                    if (i + 1 < args.length) {
                        String outputArg = args[++i];
                        if (outputArg.equals("console")) {
                            this.outputStrategy = new ConsoleOutputStrategy();
                        } else if (outputArg.startsWith("file:")) {
                            String baseDirectory = outputArg.substring(5);
                            Path outputPath = Paths.get(baseDirectory);
                            if (!Files.exists(outputPath)) {
                                Files.createDirectories(outputPath);
                            }
                            this.outputStrategy = new FileOutputStrategy(baseDirectory);
                        } else if (outputArg.startsWith("websocket:")) {
                            try {
                                int port = Integer.parseInt(outputArg.substring(10));
                                this.outputStrategy = new WebSocketOutputStrategy(port);
                            } catch (NumberFormatException e) {
                                System.err.println("That port number doesn't look right for WebSocket.");
                            }
                        } else if (outputArg.startsWith("tcp:")) {
                            try {
                                int port = Integer.parseInt(outputArg.substring(4));
                                this.outputStrategy = new TcpOutputStrategy(port);
                            } catch (NumberFormatException e) {
                                System.err.println("That port number doesn't look right for TCP.");
                            }
                        }
                    }
                    break;
            }
        }
    }

    /**
     * Simply creates a list of numbers from 1 up to the patient count to serve as
     * IDs.
     *
     * @param patientCount Total patients requested.
     * @return A list of unique patient IDs.
     */
    private List<Integer> initializePatientIds(int patientCount) {
        List<Integer> patientIds = new ArrayList<>();
        for (int i = 1; i <= patientCount; i++) {
            patientIds.add(i);
        }
        return patientIds;
    }

    /**
     * This is where we define how often each type of data is generated.
     * ECG happens every second, while Blood Levels only happen every 2 minutes.
     *
     * @param patientIds The list of patients who need their data simulated.
     */
    private void scheduleTasksForPatients(List<Integer> patientIds) {
        ECGDataGenerator ecgDataGenerator = new ECGDataGenerator(this.patientCount);
        BloodSaturationDataGenerator bloodSaturationDataGenerator = new BloodSaturationDataGenerator(this.patientCount);
        BloodPressureDataGenerator bloodPressureDataGenerator = new BloodPressureDataGenerator(this.patientCount);
        BloodLevelsDataGenerator bloodLevelsDataGenerator = new BloodLevelsDataGenerator(this.patientCount);
        AlertGenerator alertGenerator = new AlertGenerator(this.patientCount);

        for (int patientId : patientIds) {
            scheduleTask(() -> ecgDataGenerator.generate(patientId, this.outputStrategy), 1, TimeUnit.SECONDS);
            scheduleTask(() -> bloodSaturationDataGenerator.generate(patientId, this.outputStrategy), 1,
                    TimeUnit.SECONDS);
            scheduleTask(() -> bloodPressureDataGenerator.generate(patientId, this.outputStrategy), 1,
                    TimeUnit.MINUTES);
            scheduleTask(() -> bloodLevelsDataGenerator.generate(patientId, this.outputStrategy), 2, TimeUnit.MINUTES);
            scheduleTask(() -> alertGenerator.generate(patientId, this.outputStrategy), 20, TimeUnit.SECONDS);
        }
    }

    /**
     * A helper method to tell the scheduler when to run a specific job.
     * It adds a tiny random delay at the start so the CPU isn't hit with
     * every single task at the exact same microsecond.
     *
     * @param task     The specific generation logic to run.
     * @param period   How long to wait between runs.
     * @param timeUnit The scale of time (seconds, minutes, etc.).
     */
    private void scheduleTask(Runnable task, long period, TimeUnit timeUnit) {
        this.scheduler.scheduleAtFixedRate(task, this.random.nextInt(5), period, timeUnit);
    }
}
