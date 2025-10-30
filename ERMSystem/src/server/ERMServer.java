package server;

import database.DBConnection;
import model.Reflection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ERMServer {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("🚀 ERM Server started. Waiting for clients...");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("✅ Client connected!");
                new ClientHandler(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler extends Thread {
    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try (Socket s = socket;
             ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
             ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream())) {

            Object obj = ois.readObject();

            try (Connection conn = DBConnection.getConnection()) {
                if (obj instanceof Reflection) {
                    Reflection reflection = (Reflection) obj;
                    String query = "INSERT INTO reflections (student_name, roll_no, reflection) VALUES (?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(query)) {
                        ps.setString(1, reflection.getStudentName());
                        ps.setString(2, reflection.getRollNo());
                        ps.setString(3, reflection.getReflectionText());
                        ps.executeUpdate();
                        oos.writeObject("✅ Reflection submitted successfully!");
                    }
                } else if (obj instanceof String && "FETCH".equals(obj)) {
                    String query = "SELECT * FROM reflections";
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery(query)) {
                        List<Reflection> list = new ArrayList<>();
                        while (rs.next()) {
                            Reflection ref = new Reflection(
                                    rs.getString("student_name"),
                                    rs.getString("roll_no"),
                                    rs.getString("reflection")
                            );
                            ref.setStatus(rs.getString("status"));
                            list.add(ref);
                        }
                        oos.writeObject(list);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                // Optionally send an error message to the client
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
