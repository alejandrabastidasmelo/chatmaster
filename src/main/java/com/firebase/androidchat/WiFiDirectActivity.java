/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.firebase.androidchat;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.androidchat.DeviceListFragment.DeviceActionListener;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 * An activity that uses WiFi Direct APIs to discover and connect with available
 * devices. WiFi Direct APIs are asynchronous and rely on callback mechanism
 * using interfaces to notify the application of operation success or failure.
 * The application should also register a BroadcastReceiver for notification of
 * WiFi state related events.
 */
public class WiFiDirectActivity extends Activity implements ChannelListener, DeviceActionListener {

    public static final String TAG = "Yapanay";
    private WifiP2pManager manager;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;

    private final IntentFilter intentFilter = new IntentFilter();
    private Channel channel;
    private BroadcastReceiver receiver = null;

    //estos son para las notifics
    // TODO: change this to your own Firebase URL
    private static final String FIREBASE_URL = "https://blinding-fire-5986.firebaseio.com";
    public static String estado = new String();

    private static AudioManager myAudioManager;
    MediaPlayer mediaPlayer;

    private String mUsername;
    private Firebase mFirebaseRef;
    private ValueEventListener mConnectedListener;
    private ValueEventListener chatlistener;
    private ChatListAdapter mChatListAdapter;

    static AlertDialog.Builder alert = null;
    static AlertDialog alertDialog;
    static Vibrator vibration;



    static AlertDialog.Builder builder = null;


    //aqui incluyo la parte del gps
    private TextView longitude1;
    private TextView latitude1;
    private LocationManager locationManager;
    private String provider;
    private LocationListener escucha;
    private Button compartir;
    private WiFiDirectActivity myactivity;
    public static float lat = 0;
    public static float lng = 0;
    private File archivo = new File("datos.txt");
    public static String direccion = null;



