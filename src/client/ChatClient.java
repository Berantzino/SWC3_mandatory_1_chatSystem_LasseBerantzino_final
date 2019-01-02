package client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatClient implements Runnable {

    // The clients socket
    private static Socket clientSocket = null;

    // The output stream
    private static PrintStream outputStream = null;

    // The input stream
    private static DataInputStream inputStream = null;

    // Used to read from keyboard/command line
    private static BufferedReader input = null;

    // Used for when the client wishes to quit the chat server
    private static boolean sessionQuit = false;

    public static void main(String[] args) {

        int portNumber = 6969;
        String host = "localhost";

        System.out.println("You're now connected using host: " + host + " and portNumber: " + portNumber);

        // Opens up a socket on the default ip and port, as well as input and out streams.
        try {
            clientSocket = new Socket(host, portNumber);
            input = new BufferedReader(new InputStreamReader(System.in));
            outputStream = new PrintStream(clientSocket.getOutputStream());
            inputStream = new DataInputStream(clientSocket.getInputStream());

        } catch (UnknownHostException UHE) {
            System.out.println("Can't resolve host: " + host);

        } catch (IOException IOE) {
            System.out.println("Could no get input/output stream for the connection to host: " + host);
        }

        /*
         * When everything has been initialized, we want to be able to write data to the socket we opened a connection
         * to on the default portNumber.
         */
        if (clientSocket != null && outputStream != null && inputStream != null) {
            try {
                // creates a thread to read data from the server and starts it.
                new Thread(new ChatClient()).start();

                while (!sessionQuit) {
                    outputStream.println(input.readLine().trim());
                }

                // Closes the input and output stream, as well as the socket, if "sessionQuit" has been set to false.
                outputStream.close();
                inputStream.close();
                clientSocket.close();

            } catch (IOException IOE) {
                System.out.println("IOException: " + IOE);
            }
        }
    }

    @Override
    public void run() {

        String receivedMessage;

        /*
         * Keeps reading from the socket, until a message from the server containing "*** Bye", has been received.
         * Once "*** Bye" has been received, we break out of the while loop
         */
        try {
            while ((receivedMessage = inputStream.readLine()) != null) {
                System.out.println(receivedMessage);

                if (receivedMessage.contains("*** Bye")) {
                    break;
                }
            }
            sessionQuit = true;
            System.exit(1);

        } catch (IOException IOE) {
            System.out.println("IOException: " + IOE);
        }
    }
}
