package com.example.omegajoy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.jmedeisis.bugstick.Joystick;
import com.jmedeisis.bugstick.JoystickListener;

public class MainActivity extends AppCompatActivity {
    private View mDecorView;
    public TextView textView;
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

    private static final int RESULT_SETTING = 0;

    public ScrollView scrollView;
    SharedPreferences prefs;
    private int last_angle = 0;
    private int last_offset = 0;
    private CommandCenter commandCenter;

    private BluetoothService bluetoothService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        textView = findViewById(R.id.textView);
        scrollView = findViewById(R.id.scrollView);

        bluetoothService = new BluetoothService(this);
        commandCenter = new CommandCenter(this);

        mDecorView = getWindow().getDecorView();
        hideSystemUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bluetoothService.isBluetoothEnabled();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        commandCenter.switchLoopTo(false);
        try {
            commandCenter.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        bluetoothService.stopService();
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (bluetoothService.checkActivityResult(requestCode, resultCode, data)) {
            setup();
        }
        //TODO вариант, когда пользователь отказывается включать bluetooth
    }

    private void setup() {
        Joystick joystickLeft = findViewById(R.id.joystickLeft);
        joystickLeft.setJoystickListener(new JoystickListener() {
            @Override
            public void onDown() {
                commandCenter.setJoystickData(0,0);
            }

            @Override
            public void onDrag(float degrees, float offset) {
                commandCenter.setJoystickData(CommandUtil.angleConvert(degrees), CommandUtil.distanceConvert(offset));
            }

            @Override
            public void onUp() {
                commandCenter.setJoystickData(0,0);
            }
        });
        commandCenter.setBluetoothService(bluetoothService);
        commandCenter.start();
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
        bluetoothService.checkBluetoothState();
    }

    public void onMenuClick(View view) {
        Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivityForResult(i, RESULT_SETTING);
    }
}