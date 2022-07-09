package com.example.musicplayer.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(primaryKeys = {"ap_a_id", "ap_played"})
public class AlbumPlayed {

    @NonNull
    @ColumnInfo(name = "ap_a_id")
    private Integer apAId;

    @NonNull
    @ColumnInfo(name = "ap_played")
    private String apPlayed;

    @NonNull
    public Integer getApAId() {
        return apAId;
    }

    public void setApAId(@NonNull Integer apAId) {
        this.apAId = apAId;
    }

    @NonNull
    public String getApPlayed() {
        return apPlayed;
    }

    public void setApPlayed(@NonNull String apPlayed) {
        this.apPlayed = apPlayed;
    }
}
