package Client;

import common.Message;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Socket;

public class ClientApp extends JFrame {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    // Networking
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;

    // GUI Components
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JTextField userField;
    private JPasswordField passField;
    private JTextArea chatArea;
    private JTextField messageField;

    public ClientApp() {
        super("Real-Time Chat Application");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // 1. Login Panel
        JPanel loginPanel = new JPanel(new GridLayout(3, 2));
        userField = new JTextField();
        passField = new JPasswordField();
        JButton loginButton = new JButton("Login");

        loginPanel.add(new JLabel("Username:"));
        loginPanel.add(userField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(passField);
        loginPanel.add(new JLabel(""));
        loginPanel.add(loginButton);

        loginButton.addActionListener(this::performLogin);

        // 2. Chat Panel
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        messageField = new JTextField();
        JButton sendButton = new JButton("Send");
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        chatPanel.add(bottomPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage()); // Enter key sends

        // Add cards
        mainPanel.add(loginPanel, "LOGIN");
        mainPanel.add(chatPanel, "CHAT");

        add(mainPanel);
        setVisible(true);
    }

    private void performLogin(ActionEvent e) {
        String user = userField.getText();
        String pass = new String(passField.getPassword());

        try {
            // Establish Connection
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Start Listener Thread immediately
            new Thread(new MessageListener(in, this)).start();

            // Send Login Request
            // NOTE: We use "content" field for password in this simple implementation
            out.writeObject(new Message(user, pass, "LOGIN"));
            out.flush();

            this.username = user;

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Cannot connect to server.");
        }
    }

    private void sendMessage() {
        String content = messageField.getText();
        if (!content.isEmpty()) {
            try {
                out.writeObject(new Message(username, content, "CHAT"));
                out.flush();
                messageField.setText("");
            } catch (IOException e) {
                chatArea.append("Failed to send message.\n");
            }
        }
    }

    // Called by MessageListener when data arrives
    public void handleIncomingMessage(Message msg) {
        if ("SUCCESS".equals(msg.getType())) {
            cardLayout.show(mainPanel, "CHAT");
            setTitle("Chat - " + username);
        } else if ("FAIL".equals(msg.getType())) {
            JOptionPane.showMessageDialog(this, "Login Failed: " + msg.getContent());
        } else if ("CHAT".equals(msg.getType())) {
            chatArea.append(msg.getSender() + ": " + msg.getContent() + "\n");
            // Auto-scroll
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        }
    }

    //h. Test the concurrent programs
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientApp::new);
    }
}