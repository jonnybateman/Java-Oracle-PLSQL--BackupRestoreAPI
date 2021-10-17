package serveruser;

import errorlog.LogError;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerUser {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        LogError logError = new LogError();
        String userId = "0";

        // Create ServerSocket to await conection from client
        ServerSocket serverSocket = null;

        // Register your connection service on a port.
        try {
            serverSocket = new ServerSocket(5434);
            int attempts = 1;
            boolean processUserConn = true;

            // Run the listen/accept connection loop forever
            while (processUserConn) {
                Socket socket = null;

                try {
                    System.out.println("Awaiting connection!");
                    // Wait here and listen for a connection from client.
                    socket = serverSocket.accept();

                    // Connection has been made by client, create new thread to
                    // process the connection request.
                    new Thread(
                            new ProcessUserThread(socket)).start();

                    System.out.println("Connection made!");

                } catch (IOException e1) {
                    logError.writeToFile(e1.toString(), userId);

                    // If 3 attempts have been made to process user connections then quit.
                    if (attempts == 3) {
                        processUserConn = false;
                    }
                    attempts++;

                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException e2) {
                            logError.writeToFile(e2.toString(), userId);
                        }
                    }
                }
            }

        } catch (IOException e) {
            logError.writeToFile(e.toString(), userId);

        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    logError.writeToFile(e.toString(), userId);
                }
            }
        }
    }
}
