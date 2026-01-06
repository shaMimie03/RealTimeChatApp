package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private static final int PORT = 12345;
    private ExecutorService threadPool;
    private UserManager userManager;

    public ChatServer() {
        // Requirement: Concurrency Load (at least 30 users)
        threadPool = Executors.newFixedThreadPool(30);
        userManager = new UserManager();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Real-Time Chat Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                // Create handler and submit to thread pool
                ClientHandler handler = new ClientHandler(clientSocket, userManager);
                threadPool.execute(handler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        //c. Implement joining threads
        Thread setupThread = new Thread(() -> {
            System.out.println(">> Setup Thread: Initializing Database...");
            DatabaseSetup.initializeDatabase();
        });

        setupThread.start();

        try {
            // The main server thread will PAUSE here until setupThread finishes completely.
            setupThread.join();
            System.out.println(">> Setup Thread joined. Server starting now.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        new ChatServer().start();
    }
}