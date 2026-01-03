package Client;

import common.Message;
import java.io.ObjectInputStream;

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
                Object obj = in.readObject();
                if (obj instanceof Message) {
                    app.handleIncomingMessage((Message) obj);
                }
            }
        } catch (Exception e) {
            System.out.println("Disconnected from server.");
        }
    }
}
