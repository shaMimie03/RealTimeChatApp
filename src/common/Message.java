package common;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private String sender;
    private String content;
    private String type;
    // "LOGIN", "CHAT", "SUCCESS", "FAIL", "LOGOUT", "USER_OFFLINE"
    private long timestamp;

    public Message(String sender, String content, String type) {
        this.sender = sender;
        this.content = content;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public String getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "[" + sender + "]: " + content;
    }
}
