package com.example.musicplayer.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class GeneralUtils {
    public static DateTimeFormatter DB_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public static String getCurrentUTCTimestamp() {
        return LocalDateTime.now(ZoneOffset.UTC).format(DB_TIMESTAMP);
    }

    public static String convertTime(int duration) {
        float d = (float) duration / (1000 * 60);
        int min = (int) d;
        float seconds = (d - min) * 60;
        int sec = (int) seconds;
        String minute = min + "", second = sec + "";
        if (min < 10) minute = "0" + minute;
        if (sec < 10) second = "0" + second;
        return minute + ":" + second;
    }
}
