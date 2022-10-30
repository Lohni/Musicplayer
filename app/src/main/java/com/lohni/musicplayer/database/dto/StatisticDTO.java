package com.lohni.musicplayer.database.dto;

import androidx.room.ColumnInfo;

public class StatisticDTO {

    @ColumnInfo(name = "time")
    private String timestamp;

    @ColumnInfo(name = "total_time")
    private Long time_played;

    @ColumnInfo(name = "amount")
    private Integer times_played;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Long getTime_played() {
        return time_played;
    }

    public void setTime_played(Long time_played) {
        this.time_played = time_played;
    }

    public Integer getTimes_played() {
        return times_played;
    }

    public void setTimes_played(Integer times_played) {
        this.times_played = times_played;
    }
}
