package Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseSetup {
    // MySQL configuration for XAMPP
    private static final String SERVER_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "realtimechat";
    private static final String USER = "root";
    private static final String PASS = ""; // Default XAMPP password is empty

    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(SERVER_URL, USER, PASS);
             Statement stmt = conn.createStatement()) {

            // 1. Create Database if it doesn't exist
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            stmt.executeUpdate("USE " + DB_NAME);
            System.out.println(">> Database synchronization initialized.");

            // 2. Create Users Table with online status tracking
            // This supports Requirement (e) for active user synchronization
            String createUsers = "CREATE TABLE IF NOT EXISTS users (" +
                    "user_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(50) UNIQUE NOT NULL, " +
                    "password VARCHAR(50) NOT NULL, " +
                    "is_online BOOLEAN DEFAULT FALSE)";
            stmt.executeUpdate(createUsers);

            // 3. Create Messages Table
            // Fixed syntax: topic moved before TEXT content to ensure MariaDB/MySQL compatibility
            String createMessages = "CREATE TABLE IF NOT EXISTS messages (" +
                    "message_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "sender VARCHAR(50), " +
                    "topic VARCHAR(50) DEFAULT 'General Discussion', " +
                    "content TEXT, " +
                    "timestamp BIGINT)";
            stmt.executeUpdate(createMessages);

            System.out.println(">> Tables synchronized successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("CRITICAL ERROR: MySQL connection failed. Ensure XAMPP MySQL is running.");
        }
    }
}