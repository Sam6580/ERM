package src.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import src.database.DBConnection;
import src.model.Reflection;

public class ERMServer {
    public static void main(String[] args) {
        // Initialize database table before accepting connections
        System.out.println("🔧 Initializing database...");
        DBConnection.createTableIfNotExists();

        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("🚀 ERM Server started. Waiting for clients...");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("✅ Client connected!");
                new ClientHandler(socket).start();
            }
        } catch (IOException e) {
            System.err.println("❌ Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

class ClientHandler extends Thread {
    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (Socket s = socket;
             ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
             ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream())) {

            try (Connection conn = DBConnection.getConnection()) {
                // Handle multiple requests on the same connection
                while (true) {
                    Object obj;
                    try {
                        obj = ois.readObject();
                    } catch (EOFException e) {
                        // Client closed connection
                        break;
                    }
                    
                    if (obj instanceof Reflection reflection) {
                        String query = "INSERT INTO reflections (student_name, roll_no, subject, reflection, faculty_feedback, rating, submitted_at, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement ps = conn.prepareStatement(query)) {
                            ps.setString(1, reflection.getStudentName());
                            ps.setString(2, reflection.getRegisterNumber());
                            ps.setString(3, reflection.getSubject());
                            ps.setString(4, reflection.getReflectionText());
                            String feedback = reflection.getFacultyFeedback();
                            ps.setString(5, feedback != null ? feedback : "");
                            ps.setDouble(6, reflection.getRating());
                            ps.setTimestamp(7, Timestamp.valueOf(reflection.getSubmittedAt()));
                            ps.setString(8, reflection.getStatus() != null ? reflection.getStatus() : "Pending");
                            ps.executeUpdate();
                            oos.writeObject("✅ Reflection submitted successfully!");
                            System.out.println("✅ Reflection saved: " + reflection.getStudentName() + " - " + reflection.getSubject());
                        } catch (SQLException e) {
                            System.err.println("❌ SQL Error inserting reflection: " + e.getMessage());
                            System.err.println("SQL State: " + e.getSQLState());
                            e.printStackTrace();
                            String errorMsg = "❌ Database error: " + e.getMessage();
                            if (e.getMessage() != null && e.getMessage().contains("Unknown column")) {
                                errorMsg += "\n\n⚠️ Column mismatch detected! Please restart the server to update the database schema.";
                            }
                            oos.writeObject(errorMsg);
                        }
                    } else if (obj instanceof String) {
                        String command = (String) obj;
                        if ("FETCH".equals(command)) {
                            String query = "SELECT * FROM reflections ORDER BY submitted_at DESC";
                            try (Statement stmt = conn.createStatement();
                                 ResultSet rs = stmt.executeQuery(query)) {
                                List<Reflection> list = new ArrayList<>();
                                while (rs.next()) {
                                    String feedback = rs.getString("faculty_feedback");
                                    Reflection ref = new Reflection(
                                            rs.getInt("id"),
                                            rs.getString("student_name"),
                                            rs.getString("roll_no"),
                                            rs.getString("subject"),
                                            rs.getString("reflection"),
                                            feedback != null ? feedback : "",
                                            rs.getDouble("rating"),
                                            rs.getTimestamp("submitted_at").toLocalDateTime()
                                    );
                                    String status = rs.getString("status");
                                    ref.setStatus(status != null ? status : "Pending");
                                    list.add(ref);
                                }
                                oos.writeObject(list);
                                System.out.println("✅ Fetched " + list.size() + " reflection(s)");
                            } catch (SQLException e) {
                                System.err.println("❌ SQL Error fetching reflections: " + e.getMessage());
                                e.printStackTrace();
                                oos.writeObject("❌ Database error: " + e.getMessage());
                            }
                        } else if ("UPDATE".equals(command)) {
                            // Read the Reflection object for update
                            try {
                                Object updateObj = ois.readObject();
                                if (updateObj instanceof Reflection updateReflection) {
                                    String query = "UPDATE reflections SET faculty_feedback = ?, rating = ?, status = ? WHERE id = ?";
                                    try (PreparedStatement ps = conn.prepareStatement(query)) {
                                        ps.setString(1, updateReflection.getFacultyFeedback() != null ? updateReflection.getFacultyFeedback() : "");
                                        ps.setDouble(2, updateReflection.getRating());
                                        ps.setString(3, updateReflection.getStatus() != null ? updateReflection.getStatus() : "Pending");
                                        ps.setInt(4, updateReflection.getId());
                                        int rowsAffected = ps.executeUpdate();
                                        if (rowsAffected > 0) {
                                            oos.writeObject("✅ Reflection updated successfully!");
                                            System.out.println("✅ Reflection updated: ID " + updateReflection.getId());
                                        } else {
                                            oos.writeObject("❌ Reflection not found or update failed.");
                                        }
                                    } catch (SQLException e) {
                                        System.err.println("❌ SQL Error updating reflection: " + e.getMessage());
                                        e.printStackTrace();
                                        oos.writeObject("❌ Database error: " + e.getMessage());
                                    }
                                } else {
                                    oos.writeObject("❌ Invalid update object");
                                }
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                                oos.writeObject("❌ Error reading update data: " + e.getMessage());
                            }
                        } else {
                            oos.writeObject("❌ Unknown command: " + command);
                        }
                    } else {
                        oos.writeObject("❌ Unknown request type");
                    }
                }
            } catch (SQLException e) {
                System.err.println("❌ Database connection error: " + e.getMessage());
                e.printStackTrace();
                try {
                    oos.writeObject("❌ Server database error: " + e.getMessage());
                } catch (IOException ioEx) {
                    ioEx.printStackTrace();
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
