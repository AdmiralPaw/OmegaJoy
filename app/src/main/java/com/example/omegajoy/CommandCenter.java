package com.example.omegajoy;

import android.util.Log;

import java.util.ArrayList;

public class CommandCenter extends Thread{
    private int defaultDelay = 50;
    private MainActivity mainActivity;
    private BluetoothService bluetoothService;
    private int last_angle = 0;
    private int last_offset = 0;
    private boolean threadToggle = true;

    CommandCenter(MainActivity activity) {
        this.mainActivity = activity;
    }

    @Override
    public void run() {
        while (threadToggle) {
            ArrayList<Byte> data0 = new ArrayList<>();
            if (last_angle != 0 || last_offset != 0) {
                data0.add(0, (byte) (100 & 0xFF));

            } else {
                data0.add(0, (byte) (0));
            }
            //ArrayList обусловлен методом add для добавления данных
            // в конец массива, ибо изначально кол-во байт данных не известно
            for (byte dataByte: CommandUtil.convertJoystickToDrive(last_angle, last_offset)) {
                data0.add(dataByte);
            }
            //Конвертация Byte[] в byte[]
            byte[] data = new byte[data0.size()];
            int i = 0;
            for (Byte dataByte: data0) {
                data[i++] = dataByte;
            }

            bluetoothService.sendBluetoothData(data);
            try {
                //TODO вынести в настройки задержку
                Thread.sleep(defaultDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setJoystickData(int angle, int offset) {
        this.last_angle = angle;
        this.last_offset = offset;
    }

    public void setBluetoothService(BluetoothService bluetoothService) {
        this.bluetoothService = bluetoothService;
    }

    public void switchLoopTo(boolean toggle) {
        this.threadToggle = toggle;
    }

    @Override
    public void start() {
        if (getState() == Thread.State.NEW) {
            try {
                super.start();
            }
            catch (IllegalThreadStateException e) {
                Log.e("[CommandCenter]", "Thread may be runnable");
            }
        }
    }
}
