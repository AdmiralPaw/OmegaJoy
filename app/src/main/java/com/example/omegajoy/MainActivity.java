package com.example.omegajoy;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
    private View mDecorView;
    private TextView textView;
    private final Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 0) {
                int left_engine = (int) msg.arg1;
                int right_engine = (int) msg.arg2;
                textView.append("" + left_engine + "   " + right_engine + "\n");
            }
            if (msg.what == 1) {
                textView.setText("");
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
    private int last_angle = 0;
    private int last_offset = 0;

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
            }

            public void onDeviceConnectionFailed() {
                // Do something when connection failed
                Toast.makeText(getApplicationContext(), R.string.state_connection_failed, Toast.LENGTH_SHORT).show();
                btConnect = false;
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
                last_angle = 0;
                last_offset = 0;
            }

            @Override
            public void onDrag(float degrees, float offset) {
                last_angle = angleConvert(degrees);
                last_offset = distanceConvert(offset);
            }

            @Override
            public void onUp() {
                last_angle = 0;
                last_offset = 0;
            }
        });
        final Thread myThread = new Thread(
                new Runnable() {
                    public void run() {
                        while (true) {
                            int x = (int) Math.floor(last_offset * Math.cos(Math.toRadians(last_angle)));
                            int y = (int) Math.floor(last_offset * Math.sin(Math.toRadians(last_angle)));
                            int left_engine = 0;
                            int right_engine = 0;
                            int left = 1;
                            int right = 1;
                            int rotate_speed = Math.abs(x);
                            int speed = y;

                            if (x > 0) {
                                left_engine = speed + rotate_speed;
                                right_engine = speed - rotate_speed;
                            }
                            else if (x < 0) {
                                left_engine = speed - rotate_speed;
                                right_engine = speed + rotate_speed;
                            }
                            else {
                                left_engine = speed;
                                right_engine = speed;
                            }
                            if (left_engine > 100)
                                left_engine = 100;
                            if (left_engine < -100)
                                left_engine = -100;
                            if (right_engine > 100)
                                right_engine = 100;
                            if (right_engine < -100)
                                right_engine = -100;


                            if (left_engine < 0) {
                                left_engine = Math.abs(left_engine);
                                left = 0;
                            }
                            if (right_engine < 0) {
                                right_engine = Math.abs(right_engine);
                                right = 0;
                            }

                            byte[] data = new byte[3];
                            if (last_angle != 0 || last_offset != 0) {
                                data[0] = (byte) (100 & 0xFF);

                            } else {
                                data[0] = (byte) (0);
                            }
                            data[1] = (byte) ((left << 7) | (left_engine & 0xFF));
                            data[2] = (byte) ((right << 7) | (right_engine & 0xFF));

                            if (last_angle != 0 || last_offset != 0) {
                                Log.d("foo", "bar");
                            }

                            sendBluetoothData(data);
                            try {
                                //TODO вынести в настройки задержку
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        );
        myThread.start();
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
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
        }
    }

    public void sendBluetoothData(final String data) {
        // FIXME: 11/23/15 flood output T_T (понять в чем проблема)
        Log.d("LOG", data);
        bt.send(data, true);
    }

    public void sendBluetoothData(final byte[] data) {
        // FIXME: 11/23/15 flood output T_T (понять в чем проблема)
        bt.send(data, false);
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

    public void onBTClick(View view) {
        checkBluetoothState();
    }

    public void onMenuClick(View view) {
        Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivityForResult(i, RESULT_SETTING);
    }
}