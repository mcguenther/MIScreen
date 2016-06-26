package de.volzo.miscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    private Spinner spRole;
    private Button btOk;

    private Communication comm;
    private ArrayAdapter<WifiP2pDevice> adapter;

    // APP LIFECYCLE METHODS
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spRole = (Spinner) findViewById(R.id.spRole);
        btOk = (Button) findViewById(R.id.btOk);

        spRole.setOnItemSelectedListener(this);
        btOk.setOnClickListener(this);

        registerUpdateReceiver();

        adapter = new P2pDeviceAdapter(this, R.layout.device_listview_entry, new ArrayList<WifiP2pDevice>());
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

        comm = new Communication(this);
        comm.initialize();

        // Wait 10sec for every device to init before network discovery is started
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                comm.discoverPeers();
            }
        }, 10000);
    }

    @Override
    protected void onPause() {
        // TODO: kill NSD
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshView();
        // TODO: reinit NSD
    }

    @Override
    protected void onDestroy() {
        // TODO: kill NSD
        super.onDestroy();
    }

    // USER INTERFACE

    // as taken from https://developer.android.com/guide/topics/ui/controls/spinner.html
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        saveRole(pos);
    }

    private void saveRole(int pos) {
        SharedPreferences sharedPref = getSharedPreferences("minPrefs",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("role", pos);
        editor.commit();
    }

    public void onNothingSelected(AdapterView<?> parent) {
        resetView();
    }

    private void resetView() {
        saveRole(0);
        this.spRole.setSelection(0);
    }

    private void refreshView() {
        int role = getRole();
        this.spRole.setSelection(role);
    }

    private int getRole() {
        SharedPreferences sharedPref = getSharedPreferences("minPrefs", MODE_PRIVATE);
        return sharedPref.getInt("role", 0);
    }

    @Override
    public void onClick(View v) {
        int role = getRole();
        Intent intent = new Intent(this, Positioner.class);
        intent.putExtra("role", role);
        startActivity(intent);
    }

    private void updateView() {
        if (adapter != null && comm != null) {
            adapter.clear();
            for (WifiP2pDevice device : comm.connectedPeerList) {
                adapter.add(device);
            }
        }
    }

    // BroadcastReceiver for Update Events

    public void registerUpdateReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        //LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
        this.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Log.v(TAG, "broadcast received " + intent.toString());
                updateView();
            }
        }, intentFilter);
    }

    // ListView Adapter

    private class P2pDeviceAdapter extends ArrayAdapter<WifiP2pDevice> {
        public P2pDeviceAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        public P2pDeviceAdapter(Context context, int resource, List<WifiP2pDevice> items) {
            super(context, resource, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = convertView;

            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.device_listview_entry, null);
            }

            WifiP2pDevice e = getItem(position);

            if (e != null) {
                TextView tt1 = (TextView) v.findViewById(R.id.address);
                TextView tt2 = (TextView) v.findViewById(R.id.name);

                if (tt1 != null) {
                    tt1.setText("address: " + e.deviceAddress);
                }

                if (tt2 != null) {
                    tt2.setText("name: " + e.deviceName);
                }

            }

            return v;
        }
    }
}
