package Server;

import common.Message;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//a. Create thread and runnable objects
public class ClientHandler implements Runnable {
    private Socket socket;
    private UserManager userManager;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;
    private boolean isRunning = true;

    public ClientHandler(Socket socket, UserManager userManager) {
        this.socket = socket;
        this.userManager = userManager;
    }

    @Override
    public void run() {
        try {
            // Initialize streams
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
                    break; // Client disconnected
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Connection lost: " + username);
        } finally {
            closeConnection();
        }
    }

    private void processMessage(Message msg) {
        if ("LOGIN".equals(msg.getType())) {
            boolean valid = ChatHistory.validateLogin(msg.getSender(), msg.getContent());
            if (valid) {
                this.username = msg.getSender();

                //b. Implement thread influencing
                // If the user is admin, we boost their thread priority so they get CPU preference
                if ("admin".equalsIgnoreCase(username)) {
                    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                    System.out.println(">> Priority set to MAX for Admin");
                } else {
                    Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
                }

                userManager.registerHandler(username, this);
                sendMessage(new Message("Server", "Login Successful", "SUCCESS"));
                System.out.println("User logged in: " + username);
            } else {
                sendMessage(new Message("Server", "Invalid Credentials", "FAIL"));
            }
        } else if ("CHAT".equals(msg.getType())) {
            System.out.println(username + ": " + msg.getContent());
            ChatHistory.saveMessage(msg); // Save to DB
            userManager.broadcast(msg);   // Send to everyone
        }
    }

    public void sendMessage(Message msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeConnection() {
        if (username != null) {
            userManager.unregisterHandler(username);
        }
        try { if (socket != null) socket.close(); } catch (IOException e) {}
    }

    private final Lock lock = new ReentrantLock(true);
}