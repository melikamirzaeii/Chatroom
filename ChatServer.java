import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 1234;
    private static Set<String> clientNames = new HashSet<>();
    private static Set<PrintWriter> clientWriters = new HashSet<>();

    public static void main(String[] args) throws Exception {
        System.out.println("Chat Server is running on port " + PORT);
        ServerSocket serverSocket = new ServerSocket(PORT);
        try {
            for (;;) {  // Equivalent to while (true)
                new ClientHandler(serverSocket.accept()).start();
            }
        } finally {
            serverSocket.close();
        }
    }

    private static class ClientHandler extends Thread {
        private String clientName;
        private Socket clientSocket;
        private BufferedReader input;
        private PrintWriter output;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
                input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                output = new PrintWriter(clientSocket.getOutputStream(), true);

                requestClientName();

                output.println("NAMEACCEPTED " + clientName);
                notifyClientsJoined();

                handleClientMessages();
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                cleanUp();
            }
        }

        private void requestClientName() throws IOException {
            for (;;) {  // Equivalent to while (true)
                output.println("SUBMITNAME");
                clientName = input.readLine();
                if (clientName == null) {
                    return;
                }
                synchronized (clientNames) {
                    if (!clientName.isBlank() && !clientNames.contains(clientName)) {
                        clientNames.add(clientName);
                        break;
                    }
                }
            }
        }

        private void notifyClientsJoined() {
            for (PrintWriter writer : clientWriters) {
                writer.println("MESSAGE " + clientName + " has joined");
            }
            clientWriters.add(output);
        }

        private void handleClientMessages() throws IOException {
            for (;;) {  // Equivalent to while (true)
                String message = input.readLine();
                if (message == null) {
                    return;
                }
                broadcastMessage(message);
            }
        }

        private void broadcastMessage(String message) {
            for (PrintWriter writer : clientWriters) {
                writer.println("MESSAGE " + clientName + ": " + message);
            }
        }

        private void cleanUp() {
            if (clientName != null) {
                clientNames.remove(clientName);
                notifyClientsLeft();
            }
            if (output != null) {
                clientWriters.remove(output);
            }
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println(e);
            }
        }

        private void notifyClientsLeft() {
            for (PrintWriter writer : clientWriters) {
                writer.println("MESSAGE " + clientName + " has left");
            }
        }
    }
}
