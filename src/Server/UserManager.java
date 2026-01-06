package Server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class UserManager {
    private final Map<String, ClientHandler> activeHandlers = new HashMap<>();
    //d. Implement liveness problems (Starvation & Deadlock Prevention)
    //f. Implement the Lock interface
    private final Lock lock = new ReentrantLock();

    public void registerHandler(String username, ClientHandler handler) {
        lock.lock();
        try {
            activeHandlers.put(username, handler);
        } finally {
            lock.unlock();
        }
    }

    public void unregisterHandler(String username) {
       // e. Implement synchronizing access to critical sections
        lock.lock();
        try {
            activeHandlers.remove(username);
        } finally {
            lock.unlock();
        }
    }

    // Broadcast message to all connected clients
    public void broadcast(common.Message msg) {
            lock.lock();
            try {
                // g. Implement parallel Streams (Reductions, pipelines)
                // We convert the values to a stream and send messages in parallel
                activeHandlers.values().parallelStream().forEach(handler -> {
                    handler.sendMessage(msg);
                });
            } finally {
                lock.unlock();
            }
    }
}