    /**
     * @param isWifiP2pEnabled the isWifiP2pEnabled to set
     */
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);



        //para que se conecte a la base de datos
        mFirebaseRef = new Firebase(FIREBASE_URL).child("chat");

        //para que aparezca la alerta
        alert = new AlertDialog.Builder(WiFiDirectActivity.this);
        alertDialog = alert.create();
        myAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        vibration = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);



        // add necessary intent values to be matched.

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);



    }//final oncreate



    @Override
    public void onStart() {
        super.onStart();


final ListView lista = new ListView(this);
        //aqui en el on star tambien funciona aunque puede ir lo de las coordenadas en oncreate
        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        escucha = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
               // System.out.println("si cambio");
                lat = (float) (location.getLatitude());
                lng = (float) (location.getLongitude());

                //cada que la posicion cambie se guarda en un archivo los datos

                try {

                    OutputStreamWriter fout=
                            new OutputStreamWriter(
                                    openFileOutput(archivo.getName(), Context.MODE_WORLD_READABLE));

                    fout.write(lng+"\n"+lat);
                    direccion = archivo.getAbsolutePath();
                    fout.close();
                   // System.out.println("pudo guardar el archivo en el" + archivo.getName() + archivo.getAbsolutePath());

                }catch (Exception ex)
                {
                    Log.e("Ficheros", "Error al escribir fichero a memoria interna");
                }



                // longitude1.setText(String.valueOf(lat));
                //latitude1.setText(String.valueOf(lng));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
                //Toast.makeText(R.id.relativeLayout,"Enable new provider" + provider, (Toast.LENGTH_SHORT)).show();
            }

            @Override
            public void onProviderDisabled(String provider) {

            }


        };


        // Define the criteria how to select the locatioin provider -> use
        // default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);

        Location location;


        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);


        // Initialize the location fields
        if (location != null) {
            System.out.println("Provider " + provider + " has been selected.");
            escucha.onLocationChanged(location);
        } else {
            Toast.makeText(WiFiDirectActivity.this, "location is not available", (Toast.LENGTH_SHORT)).show();

            //longitude1.setText("Location not available");
            //latitude1.setText("Location not available");
        }

        //aqui finaliza la parte del gps

        mChatListAdapter = new ChatListAdapter(mFirebaseRef.limit(50), this, R.layout.chat_message, mUsername);
        lista.setAdapter(mChatListAdapter);
        mChatListAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                //listView.setSelection(mChatListAdapter.getCount() - 1);
            }
        });


        // Finally, a little indication of connection status
        mConnectedListener = mFirebaseRef.getRoot().child(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = (Boolean) dataSnapshot.getValue();
                if (connected) {
                    Toast.makeText(WiFiDirectActivity.this, "Connected to Firebase", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(WiFiDirectActivity.this, "Disconnected from Firebase", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                // No-op
            }
        });




        //aqui se obtienen los mensajes del servidor
         mFirebaseRef.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(DataSnapshot dataSnapshot) {
                 String recorre = new String();
                 for (DataSnapshot object : dataSnapshot.getChildren())
                 {
                     Chat chat = object.getValue(Chat.class);
                     estado = chat.getText();
                     mostrarAlerta();
                 }

                 //Log.e("sale" , "debe salir" + estado);
                // Toast.makeText(WiFiDirectActivity.this, recorre, Toast.LENGTH_SHORT).show();
             }

             @Override
             public void onCancelled(FirebaseError firebaseError) {

             }
         });

    }

    @Override
    public void onStop() {
        super.onStop();
       // mFirebaseRef.getRoot().child(".info/connected").removeEventListener(mConnectedListener);
//        mChatListAdapter.cleanup();
    }

    /** register the BroadcastReceiver with the intent values to be matched */
    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);


        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, escucha);
        //System.out.println("si esta escuchando");
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);


        locationManager.removeUpdates(escucha);

        //System.out.println("Entro al on pause");
    }

    /**
     * Remove all peers and clear all fields. This is called on
     * BroadcastReceiver receiving a state change event.
     */
    public void resetData() {
        DeviceListFragment fragmentList = (DeviceListFragment) getFragmentManager()
                .findFragmentById(R.id.frag_list);
        DeviceDetailFragment fragmentDetails = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        if (fragmentList != null) {
            fragmentList.clearPeers();
        }
        if (fragmentDetails != null) {
            fragmentDetails.resetViews();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_items, menu);
        return true;
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.atn_direct_enable:
                if (manager != null && channel != null) {

                    // Since this is the system wireless settings activity, it's
                    // not going to send us a result. We will be notified by
                    // WiFiDeviceBroadcastReceiver instead.

                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                } else {
                    Log.e(TAG, "channel or manager is null");
                }
                return true;

            case R.id.atn_direct_discover:
                if (!isWifiP2pEnabled) {
                    Toast.makeText(WiFiDirectActivity.this, R.string.p2p_off_warning,
                            Toast.LENGTH_SHORT).show();
                    return true;
                }
                final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                        .findFragmentById(R.id.frag_list);
                fragment.onInitiateDiscovery();
                manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Toast.makeText(WiFiDirectActivity.this, "Discovery Initiated",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(WiFiDirectActivity.this, "Discovery Failed : " + reasonCode,
                                Toast.LENGTH_SHORT).show();
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void showDetails(WifiP2pDevice device) {
        DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        fragment.showDetails(device);

    }

    @Override
    public void connect(WifiP2pConfig config) {
        manager.connect(channel, config, new ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(WiFiDirectActivity.this, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void disconnect() {
        final DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        fragment.resetViews();
        manager.removeGroup(channel, new ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
            }

            @Override
            public void onSuccess() {
                fragment.getView().setVisibility(View.GONE);
            }

        });
    }

    @Override
    public void onChannelDisconnected() {
        // we will try once more
        if (manager != null && !retryChannel) {
            Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
            resetData();
            retryChannel = true;
            manager.initialize(this, getMainLooper(), this);
        } else {
            Toast.makeText(this,
                    "Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void cancelDisconnect() {

        /*
         * A cancel abort request by user. Disconnect i.e. removeGroup if
         * already connected. Else, request WifiP2pManager to abort the ongoing
         * request
         */
        if (manager != null) {
            final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                    .findFragmentById(R.id.frag_list);
            if (fragment.getDevice() == null
                    || fragment.getDevice().status == WifiP2pDevice.CONNECTED) {
                disconnect();
            } else if (fragment.getDevice().status == WifiP2pDevice.AVAILABLE
                    || fragment.getDevice().status == WifiP2pDevice.INVITED) {

                manager.cancelConnect(channel, new ActionListener() {

                    @Override
                    public void onSuccess() {
                        Toast.makeText(WiFiDirectActivity.this, "Aborting connection",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(WiFiDirectActivity.this,
                                "Connect abort request failed. Reason Code: " + reasonCode,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

    }




    public void mostrarAlerta()
    {

        //  AlertDialog alertDialog = alert.create();
        // alertDialog = alert.create();
        alert.setTitle("NOTIFICACION");


        if (estado.equals("rojo"))
        {
            alert.setMessage("ALERTA ROJA: EL VOLCAN SE ENCUENTRA EN ALERTA ROJA DIRIJASE AL REFUGIO MAS CERCANO");
            alert.setPositiveButton("Cerrar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {
                    vibration.cancel();
                }
            });
            alert.show();



            myAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            int usuario = myAudioManager.getStreamVolume(AudioManager.STREAM_ALARM); //recuerda cual era antes de cambiarlo

            mediaPlayer = new MediaPlayer();

            Uri alarmsound = null;
            Uri ringtoneuri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


            try {
                if (mediaPlayer != null){
                    mediaPlayer.start();
                    if (!mediaPlayer.isPlaying()) {
                        mediaPlayer.setDataSource(ringtoneuri.toString());
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                        mediaPlayer.setLooping(true);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    }
                }mediaPlayer.stop();

            } catch (IOException e) {
                Toast.makeText(this, "Your alarm sound was unavailable.", Toast.LENGTH_LONG).show();

            }

            myAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, myAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), AudioManager.FLAG_PLAY_SOUND);
            vibration.vibrate(300000);

        }

        if (estado.equals("naranja"))
        {
            alert.setMessage("ALERTA NARANJA: Se le recomienda dirigirse al refugio mas cercano");
            alert.setPositiveButton("Cerrar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {
                    vibration.cancel();
                }
            });
            alert.show();



            myAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            int usuario = myAudioManager.getStreamVolume(AudioManager.STREAM_ALARM); //recuerda cual era antes de cambiarlo

            mediaPlayer = new MediaPlayer();

            Uri alarmsound = null;
            Uri ringtoneuri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


            try {
                if (mediaPlayer != null){
                    mediaPlayer.start();
                    if (!mediaPlayer.isPlaying()) {
                        mediaPlayer.setDataSource(ringtoneuri.toString());
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                        mediaPlayer.setLooping(true);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    }
                }mediaPlayer.stop();

            } catch (IOException e) {
                Toast.makeText(this, "Your alarm sound was unavailable.", Toast.LENGTH_LONG).show();

            }

            myAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, myAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), AudioManager.FLAG_PLAY_SOUND);
            vibration.vibrate(30);

        }

        if (estado.equals("amarillo"))
        {
            alert.setMessage("ALERTA AMARILLA: Se recomienda tomar precauciones");
            alert.setPositiveButton("Cerrar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {
                    vibration.cancel();
                }
            });
            alert.show();



            myAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            int usuario = myAudioManager.getStreamVolume(AudioManager.STREAM_ALARM); //recuerda cual era antes de cambiarlo

            mediaPlayer = new MediaPlayer();

            Uri alarmsound = null;
            Uri ringtoneuri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


            try {
                if (mediaPlayer != null){
                    mediaPlayer.start();
                    if (!mediaPlayer.isPlaying()) {
                        mediaPlayer.setDataSource(ringtoneuri.toString());
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                        mediaPlayer.setLooping(true);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    }
                }mediaPlayer.stop();

            } catch (IOException e) {
                Toast.makeText(this, "Your alarm sound was unavailable.", Toast.LENGTH_LONG).show();

            }

            myAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, myAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), AudioManager.FLAG_PLAY_SOUND);
            vibration.vibrate(300000);
        }

        if (estado.equals("verde"))
        {


        }





    }
}
