package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

/**
 * This class sends patient data over a network using a TCP socket.
 * It acts as a server that waits for a client to connect, then streams 
 * the simulated data to that client in real-time.
 */
public class TcpOutputStrategy implements OutputStrategy {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;

    /**
   * Initializes the TCP server on a specific port.
   * It starts a background thread to wait for a client connection so the 
   * simulator can keep running without waiting for someone to connect.
   *
   * @param port The network port number to listen on (e.g., 8080).
   */
    public TcpOutputStrategy(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("TCP Server started on port " + port);

            // Accept clients in a new thread to not block the main thread
            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    clientSocket = serverSocket.accept();
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    System.out.println("Client connected: " + clientSocket.getInetAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
   * Formats the patient data into a comma-separated string and sends it 
   * over the network if a client is currently connected.
   *
   * @param patientId The ID of the patient.
   * @param timestamp The time the data was created.
   * @param label     The type of data (e.g., "ECG").
   * @param data      The actual value being sent.
   */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        if (out != null) {
            String message = String.format("%d,%d,%s,%s", patientId, timestamp, label, data);
            out.println(message);
        }
    }
}
