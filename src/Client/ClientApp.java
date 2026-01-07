package Client;

import common.Message;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

public class ClientApp extends JFrame {

    private static final String SERVER_ADDRESS = "10.175.27.240";
    private static final int SERVER_PORT = 12345;
    private final Map<String, java.util.List<Message>> localHistory = new HashMap<>();
    private String currentTopic = "";

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;

    private CardLayout cardLayout;
    private JPanel mainPanel;

    // Login UI components
    private JTextField userField;
    private JPasswordField passField;

    // Chat UI components
    private JPanel chatPanel;
    private JScrollPane scrollPane;
    private JTextField messageField;
    private JLabel statusLabel;

    // Online users UI components
    private DefaultListModel<String> onlineModel;
    private JList<String> onlineList;

    // Design Colors
    private final Color BG_MAIN = new Color(31, 41, 51);
    private final Color BG_PANEL = new Color(45, 55, 72);
    private final Color BG_INPUT = new Color(55, 65, 81);
    private final Color ACCENT = new Color(59, 130, 246);
    private final Color BUBBLE_OTHER = new Color(75, 85, 99);
    private final Color TEXT_MAIN = new Color(229, 231, 235);
    private final Color TEXT_SUB = new Color(156, 163, 175);

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    public ClientApp() {
        super("Real-Time Chat");
        setSize(720, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createLoginPanel(), "LOGIN");
        mainPanel.add(createLobbyPanel(), "LOBBY");
        mainPanel.add(createChatPanel(), "CHAT");

        add(mainPanel);
        setVisible(true);
    }

