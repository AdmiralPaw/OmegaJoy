package com.example.omegajoy;

public final class CommandUtil {
    public static int angleConvert(float degrees) {
        int angle;
        if ((int) degrees < 0) angle = (360 + (int) degrees);
        else angle = (int) degrees;
        return angle;
    }

    public static int distanceConvert(float offset) {
        return ((int) (offset * 100));
    }

    /**
     * @author AdmPaw
     * @param angel угол от 0 до 360
     * @param offset отклонение от 0 до 100
     * @return два байта данных скорости и направления движения двигателей (гусениц) робота
     * в соответствии с ПРОТОКОЛОМ
     */
    public static byte[] convertJoystickToDrive(int angel, int offset) {
        byte[] data = new byte[2];
        //TODO добавить ссылку на объяснение типа данного движения
        int x = (int) Math.floor(offset * Math.cos(Math.toRadians(angel)));
        int y = (int) Math.floor(offset * Math.sin(Math.toRadians(angel)));
        int left_engine = 0;
        int right_engine = 0;
        int left = 1;
        int right = 1;
        int rotate_speed = Math.abs(x);

        if (x > 0) {
            left_engine = y + rotate_speed;
            right_engine = y - rotate_speed;
        }
        else if (x < 0) {
            left_engine = y - rotate_speed;
            right_engine = y + rotate_speed;
        }
        else {
            left_engine = y;
            right_engine = y;
        }
        //ПРОТОКОЛ диктует скорость от -100 до 100, где впоследствии преобразуется
        // в промежуток от 0 до 100 со значением направления движения
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
        //ПРОТОКОЛ диктует ставить направление движения (вперед - 1, назад - 0)
        // в старший бит байта, остальные 7-мь бит заполнять значением скорости от 0 до 100
        data[0] = (byte) ((left << 7) | (left_engine & 0xFF));
        data[1] = (byte) ((right << 7) | (right_engine & 0xFF));
        return data;
    }
}
