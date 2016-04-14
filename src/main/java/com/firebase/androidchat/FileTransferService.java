// Copyright 2011 Google Inc. All Rights Reserved.

package com.firebase.androidchat;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */
public class FileTransferService extends IntentService {

    private static final int SOCKET_TIMEOUT = 5000;
    public static final String ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE";
    public static final String EXTRAS_FILE_PATH = "file_url";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";

    public FileTransferService(String name) {
        super(name);
    }

    public FileTransferService() {
        super("FileTransferService");
    }

    /*
     * (non-Javadoc)
     * @see android.app.IntentService#onHandleIntent(android.content.Intent)
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        Context context = getApplicationContext();
        if (intent.getAction().equals(ACTION_SEND_FILE)) {
            String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
            Socket socket = new Socket();
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);

            try {
                Log.d(WiFiDirectActivity.TAG, "Opening client socket - ");
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                Log.d(WiFiDirectActivity.TAG, "Client socket - " + socket.isConnected());
                OutputStream stream = socket.getOutputStream();
                ContentResolver cr = context.getContentResolver();
                InputStream is = null;
                String todo = "";

                try {
                    is = openFileInput("datos.txt");
                    InputStreamReader arch = new InputStreamReader(
                            is);
                    BufferedReader br = new BufferedReader(arch);
                    String linea = br.readLine();

                    while (linea != null) {
                        todo = todo + linea + "\n";
                        linea = br.readLine();
                    }

                    Log.d(WiFiDirectActivity.TAG, todo + "esto es lo que hay en el archivo");

                } catch (IOException e) {
                    Log.d(WiFiDirectActivity.TAG, "de verdad no leyo");
                }

              /*  try {
                    Log.d(WiFiDirectActivity.TAG, Uri.parse(getApplicationContext().getFilesDir().getAbsolutePath() + WiFiDirectActivity.direccion).toString());
                   // is = cr.openInputStream(Uri.parse("file:/"+getApplicationContext().getFilesDir()+WiFiDirectActivity.direccion));



                } catch (FileNotFoundException e) {
                    Log.d(WiFiDirectActivity.TAG, e.toString() + "el error sale aqui");
                }*/
                if (is != null){
               DeviceDetailFragment.copyFile(is, stream);
                Log.d(WiFiDirectActivity.TAG, "Client: Data written" + is);}
                else {
                    Log.e(WiFiDirectActivity.TAG, "esta en nulo");
                }

            } catch (IOException e) {
                Log.e(WiFiDirectActivity.TAG, e.getMessage());
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
    }
}