    /* ================= LOGIN PANEL ================= */

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_MAIN);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_PANEL);
        card.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("Welcome Back");
        title.setForeground(TEXT_MAIN);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        userField = styledTextField("Username");
        passField = new JPasswordField();
        stylePasswordField(passField);

        JButton loginBtn = new JButton("Login");
        styleButton(loginBtn);
        loginBtn.addActionListener(this::performLogin);

        card.add(title);
        card.add(Box.createVerticalStrut(20));
        card.add(userField);
        card.add(Box.createVerticalStrut(10));
        card.add(passField);
        card.add(Box.createVerticalStrut(20));
        card.add(loginBtn);

        panel.add(card);
        return panel;
    }

    /* ================= CHAT LOBBY PANEL ================= */

    private JPanel createLobbyPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_MAIN);
        panel.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel title = new JLabel("My Chatrooms");
        title.setForeground(TEXT_MAIN);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(title, BorderLayout.NORTH);

        JPanel roomsContainer = new JPanel();
        roomsContainer.setLayout(new BoxLayout(roomsContainer, BoxLayout.Y_AXIS));
        roomsContainer.setBackground(BG_MAIN);

        String[] topics = {"General Discussion"};
        for (String topic : topics) {
            JButton roomBtn = new JButton(topic);
            styleButton(roomBtn);
            roomBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
            roomBtn.setMaximumSize(new Dimension(300, 50));

            roomBtn.addActionListener(e -> {
                currentTopic = topic;
                setTitle("Room: " + topic + " - " + username);
                chatPanel.removeAll();
                if (localHistory.containsKey(topic)) {
                    for (Message m : localHistory.get(topic)) {
                        addChatBubble(m);
                    }
                }
                cardLayout.show(mainPanel, "CHAT");
                chatPanel.revalidate();
                chatPanel.repaint();
            });

            roomsContainer.add(roomBtn);
            roomsContainer.add(Box.createVerticalStrut(10));
        }

        panel.add(new JScrollPane(roomsContainer), BorderLayout.CENTER);
        return panel;
    }

    /* ================= CHAT ROOM PANEL ================= */

    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(BG_MAIN);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton backBtn = new JButton("← Back");
        styleButton(backBtn);
        backBtn.setBackground(BG_PANEL);
        backBtn.addActionListener(e -> {
            cardLayout.show(mainPanel, "LOBBY");
            setTitle("Lobby - " + username);
        });

        JLabel header = new JLabel(" Real-Time Chat");
        header.setForeground(TEXT_MAIN);
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));

        statusLabel = new JLabel(" ● Online");
        statusLabel.setForeground(new Color(34, 197, 94));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_MAIN);

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        leftHeader.setBackground(BG_MAIN);
        leftHeader.add(backBtn);
        leftHeader.add(header);

        headerPanel.add(leftHeader, BorderLayout.WEST);
        headerPanel.add(statusLabel, BorderLayout.EAST);

        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(BG_PANEL);

        scrollPane = new JScrollPane(chatPanel);
        scrollPane.setBorder(null);

        onlineModel = new DefaultListModel<>();
        onlineList = new JList<>(onlineModel);
        onlineList.setBackground(BG_PANEL);
        onlineList.setForeground(TEXT_MAIN);
        onlineList.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JPanel onlinePanel = new JPanel(new BorderLayout());
        onlinePanel.setPreferredSize(new Dimension(160, 0));
        onlinePanel.setBackground(BG_PANEL);
        JLabel onlineTitle = new JLabel(" Online Users");
        onlineTitle.setForeground(TEXT_SUB);
        onlineTitle.setBorder(new EmptyBorder(5, 5, 5, 5));
        onlinePanel.add(onlineTitle, BorderLayout.NORTH);
        onlinePanel.add(new JScrollPane(onlineList), BorderLayout.CENTER);

        messageField = styledTextField("Type a message...");
        JButton sendBtn = new JButton("Send");
        styleButton(sendBtn);
        sendBtn.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());

        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(BG_MAIN);
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);

        JPanel center = new JPanel(new BorderLayout());
        center.add(headerPanel, BorderLayout.NORTH);
        center.add(scrollPane, BorderLayout.CENTER);
        center.add(inputPanel, BorderLayout.SOUTH);

        panel.add(center, BorderLayout.CENTER);
        panel.add(onlinePanel, BorderLayout.EAST);

        return panel;
    }

    /* ================= UI BUBBLE LOGIC ================= */

    private void addChatBubble(Message msg) {
        boolean isMe = msg.getSender().equals(username);

        RoundedPanel bubble = new RoundedPanel(isMe ? ACCENT : BUBBLE_OTHER);
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBorder(new EmptyBorder(8, 14, 8, 14));
        bubble.setMaximumSize(new Dimension(420, Integer.MAX_VALUE));

        JLabel senderLabel = new JLabel(isMe ? "You" : msg.getSender());
        senderLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        senderLabel.setForeground(TEXT_SUB);

        JLabel contentLabel = new JLabel("<html>" + msg.getContent() + "</html>");
        contentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        contentLabel.setForeground(Color.WHITE);

        JLabel timeLabel = new JLabel(timeFormat.format(new Date(msg.getTimestamp())));
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLabel.setForeground(TEXT_SUB);
        timeLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        bubble.add(senderLabel);
        bubble.add(contentLabel);
        bubble.add(Box.createVerticalStrut(4));
        bubble.add(timeLabel);

        JPanel wrapper = new JPanel(new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT));
        wrapper.setBackground(BG_PANEL);
        wrapper.add(bubble);

        chatPanel.add(wrapper);
        chatPanel.add(Box.createVerticalStrut(8));

        SwingUtilities.invokeLater(() ->
                scrollPane.getVerticalScrollBar().setValue(
                        scrollPane.getVerticalScrollBar().getMaximum()
                ));
        chatPanel.revalidate();
    }

    /* ================= STYLING HELPERS ================= */

    private JTextField styledTextField(String hint) {
        JTextField field = new JTextField();
        field.setBackground(BG_INPUT);
        field.setForeground(TEXT_MAIN);
        field.setCaretColor(TEXT_MAIN);
        field.setBorder(new EmptyBorder(10, 10, 10, 10));
        return field;
    }

    private void stylePasswordField(JPasswordField field) {
        field.setBackground(BG_INPUT);
        field.setForeground(TEXT_MAIN);
        field.setCaretColor(TEXT_MAIN);
        field.setBorder(new EmptyBorder(10, 10, 10, 10));
    }

    private void styleButton(JButton btn) {
        btn.setBackground(ACCENT);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /* ================= CORE LOGIC ================= */

    private void performLogin(ActionEvent e) {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Launch background thread to listen for server updates
            new Thread(new MessageListener(in, this)).start();

            username = userField.getText();
            out.writeObject(new Message(username, new String(passField.getPassword()), "LOGIN"));
            out.flush();

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Cannot connect to server at " + SERVER_ADDRESS);
        }
    }

    // Public method to allow MessageListener to push data to UI
    public void handleIncomingObject(Object obj) {
        if (obj instanceof String) {
            String raw = (String) obj;
            // Handle User List synchronization updates
            if (raw.startsWith("UPDATE_USERS:")) {
                String[] users = raw.replace("UPDATE_USERS:", "").split(",");
                SwingUtilities.invokeLater(() -> {
                    onlineModel.clear();
                    for (String u : users) {
                        if (!u.isEmpty()) onlineModel.addElement(u);
                    }
                });
            }
        } else if (obj instanceof Message) {
            Message msg = (Message) obj;
            if ("SUCCESS".equals(msg.getType())) {
                cardLayout.show(mainPanel, "LOBBY");
                setTitle("Lobby - " + username);
            } else if ("CHAT".equals(msg.getType())) {
                String topic = "General Discussion";
                localHistory.putIfAbsent(topic, new ArrayList<>());
                localHistory.get(topic).add(msg);

                if (topic.equals(currentTopic)) {
                    SwingUtilities.invokeLater(() -> addChatBubble(msg));
                }
            }
        }
    }

    private void sendMessage() {
        try {
            String text = messageField.getText().trim();
            if (!text.isEmpty()) {
                out.writeObject(new Message(username, text, "CHAT"));
                out.flush();
                messageField.setText("");
            }
        } catch (IOException e) {
            statusLabel.setText(" ● Disconnected");
            statusLabel.setForeground(Color.RED);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientApp::new);
    }

    /* ================= UI CUSTOM COMPONENTS ================= */

    static class RoundedPanel extends JPanel {
        private final Color bg;
        RoundedPanel(Color bg) {
            this.bg = bg;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
            super.paintComponent(g);
        }
    }
}