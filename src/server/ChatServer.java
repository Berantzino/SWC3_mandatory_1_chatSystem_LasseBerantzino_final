package server;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {

    // The servers socket
    private static ServerSocket serverSocket = null;

    // The clients socket
    private static Socket clientSocket = null;

    // This chat server can accept up to maxClientsCount client's connections.
    private static final int maxClientsCount = 10;
    private static final ClientHandler[] threads = new ClientHandler[maxClientsCount];

    public static void main(String[] args) {

        int portNumber = 6969;

        System.out.println("The Chat Server is now using default port, with portNumber: " + portNumber);

        // Opens a server socket on the default portNumber.
        try {
            serverSocket = new ServerSocket(portNumber);

        } catch (IOException IOE) {
            System.out.println(IOE);
        }

        // Creates a client socket for every client that connect to the server and gives it to the client thread
        while (true) {

            try {
                clientSocket = serverSocket.accept();
                int clientsConnected = 0;

                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] == null) {
                        (threads[i] = new ClientHandler(clientSocket, threads)).start();
                        break;
                    }
                }

                // Counts the number of clients connected to the chat server
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null) {
                        clientsConnected += 1;
                    }
                }

                if (clientsConnected == maxClientsCount) {
                    PrintStream outputStream = new PrintStream(clientSocket.getOutputStream());
                    outputStream.println("Server too busy. Please try again later.");
                    outputStream.close();
                    clientSocket.close();
                }
            } catch (IOException IOE) {
                System.out.println(IOE);
            }
        }
    }
}

