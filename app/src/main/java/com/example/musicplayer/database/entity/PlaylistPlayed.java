package com.example.musicplayer.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(primaryKeys = {"pp_p_id", "pp_played"})
public class PlaylistPlayed {

    @NonNull
    @ColumnInfo(name = "pp_p_id")
    private Integer ppPId;

    @NonNull
    @ColumnInfo(name = "pp_played")
    private String ppPlayed;

    @NonNull
    public Integer getPpPId() {
        return ppPId;
    }

    public void setPpPId(@NonNull Integer ppPId) {
        this.ppPId = ppPId;
    }

    @NonNull
    public String getPpPlayed() {
        return ppPlayed;
    }

    public void setPpPlayed(@NonNull String ppPlayed) {
        this.ppPlayed = ppPlayed;
    }
}
