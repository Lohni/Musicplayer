package com.lohni.musicplayer.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class GeneralUtils {
    public static DateTimeFormatter DB_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public static String getCurrentUTCTimestamp() {
        return LocalDateTime.now(ZoneOffset.UTC).format(DB_TIMESTAMP);
    }

    public static String getTimestampWeekBefor() {
        return LocalDateTime.now(ZoneOffset.UTC).minusWeeks(1L).format(DB_TIMESTAMP);
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

    public static String convertTimeWithUnit(int duration) {
        long seconds = duration / 1000;
        LocalDateTime lt = LocalDateTime.ofEpochSecond(seconds, 0, ZoneOffset.UTC);

        int day = lt.getDayOfYear() - 1;
        int hour = lt.getHour();
        int minute = lt.getMinute();
        int second = lt.getSecond();

        String dayString = (day > 0) ? day + "d" : "";
        String hourString = (hour > 0) ? hour + "h" : "";
        String minuteString = (minute > 0) ? minute + "m" : "";
        String secondString = (second > 0 || (hourString.isEmpty() && minuteString.isEmpty())) ? second + "s" : "";

        return dayString + hourString + minuteString + secondString;
    }

    public static String getTimeDiffAsText(LocalDateTime dbTime){
        LocalDateTime ldtNow = LocalDateTime.now(ZoneOffset.UTC);

        long dayDiff = ChronoUnit.DAYS.between(dbTime, ldtNow);
        if (dayDiff < 1) {
            long hours = ChronoUnit.HOURS.between(dbTime, ldtNow);
            if (hours < 1) {
                long minutes = ChronoUnit.MINUTES.between(dbTime, ldtNow);
                if (minutes < 1) {
                    return "<1m ago";
                }
                return minutes + "m ago";
            }
            return hours + "h ago";
        }
        return dayDiff + " days ago";
    }
}