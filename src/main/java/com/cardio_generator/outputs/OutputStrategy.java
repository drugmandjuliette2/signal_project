package com.cardio_generator.outputs;

/**
 * This interface defines the standard way for the simulator to send out data.
 * Think of it as a universal plug: no matter where the data is actually going 
 * (like a file, the console, or a server), the generators use this same 
 * method to "plug in" and deliver their results.
 */
public interface OutputStrategy {

    /**
     * This method is called whenever a generator has new data ready to be shared.
     *
     * @param patientId The ID of the patient the data belongs to.
     * @param timestamp The exact time (in milliseconds) when the data was recorded.
     * @param label     The category of the data (e.g., "HeartRate", "BloodPressure").
     * @param data      The actual measurement or status update being sent.
     */
    void output(int patientId, long timestamp, String label, String data);
}
