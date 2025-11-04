package src.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String DB_HOST = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "erm_db";
    private static final String USER = "root";  // change if needed
    private static final String PASSWORD = "sam_6580";  // your MySQL password

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
        try (Connection conn = getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            
            // Check if table exists
            boolean tableExists;
            try (java.sql.ResultSet rs = conn.getMetaData().getTables(null, null, "reflections", null)) {
                tableExists = rs.next();
            }
            
            if (!tableExists) {
                // Create new table with correct schema
                String createTableSQL = "CREATE TABLE reflections (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "student_name VARCHAR(255) NOT NULL," +
                        "roll_no VARCHAR(255) NOT NULL," +
                        "subject VARCHAR(255) NOT NULL," +
                        "reflection TEXT NOT NULL," +
                        "faculty_feedback TEXT," +
                        "rating DOUBLE DEFAULT 0.0," +
                        "submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "status VARCHAR(50) DEFAULT 'Pending'" +
                        ")";
                stmt.execute(createTableSQL);
                System.out.println("✅ Table 'reflections' created successfully.");
            } else {
                // Table exists, check and add missing columns
                System.out.println("🔍 Table 'reflections' exists. Checking schema...");
                updateTableSchema(conn, stmt);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error creating/updating table: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void updateTableSchema(Connection conn, java.sql.Statement stmt) throws SQLException {
        try {
            // Check and add missing columns
            java.sql.DatabaseMetaData metaData = conn.getMetaData();
            java.util.Set<String> existingColumns = new java.util.HashSet<>();
            java.util.List<String> columnNames = new java.util.ArrayList<>();
            try (java.sql.ResultSet columns = metaData.getColumns(null, null, "reflections", null)) {
                while (columns.next()) {
                    String colName = columns.getString("COLUMN_NAME");
                    existingColumns.add(colName.toLowerCase());
                    columnNames.add(colName);
                }
            }
            
            // Print existing columns for debugging
            System.out.println("📋 Existing columns: " + String.join(", ", columnNames));
            
            // Ensure student_name exists
            if (!existingColumns.contains("student_name")) {
                stmt.execute("ALTER TABLE reflections ADD COLUMN student_name VARCHAR(255) NOT NULL DEFAULT ''");
                System.out.println("✅ Added column 'student_name'");
            }
            
            // Ensure roll_no exists (critical column)
            if (!existingColumns.contains("roll_no")) {
                // Check for alternative names (need to check actual column names, not lowercase set)
                String altName = null;
                for (String colName : columnNames) {
                    String lowerCol = colName.toLowerCase();
                    if (lowerCol.equals("register_no") || lowerCol.equals("roll_number") || 
                        lowerCol.equals("register_number") || lowerCol.equals("rollno")) {
                        altName = colName; // Use actual case from database
                        break;
                    }
                }
                
                if (altName != null) {
                    // Rename the alternative to roll_no
                    stmt.execute("ALTER TABLE reflections CHANGE COLUMN `" + altName + "` roll_no VARCHAR(255) NOT NULL");
                    System.out.println("✅ Renamed column '" + altName + "' to 'roll_no'");
                } else {
                    // Add roll_no column
                    stmt.execute("ALTER TABLE reflections ADD COLUMN roll_no VARCHAR(255) NOT NULL DEFAULT ''");
                    System.out.println("✅ Added column 'roll_no'");
                }
            }
            
            // Ensure reflection column exists
            if (!existingColumns.contains("reflection")) {
                stmt.execute("ALTER TABLE reflections ADD COLUMN reflection TEXT NOT NULL");
                System.out.println("✅ Added column 'reflection'");
            }
            
            // Add subject column if missing
            if (!existingColumns.contains("subject")) {
                // First add as nullable, then update existing rows, then make NOT NULL
                stmt.execute("ALTER TABLE reflections ADD COLUMN subject VARCHAR(255)");
                stmt.execute("UPDATE reflections SET subject = '' WHERE subject IS NULL");
                stmt.execute("ALTER TABLE reflections MODIFY COLUMN subject VARCHAR(255) NOT NULL DEFAULT ''");
                System.out.println("✅ Added column 'subject'");
            }
            
            // Add faculty_feedback column if missing
            if (!existingColumns.contains("faculty_feedback")) {
                stmt.execute("ALTER TABLE reflections ADD COLUMN faculty_feedback TEXT");
                System.out.println("✅ Added column 'faculty_feedback'");
            }
            
            // Add rating column if missing
            if (!existingColumns.contains("rating")) {
                stmt.execute("ALTER TABLE reflections ADD COLUMN rating DOUBLE DEFAULT 0.0");
                System.out.println("✅ Added column 'rating'");
            }
            
            // Rename submission_date to submitted_at if needed
            if (existingColumns.contains("submission_date") && !existingColumns.contains("submitted_at")) {
                stmt.execute("ALTER TABLE reflections CHANGE COLUMN submission_date submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
                System.out.println("✅ Renamed column 'submission_date' to 'submitted_at'");
            } else if (!existingColumns.contains("submitted_at")) {
                stmt.execute("ALTER TABLE reflections ADD COLUMN submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
                System.out.println("✅ Added column 'submitted_at'");
            }
            
            // Ensure status column exists
            if (!existingColumns.contains("status")) {
                stmt.execute("ALTER TABLE reflections ADD COLUMN status VARCHAR(50) DEFAULT 'Pending'");
                System.out.println("✅ Added column 'status'");
            }
            
            // Verify all required columns exist
            existingColumns.clear();
            columnNames.clear();
            try (java.sql.ResultSet columns = metaData.getColumns(null, null, "reflections", null)) {
                while (columns.next()) {
                    String colName = columns.getString("COLUMN_NAME");
                    existingColumns.add(colName.toLowerCase());
                    columnNames.add(colName);
                }
            }
            
            // Check for critical columns
            boolean hasAllRequired = existingColumns.contains("student_name") &&
                                   existingColumns.contains("roll_no") &&
                                   existingColumns.contains("reflection");
            
            if (!hasAllRequired) {
                System.err.println("⚠️ Critical columns still missing after update. Recreating table...");
                throw new SQLException("Required columns missing");
            }
            
            System.out.println("✅ Table 'reflections' schema is up to date.");
            System.out.println("📋 Final columns: " + String.join(", ", columnNames));
        } catch (SQLException e) {
            System.err.println("⚠️ Warning: Could not update table schema: " + e.getMessage());
            // If ALTER fails, try dropping and recreating (destructive but ensures correctness)
            System.out.println("⚠️ Attempting to recreate table (existing data will be lost)...");
            try {
                stmt.execute("DROP TABLE IF EXISTS reflections");
                String createTableSQL = "CREATE TABLE reflections (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "student_name VARCHAR(255) NOT NULL," +
                        "roll_no VARCHAR(255) NOT NULL," +
                        "subject VARCHAR(255) NOT NULL," +
                        "reflection TEXT NOT NULL," +
                        "faculty_feedback TEXT," +
                        "rating DOUBLE DEFAULT 0.0," +
                        "submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "status VARCHAR(50) DEFAULT 'Pending'" +
                        ")";
                stmt.execute(createTableSQL);
                System.out.println("✅ Table 'reflections' recreated with correct schema.");
            } catch (SQLException ex) {
                System.err.println("❌ Failed to recreate table: " + ex.getMessage());
                throw ex;
            }
        }
    }
}
