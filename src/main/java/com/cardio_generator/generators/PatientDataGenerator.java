package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * This is the blueprint for all data generators in the simulator.
 * * Any class that implements this interface is promising to provide a way 
 * to create specific health data for a patient and send it to an output.
 */
public interface PatientDataGenerator {

    /**
     * This method must be implemented by any generator to define how it
     * creates data and where that data goes.
     *
     * @param patientId The ID of the patient we are currently simulating.
     * @param outputStrategy The method used to handle the resulting data (e.g., printing or saving).
     */
    void generate(int patientId, OutputStrategy outputStrategy);
}
