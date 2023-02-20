package com.lohni.musicplayer.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class PlaylistPlayed {

    @NonNull
    @ColumnInfo(name = "pp_id")
    @PrimaryKey(autoGenerate = true)
    private Integer ppId;

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

    @NonNull
    public Integer getPpId() {
        return ppId;
    }

    public void setPpId(@NonNull Integer ppId) {
        this.ppId = ppId;
    }
}
