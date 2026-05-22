package com.data_management;

import java.io.IOException;
import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class WebSocketClientImpl extends WebSocketClient implements DataReader {

    private final DataStorage dataStorage;

    public WebSocketClientImpl(URI serverUri, DataStorage dataStorage) {
        super(serverUri);
        this.dataStorage = dataStorage;
    }

    @Override
    public void readData(URI serverUri) throws IOException {
        try {
            System.out.println("Connecting to real-time medical data stream at: " + serverUri);
            this.connectBlocking();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Connection sequence was interrupted", e);
        }
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println(" Successfully connection to hospital data streaming server.");
    }

    @Override
    public void onMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            System.err.println("Warning: Received an empty or corrupted stream frame. Skipping.");
            return;
        }

        try {
            String[] tokens = message.split(",");
            if (tokens.length != 4) {
                System.err.println("Dropped Frame: Incorrect layout length. Expected 4 fields, got " + tokens.length);
                return;
            }

            int patientId = Integer.parseInt(tokens[0].trim());
            double measurementValue = Double.parseDouble(tokens[3].trim());
            String recordType = tokens[2].trim();
            long timestamp = Long.parseLong(tokens[1].trim());

            dataStorage.addPatientData(patientId, measurementValue, recordType, timestamp);
            System.out.println(
                    "Real-time metric saved -> Patient: " + patientId + " | " + recordType + ": " + measurementValue);

        } catch (Exception e) {
            System.err.println("Data stream recovery handler caught line error: " + e.getMessage()
                    + " -> Frame content: " + message);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Connection closed by " + (remote ? "server" : "client") + ". Reason: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("Real-time network stream encountered an internal fault: " + ex.getMessage());
    }

}
