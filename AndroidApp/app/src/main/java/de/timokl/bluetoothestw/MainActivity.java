package de.timokl.bluetoothestw;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static android.bluetooth.BluetoothDevice.*;

public class MainActivity extends Activity {

    private ArrayAdapter<String> listAdapter;
    private Map<String, ScanResult> scanResults;
    private String mac_adresse = "";
    private static final String LOG_TAG = "EstwApp";
    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static final int REQUEST_ENABLE_BT = 123;
    private static final int REQUEST_ACCESS_COARSE_LOCATION
            = 321;

    private BluetoothAdapter adapter;
    private boolean started;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_FOUND.equals(action)) {
                BluetoothDevice device =
                        intent.getParcelableExtra(EXTRA_DEVICE);
                listAdapter.add(getString(R.string.template,
                        device.getName(),
                        device.getAddress()));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView lv = findViewById(R.id.lv);
        IntentFilter filter = new IntentFilter(ACTION_FOUND);
        registerReceiver(receiver, filter);
        listAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1);
        scanResults = new HashMap<>();

        lv.setAdapter(listAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected item text from ListView
                String selectedItem = (String) parent.getItemAtPosition(position);

                // Display the selected item text on TextView
                mac_adresse = selectedItem.substring(selectedItem.length() - 19, selectedItem.length() - 2);
                if (!globaleVariablen.getIs_connected()) {
                    verbinden();
                } else {
                    Toast.makeText(getApplicationContext(),"Schon verbunden!",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onStart() {
        super.onStart();
        listAdapter.clear();
        scanResults.clear();
        adapter = null;
        started = false;


        if (ContextCompat.checkSelfPermission (this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {

            if (!ActivityCompat.shouldShowRequestPermissionRationale (this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        ActivityCompat.requestPermissions (this,
                                new String [] {Manifest.permission.READ_CONTACTS}, REQUEST_ACCESS_COARSE_LOCATION);
                    }

        } else {
            if (isBluetoothEnabled()) {
                showDevices();
            }
        }

        /*if (checkSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]
                            {Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_ACCESS_COARSE_LOCATION);
        } else {
            if (isBluetoothEnabled()) {
                showDevices();
            }
        }*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (started) {
            adapter.cancelDiscovery();
            started = false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if ((requestCode == REQUEST_ACCESS_COARSE_LOCATION) &&
                (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            if (isBluetoothEnabled()) {
                showDevices();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((resultCode == RESULT_OK) && (requestCode == REQUEST_ENABLE_BT)) {
            showDevices();
        }
    }

    private boolean isBluetoothEnabled() {
        boolean enabled = false;
        adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            enabled = adapter.isEnabled();
            if (!enabled) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        return enabled;
    }

    private void showDevices() {
        StringBuilder sb = new StringBuilder();
        Log.d(LOG_TAG, "show devices");
        if (started) {
            adapter.cancelDiscovery();
        }
        started = adapter.startDiscovery();
        if (started) {
            sb.append(getString(R.string.others));
        }
        Log.d(LOG_TAG, "STRING Builder" + String.valueOf(sb));
    }

    public void verbinden() {
        Log.d(LOG_TAG, "Verbinde mit " + mac_adresse);

        BluetoothDevice remote_device = adapter.getRemoteDevice(mac_adresse);

        // Socket erstellen
        try {
            globaleVariablen.setSocket(remote_device.createInsecureRfcommSocketToServiceRecord(uuid));
            Log.d(LOG_TAG, "Socket erstellt");
        } catch (Exception e) {
            Log.e(LOG_TAG, "Socket Erstellung fehlgeschlagen: " + e.toString());
        }

        adapter.cancelDiscovery();

        // Socket verbinden
        try {
            globaleVariablen.getSocket().connect();
            Log.d(LOG_TAG, "Socket verbunden");
            globaleVariablen.setIs_connected(true);
        } catch (IOException e) {
            globaleVariablen.setIs_connected(false);
            Log.e(LOG_TAG, "Socket kann nicht verbinden: " + e.toString());
        }

        // Socket beenden, falls nicht verbunden werden konnte
        if (!globaleVariablen.getIs_connected()) {
            try {
                globaleVariablen.getSocket().close();
            } catch (Exception e) {
                Log.e(LOG_TAG,
                        "Socket kann nicht beendet werden: " + e.toString());
            }
        }

        // Outputstream erstellen:
        try {
            globaleVariablen.setStream_out(globaleVariablen.getSocket().getOutputStream());
            Log.d(LOG_TAG, "OutputStream erstellt");
        } catch (IOException e) {
            Log.e(LOG_TAG, "OutputStream Fehler: " + e.toString());
            globaleVariablen.setIs_connected(false);
        }

        // Inputstream erstellen
        try {
            globaleVariablen.setStream_in(globaleVariablen.getSocket().getInputStream());
            Log.d(LOG_TAG, "InputStream erstellt");
        } catch (IOException e) {
            Log.e(LOG_TAG, "InputStream Fehler: " + e.toString());
            globaleVariablen.setIs_connected(false);
        }

        if (globaleVariablen.getIs_connected()) {
            Toast.makeText(this, "Verbunden mit " + mac_adresse,
                    Toast.LENGTH_LONG).show();

            Intent intent = new Intent(this, EstwActivity.class);
            startActivity(intent);

            finish();
        } else {
            Toast.makeText(this, "Verbindungsfehler mit " + mac_adresse,
                    Toast.LENGTH_LONG).show();
        }
    }
}
