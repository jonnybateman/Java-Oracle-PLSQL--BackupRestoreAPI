package com.development.smartlist;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

class ClientBackup extends AsyncTask<String, Void, String> {

    void backUpData(final String fileName) {

        final Context context = GlobalClass.getAppContext();
        final DBAdapter dbAdapter = new DBAdapter(context);

        // Run as a separate thread from the main UI thread otherwise a NetworkOnMainThreadException
        // exception is thrown.
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Socket socket = null;
                OutputStream outputStream = null;
                BufferedWriter bufferedWriter = null;
                FileInputStream fileInputStream = null;
                BufferedInputStream bufferedInputStream = null;
                InputStream inputStream = null;
                InputStreamReader inputStreamReader = null;
                BufferedReader bufferedReader = null;

                try {
                    // Open connection to Backup server, at port 5432
                    InetSocketAddress socketAddress = new InetSocketAddress("192.168.1.140", 5432);
                    int timeout = 5000;
                    socket = new Socket();
                    socket.connect(socketAddress, timeout);

                    // Create an output stream to provide a channel to communicate with the Server side.
                    outputStream = socket.getOutputStream();
                    bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

                    // Create an input stream to read data passed from the server.
                    inputStream = socket.getInputStream();
                    inputStreamReader = new InputStreamReader(inputStream);
                    bufferedReader = new BufferedReader(inputStreamReader);

                    dbAdapter.open();
                    int userId = dbAdapter.getUserId();
                    dbAdapter.close();

                    // Send message to server containing the user_id.
                    // "\n" char indicates end of line to be read by client.
                    bufferedWriter.write(userId + "\n");
                    bufferedWriter.flush();

                    // Await message from server that it has received the user id. Indicates that server
                    // is now ready to receive the backup xml file data.
                    int rtnUserId = Integer.parseInt(bufferedReader.readLine());

                    Log.d("ClientBackup","Returned User:" + rtnUserId);

                    if (userId > 0 && rtnUserId > 0) {

                        // Get the file to be transferred.
                        File file = new File(Environment.
                                getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);

                        // Define a byte array which will temporarily contain the file data.
                        byte[] byteArray = new byte[(int) file.length()];

                        // Define a FileInputStream and BufferedInputStream to read the file and store it
                        // in the byte array.
                        fileInputStream = new FileInputStream(file);
                        bufferedInputStream = new BufferedInputStream(fileInputStream);
                        int bytesRead = bufferedInputStream.read(byteArray, 0, byteArray.length);

                        Log.d("ClientBackup","BytesRead:" + bytesRead);

                        // Use the OutputStream to send the file to the Server side.
                        outputStream.write(byteArray, 0, bytesRead);

                        Log.d("ClientBackup","File sent.");

                        // Let the server know that all file data has now been sent otherwise server socket
                        // will just hang waiting for more data to be sent.
                        socket.shutdownOutput();

                        // Wait to receive confirmation that server has received the whole file and
                        // the result of processing the file.
                        int bytes = Integer.parseInt(bufferedReader.readLine());
                        String orclResult = bufferedReader.readLine();

                        // If the size of the file (bytes) that the server received matches the size of
                        // the file that was sent to server, and the file was successfully processed by
                        // the server's oracle database then the transfer was successful.
                        if (bytes == bytesRead && orclResult.equals("SUCCESS")) {
                            doInBackground("Data successfully backed up!");
                        } else {
                            doInBackground("Error in backing up data!");
                        }

                    } else {
                        doInBackground("Need to create user account first!");
                    }

                } catch (SocketTimeoutException e) {
                    doInBackground("Could not connect to server!");
                    Log.d("ClientBackup", e.toString());

                } catch (FileNotFoundException e) {
                    doInBackground("File not found");
                    Log.d("ClientBackup", e.toString());

                } catch (ConnectException e) {
                    doInBackground("Could not connect with server.");
                    Log.d("ClientBackup", e.toString());

                } catch (IOException e) {
                    doInBackground("Error in backing up data.");
                    Log.d("ClientBackup", e.toString());

                } finally {
                    // Close the stream and socket components.
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {
                            Log.d("ClientBackup", e.toString());
                        }
                    }
                    if (bufferedInputStream != null) {
                        try {
                            bufferedInputStream.close();
                        } catch (IOException e) {
                            Log.d("ClientBackup", e.toString());
                        }
                    }
                    if (bufferedWriter != null) {
                        try {
                            bufferedWriter.close();
                        } catch (IOException e) {
                            Log.d("ClientBackup", e.toString());
                        }
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            Log.d("ClientBackup", e.toString());
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            Log.d("ClientBackup", e.toString());
                        }
                    }
                    if (inputStreamReader != null) {
                        try {
                            inputStreamReader.close();
                        } catch (IOException e) {
                            Log.d("ClientBackup", e.toString());
                        }
                    }
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e) {
                            Log.d("ClientBackup", e.toString());
                        }
                    }
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            Log.d("ClientBackup", e.toString());
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
