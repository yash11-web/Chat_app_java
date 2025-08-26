import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 1234;

    // roomNo -> (username -> writer)
    private static Map<Integer, Map<String, PrintWriter>> rooms = new HashMap<>();

    public static void main(String[] args) {
        System.out.println(" Chat server started on port " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;
        private int roomNo;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Ask room number
                out.println("Enter room number:");
                roomNo = Integer.parseInt(in.readLine());

                // Ask username
                out.println("Enter your username:");
                username = in.readLine();

                synchronized (rooms) {
                    rooms.putIfAbsent(roomNo, new HashMap<>());
                    rooms.get(roomNo).put(username, out);
                }

                broadcast( + username + " joined the chat!", roomNo);

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equalsIgnoreCase("/exit")) {
                        break; // user exits
                    }
                    broadcast(username + ": " + message, roomNo);
                }
            } catch (IOException e) {
                System.out.println("Connection lost with " + username);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (rooms) {
                    if (rooms.containsKey(roomNo)) {
                        rooms.get(roomNo).remove(username);
                        if (rooms.get(roomNo).isEmpty()) {
                            rooms.remove(roomNo); // cleanup empty room
                        }
                    }
                }
                broadcast(  username + " left the chat.", roomNo);
            }
        }

        private void broadcast(String message, int roomNo) {
            synchronized (rooms) {
                if (rooms.containsKey(roomNo)) {
                    for (PrintWriter writer : rooms.get(roomNo).values()) {
                        writer.println(message);
                    }
                }
            }
            System.out.println("[Room " + roomNo + "] " + message); // print on server console
        }
    }
}
