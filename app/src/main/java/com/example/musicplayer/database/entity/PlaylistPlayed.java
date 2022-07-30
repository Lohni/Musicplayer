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
    @ColumnInfo(name = "pp_time_played")
    private Long ppTimePlayed;

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

    @NonNull
    public Long getPpTimePlayed() {
        return ppTimePlayed;
    }

    public void setPpTimePlayed(@NonNull Long ppTimePlayed) {
        this.ppTimePlayed = ppTimePlayed;
    }
}
