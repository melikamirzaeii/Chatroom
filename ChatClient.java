import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    private BufferedReader inputReader;
    private PrintWriter outputWriter;
    private String serverAddress;

    public ChatClient(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    private void start() throws IOException {
        try (Socket socket = new Socket(serverAddress, 1234)) {
            inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputWriter = new PrintWriter(socket.getOutputStream(), true);

            Scanner scanner = new Scanner(System.in);

            for (boolean isNameAccepted = false; !isNameAccepted; ) {
                String serverMessage = inputReader.readLine();
                if (serverMessage.startsWith("SUBMITNAME")) {
                    System.out.print("Enter your username: ");
                    outputWriter.println(scanner.nextLine());
                } else if (serverMessage.startsWith("NAMEACCEPTED")) {
                    System.out.println("Connected to chat server");
                    isNameAccepted = true;
                }
            }

            Thread messageReaderThread = new Thread(new MessageReader());
            messageReaderThread.start();

            for (;;) {
                outputWriter.println(scanner.nextLine());
            }
        }
    }

    private class MessageReader implements Runnable {
        public void run() {
            try {
                for (;;) {
                    String serverMessage = inputReader.readLine();
                    if (serverMessage.startsWith("MESSAGE")) {
                        System.out.println(serverMessage.substring(8));
                    }
                }
            } catch (IOException e) {
                System.out.println("Error reading from server");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        String serverAddress = "127.0.0.1"; // localhost
        ChatClient chatClient = new ChatClient(serverAddress);
        chatClient.start();
    }
}
