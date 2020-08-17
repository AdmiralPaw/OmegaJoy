package com.example.omegajoy;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.jmedeisis.bugstick.Joystick;
import com.jmedeisis.bugstick.JoystickListener;

import java.util.ArrayList;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = "[Bluetooth]";
    private static final String NAME = "omegaBot";
    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<String> pairedDeviceArrayList;
    private ArrayAdapter<String> pairedDeviceAdapter;

    private ListView listViewPairedDevice;
    private View mDecorView;
    private MyBluetoothService myBluetoothService;
    private TextView textView;
    private final Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 0) {
                textView.append(new String((byte[]) msg.obj));
            }
        }
    };

    private static final int STICK_NONE = 0;
    private static final int STICK_UP = 1;
    private static final int STICK_UPRIGHT = 2;
    private static final int STICK_RIGHT = 3;
    private static final int STICK_DOWNRIGHT = 4;
    private static final int STICK_DOWN = 5;
    private static final int STICK_DOWNLEFT = 6;
    private static final int STICK_LEFT = 7;
    private static final int STICK_UPLEFT = 8;
    private static final int RESULT_SETTING = 0;

    private Joystick joystickLeft;

    private ScrollView scrollView;
    Context context;
    BluetoothSPP bt;
    Boolean btConnect = false;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        textView = findViewById(R.id.textView);
        scrollView = findViewById(R.id.scrollView);
        bt = new BluetoothSPP(context);
        checkBluetoothState();

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                // Do something when successfully connected
                Toast.makeText(getApplicationContext(), R.string.state_connected, Toast.LENGTH_SHORT).show();
                btConnect = true;
