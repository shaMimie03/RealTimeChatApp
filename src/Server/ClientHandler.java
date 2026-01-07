package Server;

import common.Message;
import java.io.*;
import java.net.Socket;

/**
 * ClientHandler manages the server-side connection for each individual client.
 * It handles the login process and routes chat messages to other users.
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    private UserManager userManager;
    private ChatServer server; // Reference to the main server for broadcasting
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;
    private boolean isRunning = true;

    // Requirement (a): Constructor updated to accept 3 arguments
    public ClientHandler(Socket socket, UserManager userManager, ChatServer server) {
        this.socket = socket;
        this.userManager = userManager;
        this.server = server;
    }

    // Requirement: Getter for ChatServer to retrieve username for the list update
    public String getUsername() {
        return username;
    }

    @Override
    public void run() {
        try {
            // Initialize I/O streams for object serialization
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            while (isRunning) {
                try {
                    Object obj = in.readObject();
                    if (obj instanceof Message) {
                        Message msg = (Message) obj;
                        processMessage(msg);
                    }
                } catch (EOFException e) {
                    break; // Client closed the connection
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Connection lost for user: " + (username != null ? username : "Unknown"));
        } finally {
            closeConnection();
        }
    }

    /**
     * Processes different types of incoming messages (LOGIN or CHAT).
     * @param msg The message object received from the client.
     */
    private void processMessage(Message msg) {
        if ("LOGIN".equals(msg.getType())) {
            // Validate user credentials via the database
            boolean valid = ChatHistory.validateLogin(msg.getSender(), msg.getContent());
            if (valid) {
                this.username = msg.getSender();

                // Requirement (b): Implement thread influencing
                if ("admin".equalsIgnoreCase(username)) {
                    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                    System.out.println(">> Thread priority set to MAX for Admin: " + username);
                } else {
                    Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
                }

                // Register the handler in the thread-safe user manager
                userManager.registerHandler(username, this);
                sendMessage(new Message("Server", "Login Successful", "SUCCESS"));

                // Requirement (e): Trigger server to broadcast updated list to everyone
                server.broadcastUserList();

                System.out.println("User registered in system: " + username);
            } else {
                sendMessage(new Message("Server", "Invalid Credentials", "FAIL"));
            }
        } else if ("CHAT".equals(msg.getType())) {
            System.out.println("Broadcast from " + username + ": " + msg.getContent());

            // Requirement (g): Save message to "General Discussion" topic only
            ChatHistory.saveMessage(msg, "General Discussion");

            // Broadcast the message to all active users
            userManager.broadcast(msg);
        }
    }

    /**
     * Requirement: Overloaded sendMessage for String data (User List updates).
     * @param rawMessage The raw string to send.
     */
    public void sendMessage(String rawMessage) {
        try {
            out.writeObject(rawMessage);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error sending raw string to " + username);
        }
    }

    /**
     * Sends a message object back to the specific client.
     * @param msg The message to send.
     */
    public void sendMessage(Message msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error sending message to " + username);
        }
    }

    /**
     * Performs cleanup by unregistering the user and closing the socket.
     */
    private void closeConnection() {
        if (username != null) {
            userManager.unregisterHandler(username);
            // Requirement (e): Notify server to remove client and update list
            server.removeClient(this);
        }
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            // Socket already closed
        }
    }
}