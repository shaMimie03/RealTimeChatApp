package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private static final int PORT = 12345;
    private ExecutorService threadPool;
    private UserManager userManager;

    //  Synchronized set to manage active client handlers for broadcasting
    private static Set<ClientHandler> activeClients = Collections.synchronizedSet(new HashSet<>());

    public ChatServer() {
        // Concurrency Load (at least 30 users)
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
                ClientHandler handler = new ClientHandler(clientSocket, userManager, this);

                // Add to active clients list for broadcasting
                activeClients.add(handler);

                threadPool.execute(handler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Use Parallel Streams for efficient broadcasting
    public void broadcastUserList() {
        StringBuilder userList = new StringBuilder("UPDATE_USERS:");
        synchronized (activeClients) {
            for (ClientHandler client : activeClients) {
                if (client.getUsername() != null) {
                    userList.append(client.getUsername()).append(",");
                }
            }
        }

        // Use parallel stream to send the list to everyone instantly
        activeClients.parallelStream().forEach(client -> client.sendMessage(userList.toString()));
    }

    public void removeClient(ClientHandler handler) {
        activeClients.remove(handler);
        broadcastUserList(); // Update others when someone leaves
    }

    public static void main(String[] args) {
        // Implement joining threads
        Thread setupThread = new Thread(() -> {
            System.out.println(">> Setup Thread: Initializing Database...");
            DatabaseSetup.initializeDatabase();
        });

        setupThread.start();

        try {
            // Wait for database initialization to finish before starting server
            setupThread.join();
            System.out.println(">> Setup Thread joined. Server starting now.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        new ChatServer().start();
    }
}