package serverrestore;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import OracleConnect.DmlExecute;
import OracleConnect.FunctionCall;
import errorlog.LogError;
import java.sql.Types;

public class ServerRestore {
    
    private static final String FILE_DIRECTORY =
            "C:\\oraclexe\\app\\oracle\\product\\11.2.0\\server\\rdbms\\xml\\";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        // Create ServerSocket to await conection from client
        ServerSocket serverSocket = null;
        // Variable declaration.
        LogError logError = new LogError();
        FunctionCall functionCall = new FunctionCall();
        Socket socket = null;
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;
        OutputStream outputStream = null;
        BufferedWriter bufferedWriter = null;
        String userId = "0";
        boolean userExists = false;
        boolean userBackedUp = false;
        
        // Register your connection service on a port.
        try {
            serverSocket = new ServerSocket(5433);
        
            // Run the listen/accept connection loop forever
            while (true) {
                try {
                    System.out.println("Awaiting connection!");
                    // Wait here and listen for a connection from client.
                    socket = serverSocket.accept();
                    System.out.println("Connection made!");
                    
                    // Create input stream to receive data from client.
                    inputStream = socket.getInputStream();
                    inputStreamReader = new InputStreamReader(inputStream);
                    bufferedReader = new BufferedReader(inputStreamReader);
                    
                    // Catch the user identification sent from the client.
                    userId = bufferedReader.readLine();
                    
                    if (Integer.parseInt(userId) > 0) {     
                    
                        System.out.println("User Id:" + userId);
                    
                        // Check the user id against the user database.
                        String sqlStmt = "SELECT nvl(backup_result,'NO_BACKUP') from " +
                                "users_xml where user_id = " + userId;
                        // Execute the DML statement and store the results in an array
                        // of hashmaps. Each map stores one row with associated data types.
                        ArrayList<HashMap<Integer, Object>> dataStore = new DmlExecute().
                                executeDML(sqlStmt, userId);
                    
                        // For each hashmap (row) in the arraylist.
                        for (HashMap<Integer, Object> row : dataStore ) {
                            // Record exists therefore the user must exist.
                            userExists = true;
                            
                            // For each entry in the current hashmap.
                            for (Map.Entry<Integer, Object> entry : row.entrySet()) {
                            
                                if (entry.getKey() == Types.VARCHAR) {
                                    if (entry.getValue().equals("SUCCESS")) {
                                        userBackedUp = true;
                                    }
                                }
                            }
                        }
                    
                        // Setup the output stream to send data to the client;
                        outputStream = socket.getOutputStream();
                        bufferedWriter = new BufferedWriter(
                                new OutputStreamWriter(outputStream));
                    
                        // Inform client whether user exists and if they have already backed up.
                        bufferedWriter.write(String.valueOf(userExists) + "\n");
                        bufferedWriter.write(String.valueOf(userBackedUp) + "\n");
                        bufferedWriter.flush();
                 
                        // If the user id is valid and the user has already backed up their data.
                        if (userExists && userBackedUp) {
                        
                            // Call oracle database and create the user's restore file and
                            // ready it for transfer.
                            String fileName = functionCall.callFunction(
                                    new String[] {"create_xml_file",
                                                  userId,
                                                  "int"}, userId);
                        
                            if (!fileName.equals("0")) {
                            
                                // XML file was created, inform the client.
                                bufferedWriter.write("true" + "\n");
                                bufferedWriter.flush();
                            
                                // Get the file to be transferred.
                                File file = new File(FILE_DIRECTORY, fileName);
                        
                                // Define a byte array which will temporarily contain the file data.
                                byte[] byteArray = new byte[(int) file.length()];
                        
                                // Send the size of the data file to the client.
                                // "\n" char indicates end of line to be read by client.
                                bufferedWriter.write(byteArray.length + "\n");
                                bufferedWriter.flush();
                        
                                // Define a FileInputStream and BufferedInputStream to read
                                // the file and store it in the byte array.
                                fileInputStream = new FileInputStream(file);
                                bufferedInputStream = new BufferedInputStream(fileInputStream);
                                int bytesRead = bufferedInputStream.read(
                                        byteArray, 0, byteArray.length);
                            
                                // Wait for the send message from the client, indicates
                                // client is ready to receive the xml restore file.
                                String send = bufferedReader.readLine();
                        
                                // Use the OutputStream to send the file to the Client side.
                                outputStream.write(byteArray, 0, bytesRead);
                           
                                // Let the client know that all file data has now been sent
                                // otherwise server socket will just hang waiting for data to be sent.
                                socket.shutdownOutput();
                            
                                // Check that client received the restore file.
                                String fileReceived = bufferedReader.readLine();
                            
                                if (!Boolean.parseBoolean(fileReceived)) {
                                    // Log error that client did not receive the restore file.
                                    logError.writeToFile("File transfer to client failed", userId);
                                }
                            } else {
                                // XML file was not created, inform the client.
                                bufferedWriter.write("false" + "\n");
                                bufferedWriter.flush();
                            
                            }
                        
                        }
                    
                    }
                    
                } catch (IOException e) {
                    logError.writeToFile(e.toString(), userId);
                    
                } finally {
                    if (bufferedInputStream != null) {
                        try {
                            bufferedInputStream.close();
                        } catch (IOException e) {
                            logError.writeToFile(e.toString(), userId);
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {
                            logError.writeToFile(e.toString(), userId);
                        }
                    }
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
                // Reset variables for next iteration of loop.
                userId = "0";
                userExists = false;
                userBackedUp = false;
            }
            
        } catch (IOException e) {
            logError.writeToFile(e.toString(), userId);
                    
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    logError.writeToFile(e.toString(), userId);
                }
            }
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
