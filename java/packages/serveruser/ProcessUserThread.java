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
import java.net.Socket;

public class ProcessUserThread implements Runnable {

    protected Socket threadedSocket = null;

    public ProcessUserThread(Socket socket) {
        this.threadedSocket = socket;
    }

    @Override
    public void run() {

        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        OutputStream outputStream = null;
        BufferedWriter bufferedWriter = null;
        FunctionCall functionCall = new FunctionCall();
        LogError logError = new LogError();
        String userId = "0";
        String userName;
        String rtnUserId = "0";
        String request;

        try {

            // Create InputStream to receive data through the socket conection.
            inputStream = threadedSocket.getInputStream();
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
                            new String[]{"pkg_user_handling.create_user",
                                userName,
                                "string"}, "0");
                    break;

                case "modify":
                    // Modify the username for the user.
                    rtnUserId = functionCall.callFunction(
                            new String[]{"pkg_user_handling.modify_user",
                                userId,
                                "int",
                                userName,
                                "string"}, userId);
                    break;
            }

            System.out.println("User Id:" + rtnUserId);
            // Create an output stream to send back the returned userId of the
            // Oracle function call.
            outputStream = threadedSocket.getOutputStream();
            bufferedWriter = new BufferedWriter(
                    new OutputStreamWriter(outputStream));
            bufferedWriter.write(rtnUserId + "\n");
            bufferedWriter.flush();

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
            if (threadedSocket != null) {
                try {
                    threadedSocket.close();
                } catch (IOException e) {
                    logError.writeToFile(e.toString(), userId);
                }
            }
        }
    }
}
