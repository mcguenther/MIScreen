package de.volzo.miscreen;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Parts of code taken from https://developer.android.com/training/connect-devices-wirelessly/wifi-direct.html
 */
public class Communication {

    private static final String TAG = Communication.class.getName();

    Context context;

    private WifiP2pManager p2pManager;
    private WifiP2pManager.Channel channel;

    private List peers = new ArrayList();
    private WifiP2pManager.PeerListListener peerListListener;
    private BroadcastReceiver broadcastReceiver;

    public Communication(Context context) {
        this.context = context;
    }

    public void initialize() {

        peerListListener = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peerList) {
                // Out with the old, in with the new.
                peers.clear();
                peers.addAll(peerList.getDeviceList());
                Log.i(TAG, "peers found (" + peers.size() + " devices)");
            }
        };

        // BROADCAST RECEIVER

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                    // Determine if Wifi P2P mode is enabled or not, alert
                    // the Activity.
                    int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                    if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                        Log.d(TAG, "p2p is enabled");
                    } else {
                        Log.e(TAG, "p2p is NOT enabled");
                    }
                } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

                    // The peer list has changed!  We should probably do something about
                    // that.
                    // Request available peers from the wifi p2p manager. This is an
                    // asynchronous call and the calling activity is notified with a
                    // callback on PeerListListener.onPeersAvailable()

                    if (p2pManager != null) {
                        p2pManager.requestPeers(channel, peerListListener);
                    }

                } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

                    // Connection state changed!  We should probably do something about
                    // that.

                } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                    Log.i(TAG, ((WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)).toString());
                }
            }
        };

        context.registerReceiver(broadcastReceiver, intentFilter);

        // WIFI

        p2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = p2pManager.initialize(context, context.getMainLooper(), null);

    }

    public void discover() {
        p2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // Code for when the discovery initiation is successful goes here.
                // No services have actually been discovered yet, so this method
                // can often be left blank.  Code for peer discovery goes in the
                // onReceive method, detailed below.
                Log.i(TAG, "peer discovery successfully initialized");
            }

            @Override
            public void onFailure(int reasonCode) {
                // Code for when the discovery initiation fails goes here.
                // Alert the user that something went wrong.
                Log.i(TAG, "peer discovery failed");
            }
        });
    }

    public void kill() {
        context.unregisterReceiver(broadcastReceiver);

        // TODO probably a lot more stuff needs to be teared down/closed/unregistered
    }

}
