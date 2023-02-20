package com.lohni.musicplayer.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class TrackPlayed {

    @NonNull
    @ColumnInfo(name = "tp_id")
    @PrimaryKey(autoGenerate = true)
    private Integer tpId;

    @NonNull
    @ColumnInfo(name = "tp_t_id")
    private Integer tpTId;

    @NonNull
    @ColumnInfo(name = "tp_played")
    private String tpPlayed;

    @NonNull
    @ColumnInfo(name = "tp_time_played")
    private Long tpTimePlayed;

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

    @NonNull
    public Long getTpTimePlayed() {
        return tpTimePlayed;
    }

    public void setTpTimePlayed(@NonNull Long tpTimePlayed) {
        this.tpTimePlayed = tpTimePlayed;
    }

    @NonNull
    public Integer getTpId() {
        return tpId;
    }

    public void setTpId(@NonNull Integer tpId) {
        this.tpId = tpId;
    }
}
