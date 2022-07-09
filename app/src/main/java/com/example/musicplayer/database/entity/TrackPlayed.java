package com.example.musicplayer.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(primaryKeys = {"tp_t_id", "tp_played"})
public class TrackPlayed {

    @NonNull
    @ColumnInfo(name = "tp_t_id")
    private Integer tpTId;

    @NonNull
    @ColumnInfo(name = "tp_played")
    private String tpPlayed;

    @NonNull
    public Integer getTpTId() {
        return tpTId;
    }

    public void setTpTId(@NonNull Integer tpTId) {
        this.tpTId = tpTId;
    }

    @NonNull
    public String getTpPlayed() {
        return tpPlayed;
    }

    public void setTpPlayed(@NonNull String tpPlayed) {
        this.tpPlayed = tpPlayed;
    }
}
