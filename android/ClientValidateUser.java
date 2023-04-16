package com.development.smartlist;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

class ClientValidateUser extends AsyncTask<String, Void, String> {

    // Constructor.
    ClientValidateUser() {
    }

    void validateUser(final int userId, final String userName, final String request){

        final Context context = GlobalClass.getAppContext();
        final DBAdapter dbAdapter = new DBAdapter(context);

        // Run as a separate thread from the main UI thread otherwise a NetworkOnMainThreadException
        // exception is thrown.
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                final GlobalClass globalObject = (GlobalClass) context.getApplicationContext();

                Socket socket = null;
                OutputStream outputStream = null;
                BufferedWriter bufferedWriter = null;
                InputStream inputStream = null;
                InputStreamReader inputStreamReader = null;
                BufferedReader bufferedReader = null;

                try {
                    // Open connection to Backup server, at port 5434
                    //socket = new Socket("smartlist.internet-box.ch", 5434);
                    InetSocketAddress socketAddress = new InetSocketAddress("192.168.1.140", 5432);
                    int timeout = 5000;
                    socket = new Socket();
                    socket.connect(socketAddress, timeout);

                    // Create an output stream to provide a channel to communicate with the Server side.
                    outputStream = socket.getOutputStream();

                    // Send message to server containing the username and user_id.
                    bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                    // "\n" char indicates end of line to be read by client.
                    bufferedWriter.write(userId + "\n");
                    bufferedWriter.write(userName + "\n");
                    bufferedWriter.write(request + "\n");
                    bufferedWriter.flush();

                    // Create an input stream to receive confirmation of user info insert/update.
                    inputStream = socket.getInputStream();
                    inputStreamReader = new InputStreamReader(inputStream);
                    bufferedReader = new BufferedReader(inputStreamReader);
                    int orclResult = Integer.parseInt(bufferedReader.readLine());

                    dbAdapter.open();
                    if (orclResult > 0 && request.equals("new")) {
                        // orclResult contains returned user id from server database. If '0' was
                        // returned an error occurred validating the user info.
                        if (dbAdapter.insertUser(orclResult, userName) > 0) {
                            // Update the global username.
                            globalObject.setUserName(userName);
                            doInBackground("User info updated!");
                        }
                    }
                    else if (orclResult > 0 && request.equals("modify")) {
                        if (dbAdapter.updateUserName(userName) > 0) {
                            // Update the global username.
                            globalObject.setUserName(userName);
                            doInBackground("User info updated!");
                        }
                    }
                    else {
                        doInBackground("Problem updating user info, try changing username!");
                    }
                    dbAdapter.close();

                } catch (ConnectException e) {
                    doInBackground("Could not connect with server!");
                    Log.d("ClientValidateUser", e.toString());

                } catch (SocketTimeoutException e) {
                    doInBackground("Could not connect to server!");
                    Log.d("ClientRestore", e.toString());

                } catch (IOException e) {
                    doInBackground("Error in user setup!");
                    Log.d("ClientValidateUser", e.toString());

                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            Log.d("ClientValidateUser", e.toString());
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            Log.d("ClientValidateUser", e.toString());
                        }
                    }
                    if (inputStreamReader != null) {
                        try {
                            inputStreamReader.close();
                        } catch (IOException e) {
                            Log.d("ClientValidateUser", e.toString());
                        }
                    }
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e) {
                            Log.d("ClientValidateUser", e.toString());
                        }
                    }
                    if (bufferedWriter != null) {
                        try {
                            bufferedWriter.close();
                        } catch (IOException e) {
                            Log.d("ClientValidateUser", e.toString());
                        }
                    }
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            Log.d("ClientValidateUser", e.toString());
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
