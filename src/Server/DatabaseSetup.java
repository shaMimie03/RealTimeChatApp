package Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseSetup {
    // Note: Connect to the server root, not the specific database yet
    private static final String SERVER_URL = "jdbc:mysql://localhost:3306/realtimechat";
    private static final String DB_NAME = "realtimechat";
    private static final String USER = "root";
    private static final String PASS = ""; // Default XAMPP password is empty

    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(SERVER_URL, USER, PASS);
             Statement stmt = conn.createStatement()) {

            // 1. Create Database if it doesn't exist
            String createDb = "CREATE DATABASE IF NOT EXISTS " + DB_NAME;
            stmt.executeUpdate(createDb);
            System.out.println("Database '" + DB_NAME + "' checked/created.");

            // 2. Select the Database
            stmt.executeUpdate("USE " + DB_NAME);

            // 3. Create Users Table
            String createUsers = "CREATE TABLE IF NOT EXISTS users (" +
                    "user_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(50) UNIQUE NOT NULL, " +
                    "password VARCHAR(50) NOT NULL)";
            stmt.executeUpdate(createUsers);

            // 4. Create Messages Table
            String createMessages = "CREATE TABLE IF NOT EXISTS messages (" +
                    "message_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "sender VARCHAR(50), " +
                    "content TEXT, " +
                    "timestamp BIGINT)";
            stmt.executeUpdate(createMessages);

            // 5. Insert a default admin user (ignore error if exists)
            try {
                stmt.executeUpdate("INSERT INTO users (username, password) VALUES ('admin', 'admin')");
                System.out.println("Default admin user created.");
            } catch (Exception e) {
                // User likely already exists, ignore
            }

            System.out.println("Tables checked/created successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("CRITICAL ERROR: Could not connect to MySQL. Is XAMPP running?");
        }
    }
}
