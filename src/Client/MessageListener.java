package Client;

import common.Message;
import java.io.ObjectInputStream;

public class MessageListener implements Runnable {
    private ObjectInputStream in;
    private ClientApp clientApp;

    public MessageListener(ObjectInputStream in, ClientApp clientApp) {
        this.in = in;
        this.clientApp = clientApp;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object obj = in.readObject();
                if (obj instanceof Message) {
                    Message msg = (Message) obj;
                    clientApp.handleIncomingMessage(msg);
                }
            }
        } catch (Exception e) {
            System.out.println("Disconnected from server.");
        }
    }
}