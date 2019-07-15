package serverbackup;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import OracleConnect.FunctionCall;
import errorlog.LogError;

public class ServerBackup {

    private static final int MAX_FILE_SIZE = 1048576; // Max size of 1Mb.
    private static final String FILE_DIRECTORY =
            "C:\\oraclexe\\app\\oracle\\product\\11.2.0\\server\\rdbms\\xml\\";
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        // Variable Declaration.
        LogError logError = new LogError();
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        FileOutputStream fileOutputStream = null;
        OutputStream outputStream = null;
        BufferedWriter bufferedWriter = null;
        String userId = "";
        String filename;
        
        // Create ServerSocket to await conection from client
        ServerSocket serverSocket = null;
        Socket socket = null;
        
        // Register your connection service on a port.
        try {
            serverSocket = new ServerSocket(5432);
        
            // Run the listen/accept connection loop forever
            if (serverSocket != null) {
                while (true) {
                    try {
                        System.out.println("Awaiting connection!");
                        // Wait here and listen for a connection from client.
                        socket = serverSocket.accept();
                        System.out.println("Connection made!");
                        
                        // Create InputStream to receive data through the socket conection.
                        inputStream = socket.getInputStream();
                        inputStreamReader = new InputStreamReader(inputStream);
                        // Create a buffered reader to be able to read data from the unput stream.
                        bufferedReader = new BufferedReader(inputStreamReader);
                    
                        // Catch the user identification sent from the client.
                        userId = bufferedReader.readLine();
                    
                        // Create byte array to read the transferred file into.
                        byte[] byteArray = new byte[MAX_FILE_SIZE];
                    
                        // Create a file OutputStream to write the received data to an empty file.
                        Date date = new Date();
                        filename = dateFormat.format(date) + "_" + userId +
                                "_BACKUP.xml";
                        fileOutputStream = new FileOutputStream(FILE_DIRECTORY +
                                filename);
                    
                        // Read the data being received through the InputStream and write it
                        // to the new file via the FileOutputStream.
                        int bytesCount;  // Number of bytes read at one time.
                        int bytesTotal = 0; // Tracks the total number of bytes read.
                        // While bytes are still being read from the input stream.
                        while ((bytesCount = inputStream.read(byteArray)) > 0) {
                            bytesTotal += bytesCount;
                            fileOutputStream.write(byteArray, 0, bytesCount);
                        }
                    
                        // Access the database and call function to process the
                        // backup xml file.
                        FunctionCall functionCall = new FunctionCall();
                        String callResult =
                                functionCall.callFunction(
                                        new String[] {"process_xml_file",
                                                                filename,
                                                                "string"}, userId);
                    
                        System.out.println("call result:" + callResult);
                  
                        // Create an output stream to send the number of bytes transferred
                        // back to the client. Used for confirmation of file transfer.
                        // If bytes transferred equals that of file size then transfer of file
                        // confirmed. Also send to client result of database update.
                        outputStream = socket.getOutputStream();
                        bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                        // "\n" char indicates end of line to be read by client.
                        bufferedWriter.write(bytesTotal + "\n");
                        bufferedWriter.write(callResult + "\n");
                        bufferedWriter.flush();
                    
                    } catch (IOException e) {
                        LogError err = new LogError();
                        err.writeToFile(e.toString(), userId);
                    } finally {
                        // Close all stream components.
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e) {
                                LogError err = new LogError();
                                err.writeToFile(e.toString(), userId);
                            }
                        }
                        if (inputStreamReader != null) {
                            try {
                                inputStreamReader.close();
                            } catch (IOException e) {
                                LogError err = new LogError();
                                err.writeToFile(e.toString(), userId);
                            }
                        }
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e) {
                                LogError err = new LogError();
                                err.writeToFile(e.toString(), userId);
                            }
                        }
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e) {
                                LogError err = new LogError();
                                err.writeToFile(e.toString(), userId);
                            }
                        }
                        if (outputStream != null) {
                            try {
                                outputStream.close();
                            } catch (IOException e) {
                                LogError err = new LogError();
                                err.writeToFile(e.toString(), userId);
                            }
                        }
                        if (bufferedWriter != null) {
                            try {
                                bufferedWriter.close();
                            } catch (IOException e) {
                                LogError err = new LogError();
                                err.writeToFile(e.toString(), userId);
                            }
                        }
                    }
                }
            }
            
        } catch (IOException e) {
            LogError err = new LogError();
            err.writeToFile(e.toString(), userId);
            
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
