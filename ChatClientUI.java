import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class ChatClientUI {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 1234;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private int roomNo;

    // UI components
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private JButton exitButton;

    public ChatClientUI() {
        // Ask for room number
        String roomStr = JOptionPane.showInputDialog(frame, "Enter room number:");
        try {
            roomNo = Integer.parseInt(roomStr);
        } catch (Exception e) {
            roomNo = 1; // default
        }

        // Ask for username
        username = JOptionPane.showInputDialog(frame, "Enter your username:");
        if (username == null || username.trim().isEmpty()) {
            username = "User" + (int)(Math.random() * 1000);
        }

        // Build UI
        frame = new JFrame("Chat - " + username + " [Room " + roomNo + "]");
        chatArea = new JTextArea(20, 40);
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);

        inputField = new JTextField(25);
        sendButton = new JButton("Send");
        exitButton = new JButton("Exit");

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(inputField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(sendButton);
        buttonPanel.add(exitButton);
        panel.add(buttonPanel, BorderLayout.EAST);

        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        frame.add(panel, BorderLayout.SOUTH);

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Events
        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage()); // press Enter
        exitButton.addActionListener(e -> exitChat());
    }

    private void startClient() {
        try {
            socket = new Socket(SERVER_ADDRESS, PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Send room no & username
            in.readLine(); // "Enter room number:"
            out.println(roomNo);
            in.readLine(); // "Enter your username:"
            out.println(username);

            // Thread to receive messages
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        chatArea.append(message + "\n");
                    }
                } catch (IOException e) {
                    chatArea.append(" Disconnected from server.\n");
                }
            }).start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Could not connect to server!", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            out.println(message);
            inputField.setText("");
        }
    }

    private void exitChat() {
        out.println("/exit");
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        frame.dispose();
        System.exit(0);
    }

    public static void main(String[] args) {
        ChatClientUI client = new ChatClientUI();
        client.startClient();
    }
}
