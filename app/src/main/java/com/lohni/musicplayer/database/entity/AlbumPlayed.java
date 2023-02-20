package com.lohni.musicplayer.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class AlbumPlayed {

     @NonNull
     @ColumnInfo(name = "ap_id")
     @PrimaryKey(autoGenerate = true)
     private Integer apId;

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

    @NonNull
    public Integer getApId() {
        return apId;
    }

    public void setApId(@NonNull Integer apId) {
        this.apId = apId;
    }
}
