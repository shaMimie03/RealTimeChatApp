package Client;

import common.Message;
import java.io.ObjectInputStream;

/**
 * MessageListener runs on a background thread to continuously
 * listen for incoming objects from the server.
 */
public class MessageListener implements Runnable {
    private ObjectInputStream in;
    private ClientApp app;

    public MessageListener(ObjectInputStream in, ClientApp app) {
        this.in = in;
        this.app = app;
    }

    @Override
    public void run() {
        try {
            while (true) {
                // Requirement (e): Handling different object types for synchronization
                Object obj = in.readObject();

                // Redirect the received object (String or Message) to the UI handler
                app.handleIncomingObject(obj);
            }
        } catch (Exception e) {
            System.out.println("Connection to server closed.");
        }
    }
}