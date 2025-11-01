package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String DB_HOST = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "erm_db";
    private static final String USER = "root";  // change if needed
    private static final String PASSWORD = "";  // your MySQL password

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(DB_HOST, USER, PASSWORD);
                 java.sql.Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            } catch (SQLException e) {
                System.err.println("Failed to create database: " + e.getMessage());
            }
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_HOST + DB_NAME, USER, PASSWORD);
    }

    public static void createTableIfNotExists() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS reflections (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "student_name VARCHAR(255) NOT NULL," +
                "roll_no VARCHAR(255) NOT NULL," +
                "reflection TEXT NOT NULL," +
                "status VARCHAR(50) DEFAULT 'Pending'," +
                "submission_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";

        try (Connection conn = getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("Table 'reflections' is ready.");
        } catch (SQLException e) {
            System.err.println("Error creating table: " + e.getMessage());
        }
    }
}
