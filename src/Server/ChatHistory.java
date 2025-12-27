package Server;

import common.Message;
import java.sql.*;

public class ChatHistory {

    // ---------------------------------------------------------
    // FIX IS HERE:
    // 1. Used "127.0.0.1" instead of "localhost"
    // 2. Added "/realtimechat" to select the database
    // 3. Added "?useSSL=false..." to fix the driver errors
    // ---------------------------------------------------------
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/realtimechat?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    // Credentials
    private static final String USER = "root";
    private static final String PASS = "";

    public static Connection getConnection() throws SQLException {
        // This ensures the driver is loaded
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static boolean validateLogin(String username, String password) {
        // The try-with-resources block automatically closes the connection
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username=? AND password=?")) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Returns true if a match is found

        } catch (SQLException e) {
            System.out.println(">> Login Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static void saveMessage(Message msg) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO messages (sender, content, timestamp) VALUES (?, ?, ?)")) {

            stmt.setString(1, msg.getSender());
            stmt.setString(2, msg.getContent());
            stmt.setLong(3, msg.getTimestamp());

            stmt.executeUpdate();
            System.out.println(">> Message saved to DB.");

        } catch (SQLException e) {
            System.out.println(">> Save Message Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}