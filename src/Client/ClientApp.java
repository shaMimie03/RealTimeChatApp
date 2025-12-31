package Client;

import common.Message;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
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

    // Layout
    private CardLayout cardLayout;
    private JPanel mainPanel;

    // Login
    private JTextField userField;
    private JPasswordField passField;

    // Chat
    private JTextArea chatArea;
    private JTextField messageField;

    // Colors
    private final Color BG_MAIN = new Color(31, 41, 51);
    private final Color BG_PANEL = new Color(45, 55, 72);
    private final Color BG_INPUT = new Color(55, 65, 81);
    private final Color ACCENT = new Color(59, 130, 246);
    private final Color TEXT_MAIN = new Color(229, 231, 235);
    private final Color TEXT_SUB = new Color(156, 163, 175);

    public ClientApp() {
        super("Real-Time Chat");

        setSize(420, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(BG_MAIN);

        mainPanel.add(createLoginPanel(), "LOGIN");
        mainPanel.add(createChatPanel(), "CHAT");

        add(mainPanel);
        setVisible(true);
    }

    /* ===================== LOGIN PANEL ===================== */

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_MAIN);
        panel.setLayout(new GridBagLayout());

        JPanel card = new JPanel();
        card.setBackground(BG_PANEL);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("Welcome Back");
        title.setForeground(TEXT_MAIN);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Login to continue");
        subtitle.setForeground(TEXT_SUB);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        userField = styledTextField("Username");
        passField = new JPasswordField();
        stylePasswordField(passField);

        JButton loginBtn = new JButton("Login");
        styleButton(loginBtn);
        loginBtn.addActionListener(this::performLogin);

        card.add(title);
        card.add(Box.createVerticalStrut(5));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(25));
        card.add(userField);
        card.add(Box.createVerticalStrut(15));
        card.add(passField);
        card.add(Box.createVerticalStrut(20));
        card.add(loginBtn);

        panel.add(card);
        return panel;
    }

    /* ===================== CHAT PANEL ===================== */

    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_MAIN);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Header
        JLabel header = new JLabel(" Real-Time Chat");
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.setForeground(TEXT_MAIN);
        header.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(header, BorderLayout.NORTH);

        // Chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setBackground(BG_PANEL);
        chatArea.setForeground(TEXT_MAIN);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Input area
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(BG_MAIN);
        inputPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        messageField = styledTextField("Type a message...");
        JButton sendBtn = new JButton("Send");
        styleButton(sendBtn);

        sendBtn.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);

        panel.add(inputPanel, BorderLayout.SOUTH);
        return panel;
    }

    /* ===================== STYLING HELPERS ===================== */

    private JTextField styledTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setBackground(BG_INPUT);
        field.setForeground(TEXT_MAIN);
        field.setCaretColor(TEXT_MAIN);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(new EmptyBorder(10, 10, 10, 10));
        field.setToolTipText(placeholder);
        return field;
    }

    private void stylePasswordField(JPasswordField field) {
        field.setBackground(BG_INPUT);
        field.setForeground(TEXT_MAIN);
        field.setCaretColor(TEXT_MAIN);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(new EmptyBorder(10, 10, 10, 10));
    }

    private void styleButton(JButton btn) {
        btn.setBackground(ACCENT);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
    }

    /* ===================== LOGIC (UNCHANGED) ===================== */

    private void performLogin(ActionEvent e) {
        String user = userField.getText();
        String pass = new String(passField.getPassword());

        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            new Thread(new MessageListener(in, this)).start();
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

    public void handleIncomingMessage(Message msg) {
        if ("SUCCESS".equals(msg.getType())) {
            cardLayout.show(mainPanel, "CHAT");
            setTitle("Chat - " + username);
        } else if ("FAIL".equals(msg.getType())) {
            JOptionPane.showMessageDialog(this, "Login Failed: " + msg.getContent());
        } else if ("CHAT".equals(msg.getType())) {
            chatArea.append(msg.getSender() + ": " + msg.getContent() + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientApp::new);
    }
}
