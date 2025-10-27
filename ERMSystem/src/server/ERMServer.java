package server;

import database.DBConnection;
import model.Reflection;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

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
    private Socket socket;
    private Connection conn;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.conn = DBConnection.getConnection();
    }

    public void run() {
        try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {

            Object obj = ois.readObject();

            if (obj instanceof Reflection) {
                Reflection reflection = (Reflection) obj;
                String query = "INSERT INTO reflections (student_name, roll_no, reflection) VALUES (?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(query);
                ps.setString(1, reflection.getStudentName());
                ps.setString(2, reflection.getRollNo());
                ps.setString(3, reflection.getReflectionText());
                ps.executeUpdate();
                oos.writeObject("✅ Reflection submitted successfully!");
            } else if (obj instanceof String && ((String) obj).equals("FETCH")) {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM reflections");
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
