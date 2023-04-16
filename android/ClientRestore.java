package com.development.smartlist;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.DateFormat;

/**
 * Restore user data via a socket interface to server database.
 */

class ClientRestore extends AsyncTask<String, Void, String> {

    private static final String FILE_DIRECTORY = "/storage/emulated/0/Download/";

    void restoreData() {

        final Context context = GlobalClass.getAppContext();

        // Run as a separate thread from the main UI thread otherwise a NetworkOnMainThreadException
        // exception is thrown.
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                DBAdapter dbAdapter = new DBAdapter(context);

                Socket socket = null;
                OutputStream outputStream = null;
                BufferedWriter bufferedWriter = null;
                InputStream inputStream = null;
                FileOutputStream fileOutputStream = null;
                InputStreamReader inputStreamReader = null;
                BufferedReader bufferedReader = null;

                try {
                    // Get the userId to pass to server to identify the data of interest.
                    dbAdapter.open();
                    int userId = dbAdapter.getUserId();
                    dbAdapter.close();

                    // If a user account has been set up on client.
                    if (userId > 0) {

                        // Open connection to Backup server, at port 5433
                        InetSocketAddress socketAddress = new InetSocketAddress("192.168.1.140", 5433);
                        int timeout = 5000;
                        socket = new Socket();
                        socket.connect(socketAddress, timeout);

                        // Create an output stream to provide a channel to communicate with the Server side.
                        outputStream = socket.getOutputStream();
                        // Create a a BufferedWriter to write to the OutputStream.
                        bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

                        // Create InputStream to receive data through the socket connection.
                        inputStream = socket.getInputStream();
                        // Create InputStreamReader to read data sent from the server.
                        inputStreamReader = new InputStreamReader(inputStream);
                        bufferedReader = new BufferedReader(inputStreamReader);

                        // Generate a file name for the received xml data.
                        Date date = new Date();
                        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                        String fileName = dateFormat.format(date) + "_" + userId +
                                "_RESTORE.xml";

                        // Create a file OutputStream to write the received data to an empty file.
                        fileOutputStream = new FileOutputStream(FILE_DIRECTORY + fileName);

                        // Send message to server to request a restore of user data.
                        // "\n" char indicates end of line to be read by client.
                        bufferedWriter.write(userId + "\n");
                        bufferedWriter.flush();

                        // If the user exists on the server then
                        if (Boolean.parseBoolean(bufferedReader.readLine())) {

                            // If the user has already backed up their data.
                            if (Boolean.parseBoolean(bufferedReader.readLine())) {

                                // Check if the xml file was created by the server.
                                if (Boolean.parseBoolean(bufferedReader.readLine())) {

                                    // Get the file size from the input stream.
                                    // Read the prospective file size sent from the server.
                                    int bytes = Integer.parseInt(bufferedReader.readLine());

                                    // Create byte array to read the xml data into.
                                    byte[] byteArray = new byte[bytes];

                                    // Read the data being received through the InputStream and write it
                                    // to the new file via the FileOutputStream.
                                    int count;
                                    int countTotal = 0;

                                    // Send message to server that the client is ready to receive the xml restore file.
                                    bufferedWriter.write("send" + "\n");
                                    bufferedWriter.flush();

                                    while (true) {
                                        count = inputStream.read(byteArray);
                                        if (count < 0) break;
                                        fileOutputStream.write(byteArray, 0, count);
                                        countTotal += count;
                                    }

                                    // Check that the received file is of the correct size.
                                    if (bytes == countTotal) {

                                        // Send message to server signalling file successfully received.
                                        bufferedWriter.write("true" + "\n");
                                        bufferedWriter.flush();

                                        // Process the xml file to restore user data.
                                        boolean result = new ClientRestoreXML().restoreUserData(fileName);

                                        if (result) {
                                            doInBackground("Backup data restored!");
                                        } else {
                                            doInBackground("Error: backup data not restored!");
                                        }

                                    } else {
                                        // Send message to server signalling file was not successfully received.
                                        bufferedWriter.write("false" + "\n");
                                        bufferedWriter.flush();
                                        doInBackground("Backup file transfer failure!");
                                    }

                                } else {
                                    // Server could not create xml file.
                                    doInBackground("Backup file could not be created!");
                                }

                            } else {
                                // User account has not yet been backed up.
                                doInBackground("User account has never been backed up!");
                            }

                        } else {
                            // User does not exist server side.
                            doInBackground("User account does not exist!");
                        }

                    } else {
                        // User id has not been created yet.
                        doInBackground("First, create a user account!");
                    }

                } catch (SocketTimeoutException e) {
                    doInBackground("Could not connect to server!");
                    Log.d("ClientRestore", e.toString());

                } catch (FileNotFoundException e) {
                    doInBackground("File not found!");
                    Log.d("ClientRestore", e.toString());

                } catch (ConnectException e) {
                    doInBackground("Could not connect with server.");
                    Log.d("ClientRestore", e.toString());

                } catch (IOException e) {
                    doInBackground("Error in restoring data!");
                    Log.d("ClientRestore", e.toString());

                } finally {
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e) {
                            Log.d("ClientRestore", e.toString());
                        }
                    }
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e) {
                            Log.d("ClientRestore", e.toString());
                        }
                    }
                    if (inputStreamReader != null) {
                        try {
                            inputStreamReader.close();
                        } catch (IOException e) {
                            Log.d("ClientRestore", e.toString());
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            Log.d("ClientRestore", e.toString());
                        }
                    }
                    if (bufferedWriter != null) {
                        try {
                            bufferedWriter.close();
                        } catch (IOException e) {
                            Log.d("ClientRestore", e.toString());
                        }
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            Log.d("ClientRestore", e.toString());
                        }
                    }
                    if (socket != null) {
                        try {
                            socket.close();
                            Log.d("ClientRestore", "SocketClosed");
                        } catch (IOException e) {
                            Log.d("ClientRestore", e.toString());
                        }
                    }
                }
            }
        });
    }

    // Toast message can only be used in UI thread. Handler specified to display Toast messages to user.
    // params[0] is message passed to be displayed by Toast.
    @Override
    protected String doInBackground(String... params) {
        final Context context = GlobalClass.getAppContext();
        final String msg = params[0];
        Handler handler;
        handler = new Handler(context.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
        });
        return null;
    }
}
