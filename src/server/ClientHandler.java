package server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

class ClientHandler extends Thread {

    private DataInputStream inputStream = null;
    private PrintStream outputStream = null;
    private Socket clientSocket = null;
    private final ClientHandler[] threads;
    private int maxClientsCount;
    private String userName;

    public ClientHandler(Socket clientSocket, ClientHandler[] threads) {
        this.clientSocket = clientSocket;
        this.threads = threads;
        maxClientsCount = threads.length;
    }

    @Override
    public void run() {
        int maxClientsCount = this.maxClientsCount;
        ClientHandler[] threads = this.threads;

        // Creates input and output streams for the given client
        try {
            inputStream = new DataInputStream(clientSocket.getInputStream());
            outputStream = new PrintStream(clientSocket.getOutputStream());

            outputStream.println("Please enter your desired userName\n" +
                    "It has to be 1-12 characters, and may contain letters, digits, '-' and '_'.");
            userName = inputStream.readLine().trim();

            /*
             * Completes a few checks on the requested userName from the client.
             * Checks that the username is not already taken, that it's between 1-12 characters,
             * and that it does not contain any of the special chars, and therefore only the allowed
             * letters, digits, '-' and '_'
             */
            while (true) {

                if (takenUsername(userName)) {
                    outputStream.println("J_ER 1: Username is already taken, please try again!");
                    userName = inputStream.readLine().trim();
                } else if (userName.matches("[!@#$%&*()+=|<>?{}\\[\\]~]+")) {
                    outputStream.println("J_ER 2: You may only use the above listed characters, please try again!");
                    userName = inputStream.readLine().trim();
                } else if (userName.length() < 1 || userName.length() > 12 || userName.contains(" ")) {
                    outputStream.println("J_ER 3: Your username must be between 1-12 characters, please try again!");
                    userName = inputStream.readLine().trim();
                } else {
                    break;
                }
            }

            outputStream.println("J_OK\nHello " + userName + ". Welcome to the Chat Server!" +
                    "\nIf you wish to leave you can type QUIT in a new line.\n" +
                    "If you wish to see a list of chatters you can type LIST in a new line.");

            // Sends a message to every client when a new client joins the chat room
            for (int i = 0; i < maxClientsCount; i++) {

                if (threads[i] != null && threads[i] != this) {
                    threads[i].outputStream.println("*** " + userName + " has joined the chat room! ***");
                }
            }
            // Handles messages from the client. Breaks out of the while loop if it starts with QUIT
            while (true) {

                String inFromClient = inputStream.readLine();

                if (inFromClient.length() > 250) {
                    outputStream.println("Message too long! May not be longer than 250 characters");
                    inFromClient = "";
                }

                if (inFromClient.equals("")) {
                    outputStream.println("Please don't try to send empty messages!");
                }

                if (inFromClient.startsWith("QUIT")) {
                    break;
                }

                if (inFromClient.startsWith("LIST")) {
                    String listOfClients = "";

                    for (ClientHandler ch : threads) {
                        if (ch != null) {
                            listOfClients += ch.userName + " ";
                        }
                    }
                    outputStream.println("List of active chatters: " + listOfClients);
                    inFromClient = "";
                }

                // Sends the message from a client, to all other clients, unless it is one of the above commands.
                if (!inFromClient.equals("")) {
                    for (int i = 0; i < maxClientsCount; i++) {
                        if (threads[i] != null) {
                            threads[i].outputStream.println(userName + ": " + inFromClient);
                        }
                    }
                }
            }

            // Sends a message to all clients, when a client has quit the chat room
            for (int i = 0; i < maxClientsCount; i++) {
                if (threads[i] != null && threads[i] != this) {
                    threads[i].outputStream.println("*** " + userName + " has left the chat room! ***");
                }
            }
            outputStream.println("*** Bye " + userName + " ***");

            // sets the client that quit's thread to null, in order for it to be used by another chatter.
            for (int i = 0; i < maxClientsCount; i++) {
                if (threads[i] == this) {
                    threads[i] = null;
                }
            }

            // Closes the input/output stream, as well as the socket.
            inputStream.close();
            outputStream.close();
            clientSocket.close();

        } catch (IOException IOE) {
            System.out.println("IOException: " + IOE);
        }
    }

    // Checks to see if the provided userName is already taken.
    public boolean takenUsername(String userName) {

        for (int i = 0; i < maxClientsCount; i++) {
            if (threads[i] != null && threads[i].userName.equals(userName) && threads[i] != this) {
                return true;
            }
        }
        return false;
    }
}