//                // change setting menu
//                MenuItem settingsItem = menu.findItem(R.id.mnuBluetooth);
//                settingsItem.setTitle(R.string.mnu_disconnect);
            }

            public void onDeviceDisconnected() {
                // Do something when connection was disconnected
                Toast.makeText(getApplicationContext(), R.string.state_disconnected, Toast.LENGTH_SHORT).show();
                btConnect = false;
                btConnect = true;
//                // change setting menu
//                MenuItem settingsItem = menu.findItem(R.id.mnuBluetooth);
//                settingsItem.setTitle(R.string.mnu_connect);
            }

            public void onDeviceConnectionFailed() {
                // Do something when connection failed
                Toast.makeText(getApplicationContext(), R.string.state_connection_failed, Toast.LENGTH_SHORT).show();
                btConnect = false;
//                // change setting menu
//                MenuItem settingsItem = menu.findItem(R.id.mnuBluetooth);
//                settingsItem.setTitle(R.string.mnu_connect);
            }
        });

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                textView.append(message + "\n");
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });

        mDecorView = getWindow().getDecorView();
        hideSystemUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // setup bluetooth
        if (!bt.isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BluetoothState.REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bt.stopService();
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bt.connect(data);
            setup();
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
                setup();
            } else {
                // Do something if user doesn't choose any device (Pressed back)
            }
        }
    }

    private void setup() {
        joystickLeft = findViewById(R.id.joystickLeft);
        joystickLeft.setJoystickListener(new JoystickListener() {
            @Override
            public void onDown() {
                sendBluetoothData("STICK_DOWNED");
            }

            @Override
            public void onDrag(float degrees, float offset) {
                // check position
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                int direction = get8Direction(degrees);
                int distance = distanceConvert(offset);
                if (distance >= 75) {
                    String data;
                    if (direction == STICK_UP) {
                        data = prefs.getString("pref_stick_up", "");
                        sendBluetoothData(data + " " + distanceConvert(offset));
                    } else if (direction == STICK_UPRIGHT) {
                        data = prefs.getString("pref_stick_upright", "");
                        sendBluetoothData(data + " " + distanceConvert(offset));
                    } else if (direction == STICK_RIGHT) {
                        data = prefs.getString("pref_stick_right", "");
                        sendBluetoothData(data + " " + distanceConvert(offset));
                    } else if (direction == STICK_DOWNRIGHT) {
                        data = prefs.getString("pref_stick_downright", "");
                        sendBluetoothData(data + " " + distanceConvert(offset));
                    } else if (direction == STICK_DOWN) {
                        data = prefs.getString("pref_stick_down", "");
                        sendBluetoothData(data + " " + distanceConvert(offset));
                    } else if (direction == STICK_DOWNLEFT) {
                        data = prefs.getString("pref_stick_downleft", "");
                        sendBluetoothData(data + " " + distanceConvert(offset));
                    } else if (direction == STICK_LEFT) {
                        data = prefs.getString("pref_stick_left", "");
                        sendBluetoothData(data + " " + distanceConvert(offset));
                    } else if (direction == STICK_UPLEFT) {
                        data = prefs.getString("pref_stick_upleft", "");
                        sendBluetoothData(data + " " + distanceConvert(offset));
                    } else {
                        data = "0";
                    }
                    Log.d("LOG_Pre", data);
                }
            }

            @Override
            public void onUp() {
                sendBluetoothData("STICK_UPPED");
            }
        });
    }



    public int get8Direction(float degrees) {
        float angle = angleConvert(degrees);

        if (angle >= 45 && angle < 135) {
            return STICK_UP;
//        } else if (angle >= 40 && angle < 50) {
//            return STICK_UPRIGHT;
        } else if (angle >= 315 || angle < 45) {
            return STICK_RIGHT;
//        } else if (angle >= 310 && angle < 320) {
//            return STICK_DOWNRIGHT;
        } else if (angle >= 225 && angle < 315) {
            return STICK_DOWN;
//        } else if (angle >= 220 && angle < 230) {
//            return STICK_DOWNLEFT;
        } else if (angle >= 135 && angle < 225) {
            return STICK_LEFT;
//        } else if (angle >= 130 && angle < 140) {
//            return STICK_UPLEFT;
        }

        return 0;
    }

    public int angleConvert(float degrees) {
        int angle;
        if ((int) degrees < 0) angle = (360 + (int) degrees);
        else angle = (int) degrees;
        return angle;
    }

    public int distanceConvert(float offset) {
        return ((int) (offset * 100));
    }


    private void checkBluetoothState() {
        if (bt.isBluetoothEnabled()) {
            if (this.btConnect) {
                bt.disconnect();
            }
            bt.setupService();
            //bt.startService(BluetoothState.DEVICE_OTHER);
            bt.startService(BluetoothState.DEVICE_OTHER);
            // load device list
            Intent intent = new Intent(MainActivity.this, MyDeviceList.class);
            intent.putExtra("bluetooth_devices", "Bluetooth devices");
            intent.putExtra("no_devices_found", "No device");
            intent.putExtra("scanning", "Scanning");
            intent.putExtra("scan_for_devices", "Search");
            intent.putExtra("landscape", "true");
            intent.putExtra("select_device", "Select");
            intent.putExtra("layout_list", R.layout.bluetooth_divices);
//            intent.putExtra("layout_text", R.layout.device_layout_text);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
        }
    }

    public void sendBluetoothData(final String data) {
        // FIXME: 11/23/15 flood output T_T (понять в чем проблема)

        final Handler handler = new Handler();

        final Runnable r = new Runnable() {
            public void run() {
                Log.d("LOG", data);
                bt.send(data, true);
            }
        };
        handler.postDelayed(r, 200);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus)
            hideSystemUI();
    }

    // This snippet hides the system bars.
    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mDecorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE
            );
        }
    }

    public Handler getHandler() {
        return this.handler;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return this.bluetoothAdapter;
    }

    public void onBTClick(View view) {
        checkBluetoothState();
    }

    public void onMenuClick(View view) {
        Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivityForResult(i, RESULT_SETTING);
    }
}