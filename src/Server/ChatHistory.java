package Server;

import common.Message;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ChatHistory handles all database operations including user authentication
 * and persistent storage of chat messages filtered by topic.
 */
public class ChatHistory {

    // Connection string with configurations for timezone and SSL
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/realtimechat?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "";

    /**
     * Establishes a connection to the MySQL database.
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Manually load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
        }
        return DriverManager.getConnection(URL, USER, PASS);
    }

    /**
     * Validates user credentials during the login process.
     * @param username The username entered by the client
     * @param password The password entered by the client
     * @return true if credentials match a record in the 'users' table
     */
    public static boolean validateLogin(String username, String password) {
        String query = "SELECT * FROM users WHERE username=? AND password=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Returns true if a record exists

        } catch (SQLException e) {
            System.err.println(">> Login Validation Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Persists a message to the database, associated with a specific chatroom/topic.
     * @param msg The message object sent by the client
     * @param topic The name of the chatroom where the message was sent (for Lobby support)
     */
    public static void saveMessage(Message msg, String topic) {
        String sql = "INSERT INTO messages (sender, content, timestamp, topic) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, msg.getSender());
            stmt.setString(2, msg.getContent());
            stmt.setLong(3, msg.getTimestamp());
            stmt.setString(4, topic); // Categorizes the message under a specific room

            stmt.executeUpdate();
            System.out.println(">> Message from [" + msg.getSender() + "] saved to topic: " + topic);

        } catch (SQLException e) {
            System.err.println(">> Database Save Error: " + e.getMessage());
        }
    }
}