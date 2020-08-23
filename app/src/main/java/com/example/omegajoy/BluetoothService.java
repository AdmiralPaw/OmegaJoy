package com.example.omegajoy;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;

public class BluetoothService {
    private BluetoothSPP bluetoothSPP;
    private Boolean btConnect = false;
    private MainActivity mainActivity;

    BluetoothService(MainActivity activity) {
        this.mainActivity = activity;
        bluetoothSPP = new BluetoothSPP(activity.getApplicationContext());
        checkBluetoothState();
        bluetoothSPP.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                // Do something when successfully connected
                Toast.makeText(mainActivity.getApplicationContext(), R.string.state_connected, Toast.LENGTH_SHORT).show();
                btConnect = true;
//                // change setting menu
//                MenuItem settingsItem = menu.findItem(R.id.mnuBluetooth);
//                settingsItem.setTitle(R.string.mnu_disconnect);
            }

            public void onDeviceDisconnected() {
                // Do something when connection was disconnected
                Toast.makeText(mainActivity.getApplicationContext(), R.string.state_disconnected, Toast.LENGTH_SHORT).show();
                btConnect = false;
                btConnect = true;
            }

            public void onDeviceConnectionFailed() {
                // Do something when connection failed
                Toast.makeText(mainActivity.getApplicationContext(), R.string.state_connection_failed, Toast.LENGTH_SHORT).show();
                btConnect = false;
            }
        });

        bluetoothSPP.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                mainActivity.textView.append(message + "\n");
                mainActivity.scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    public void checkBluetoothState() {
        if (bluetoothSPP.isBluetoothEnabled()) {
            if (this.btConnect) {
                bluetoothSPP.disconnect();
            }
            bluetoothSPP.setupService();
            bluetoothSPP.startService(BluetoothState.DEVICE_OTHER);
            // load device list
            Intent intent = new Intent(mainActivity, MyDeviceList.class);
            intent.putExtra("bluetooth_devices", "Bluetooth devices");
            intent.putExtra("no_devices_found", "No device");
            intent.putExtra("scanning", "Scanning");
            intent.putExtra("scan_for_devices", "Search");
            intent.putExtra("landscape", "true");
            intent.putExtra("select_device", "Select");
            intent.putExtra("layout_list", R.layout.bluetooth_divices);

            mainActivity.startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
        }
    }

    public void isBluetoothEnabled() {
        // setup bluetooth
        if (!bluetoothSPP.isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mainActivity.startActivityForResult(enableBtIntent, BluetoothState.REQUEST_ENABLE_BT);
        }
    }

    public boolean checkActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bluetoothSPP.connect(data);
            return true;
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bluetoothSPP.setupService();
                bluetoothSPP.startService(BluetoothState.DEVICE_OTHER);
                return true;
            }
        }
        return false;
    }

    public void sendBluetoothData(final String data) {
        // FIXME: 11/23/15 flood output T_T (понять в чем проблема)
        Log.d("LOG", data);
        bluetoothSPP.send(data, true);
    }

    public void sendBluetoothData(final byte[] data) {
        // FIXME: 11/23/15 flood output T_T (понять в чем проблема)
        bluetoothSPP.send(data, false);
    }

    public void stopService() {
        bluetoothSPP.stopService();
    }
}
