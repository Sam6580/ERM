package src.client;

import java.io.*;
import java.net.Socket;
import java.util.List;
import src.model.Reflection;

public class ERMClient {
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    public ERMClient() throws IOException {
        try {
            this.socket = new Socket("localhost", 5000); // Connect to server
            this.oos = new ObjectOutputStream(socket.getOutputStream());
            this.ois = new ObjectInputStream(socket.getInputStream());
            System.out.println("✅ Connected to ERM Server");
        } catch (IOException e) {
            System.err.println("❌ Failed to connect to server: " + e.getMessage());
            throw new IOException("Unable to connect to server. Make sure the server is running on port 5000.", e);
        }
    }

    // Send a Reflection object to the server
    public String sendReflection(Reflection reflection) throws IOException, ClassNotFoundException {
        oos.writeObject(reflection);
        oos.flush();
        Object response = ois.readObject();
        return response instanceof String ? (String) response : "Unexpected response from server.";
    }

    // Request all reflections from the server
    @SuppressWarnings("unchecked")
    public List<Reflection> fetchReflections() throws IOException, ClassNotFoundException {
        try {
            if (socket == null || socket.isClosed() || oos == null) {
                throw new IOException("Connection is closed");
            }
            oos.writeObject("FETCH");
            oos.flush();
            Object response = ois.readObject();
            if (response instanceof List<?>) {
                return (List<Reflection>) response;
            } else if (response instanceof String && ((String) response).startsWith("❌")) {
                throw new IOException((String) response);
            } else {
                throw new IOException("Invalid response from server: " + response);
            }
        } catch (IOException | ClassNotFoundException e) {
            // Don't close connection on error - let it be reused
            throw e;
        }
    }

    // Update a reflection with feedback, rating, and status
    public String updateReflection(Reflection reflection) throws IOException, ClassNotFoundException {
        try {
            if (socket == null || socket.isClosed() || oos == null) {
                throw new IOException("Connection is closed");
            }
            oos.writeObject("UPDATE");
            oos.flush();
            oos.writeObject(reflection);
            oos.flush();
            Object response = ois.readObject();
            return response instanceof String ? (String) response : "Unexpected response from server.";
        } catch (IOException | ClassNotFoundException e) {
            throw e;
        }
    }

    // Close the connection
    public void close() throws IOException {
        try {
            if (ois != null) ois.close();
            if (oos != null) oos.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("✅ Connection closed");
        } catch (IOException e) {
            System.err.println("⚠️ Error closing connection: " + e.getMessage());
            throw e;
        }
    }
}
