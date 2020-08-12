package com.example.omegajoy;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.jmedeisis.bugstick.Joystick;
import com.jmedeisis.bugstick.JoystickListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

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
            switch (msg.what) {
                case 0:
                    textView.append(new String((byte[]) msg.obj));
                    break;
                default:
                    break;
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

    private Joystick joystickLeft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
        this.myBluetoothService = new MyBluetoothService(this);

        listViewPairedDevice = findViewById(R.id.pairedlist);

        mDecorView = getWindow().getDecorView();
        hideSystemUI();
    }

    @Override
    protected void onStart() { // Запрос на включение Bluetooth
        super.onStart();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        setup();
    }

    private void setup() { // Создание списка сопряжённых Bluetooth-устройств
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) { // Если есть сопряжённые устройства
            pairedDeviceArrayList = new ArrayList<>();
            for (BluetoothDevice device : pairedDevices) { // Добавляем сопряжённые устройства - Имя + MAC-адресс
                pairedDeviceArrayList.add(device.getName() + "\n" + device.getAddress());
            }
            pairedDeviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pairedDeviceArrayList);
            listViewPairedDevice.setAdapter(pairedDeviceAdapter);
            listViewPairedDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() { // Клик по нужному устройству
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    listViewPairedDevice.setVisibility(View.GONE); // После клика скрываем список
                    String itemValue = (String) listViewPairedDevice.getItemAtPosition(position);
                    String MAC = itemValue.substring(itemValue.length() - 17); // Вычленяем MAC-адрес
                    BluetoothDevice device2 = bluetoothAdapter.getRemoteDevice(MAC);
                    myBluetoothService.startConnect(device2);
                }
            });
        }
        joystickLeft = (Joystick) findViewById(R.id.joystickLeft);
        joystickLeft.setJoystickListener(new JoystickListener() {
            @Override
            public void onDown() {

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
                if (direction == STICK_UP) {
                    String data = "STICK_UP";
                    myBluetoothService.write(data);
                } else if (direction == STICK_UPRIGHT) {
                    String data = "STICK_UPRIGHT";
                    myBluetoothService.write(data);
                } else if (direction == STICK_RIGHT) {
                    String data = "STICK_RIGHT";
                    myBluetoothService.write(data);
                } else if (direction == STICK_DOWNRIGHT) {
                    String data = "STICK_DOWNRIGHT";
                    myBluetoothService.write(data);
                } else if (direction == STICK_DOWN) {
                    String data = "STICK_DOWN";
                    myBluetoothService.write(data);
                } else if (direction == STICK_DOWNLEFT) {
                    String data = "STICK_DOWNLEFT";
                    myBluetoothService.write(data);
                } else if (direction == STICK_LEFT) {
                    String data = "STICK_LEFT";
                    myBluetoothService.write(data);
                } else if (direction == STICK_UPLEFT) {
                    String data = "STICK_UPLEFT";
                    myBluetoothService.write(data);
                } else {
                    // no direction
                }
            }

            @Override
            public void onUp() {

            }
        });
    }



    public int get8Direction(float degrees) {
        float angle = angleConvert(degrees);

        if (angle >= 85 && angle < 95) {
            return STICK_UP;
        } else if (angle >= 40 && angle < 50) {
            return STICK_UPRIGHT;
        } else if (angle >= 355 || angle < 5) {
            return STICK_RIGHT;
        } else if (angle >= 310 && angle < 320) {
            return STICK_DOWNRIGHT;
        } else if (angle >= 265 && angle < 275) {
            return STICK_DOWN;
        } else if (angle >= 220 && angle < 230) {
            return STICK_DOWNLEFT;
        } else if (angle >= 175 && angle < 185) {
            return STICK_LEFT;
        } else if (angle >= 130 && angle < 140) {
            return STICK_UPLEFT;
        }

        return 0;
    }

    public int angleConvert(float degrees) {
        int angle = 0;
        if ((int) degrees < 0) angle = (360 + (int) degrees);
        else angle = (int) degrees;
        return angle;
    }

    public int distanceConvert(float offset) {
        int pwm = (int) (offset * 100);
        return (pwm);
    }

    public void myOnClick(View view) {
        myBluetoothService.write("12345");
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

    public Handler getHandler(){
        return this.handler;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return this.bluetoothAdapter;
    }

}