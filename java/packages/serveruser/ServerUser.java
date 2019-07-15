package serveruser;

import OracleConnect.FunctionCall;
import errorlog.LogError;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerUser {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        OutputStream outputStream = null;
        BufferedWriter bufferedWriter = null;
        LogError logError = new LogError();
        FunctionCall functionCall = new FunctionCall();
        String userId = "0";
        String userName;
        String rtnUserId = "0";
        String request;
        
        // Create ServerSocket to await conection from client
        ServerSocket serverSocket;
        
        // Register your connection service on a port.
        try {
            serverSocket = new ServerSocket(5434);
        
            // Run the listen/accept connection loop forever
            while (true) {
                try {
                    System.out.println("Awaiting connection!");
                    // Wait here and listen for a connection from client.
                    Socket socket = serverSocket.accept();
                    System.out.println("Connection made!");
                        
                    // Create InputStream to receive data through the socket conection.
                    inputStream = socket.getInputStream();
                    inputStreamReader = new InputStreamReader(inputStream);
                    // Create a buffered reader to be able to read data from the unput stream.
                    bufferedReader = new BufferedReader(inputStreamReader);

                    // Catch the userId sent from the client.
                    userId = bufferedReader.readLine();
                    // Catch the user name sent from the client.
                    userName = bufferedReader.readLine();
                    // Catch the user request from the client.
                    // 'new' - create new user.
                    // 'modify' - modify existing user.
                    request = bufferedReader.readLine();
                    
                    // Access the database and call function to process the
                    // user information.
                    switch (request) {
                        
                        case "new":
                            // Insert the user into the database.
                            rtnUserId = functionCall.callFunction(
                                    new String[] {"pkg_user_handling.create_user",
                                                userName,
                                                "string"}, "0");
                            break;
                            
                        case "modify":
                            // Modify the username for the user.
                            rtnUserId = functionCall.callFunction(
                                    new String[] {"pkg_user_handling.modify_user",
                                                userId,
                                                "int",
                                                userName,
                                                "string"}, userId);
                            break;
                    }
                    System.out.println("User Id:" + rtnUserId);
                    // Create an output stream to send back the returned userId of the
                    // Oracle function call.
                    outputStream = socket.getOutputStream();
                    bufferedWriter = new BufferedWriter(
                            new OutputStreamWriter(outputStream));
                    bufferedWriter.write(rtnUserId + "\n");
                    bufferedWriter.flush();
                    
                    // Reset the userId ready for the next iteration of the await loop.
                    userId = "0";
                    rtnUserId = "0";
                        
                } catch (IOException e) {
                    logError.writeToFile(e.toString(), userId);
                    
                } finally {
                    if (bufferedWriter != null) {
                        try {
                            bufferedWriter.close();
                        } catch (IOException e) {
                            logError.writeToFile(e.toString(), userId);
                        }
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            logError.writeToFile(e.toString(), userId);
                        }
                    }
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e) {
                            logError.writeToFile(e.toString(), userId);
                        }
                    }
                    if (inputStreamReader != null) {
                        try {
                            inputStreamReader.close();
                        } catch (IOException e) {
                            logError.writeToFile(e.toString(), userId);
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            logError.writeToFile(e.toString(), userId);
                        }
                    }
                }
            }
            
        } catch (IOException e) {
            logError.writeToFile(e.toString(), userId);
        }
    }
    
}
