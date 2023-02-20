package com.lohni.musicplayer.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(primaryKeys = {"atp_ap_id", "atp_tp_id"})
public class AlbumTrackPlayed {

    @NonNull
    @ColumnInfo(name = "atp_ap_id")
    private Integer atpApId;

    @NonNull
    @ColumnInfo(name = "atp_tp_id")
    private Integer atpTpId;

    @NonNull
    public Integer getAtpApId() {
        return atpApId;
    }

    public void setAtpApId(@NonNull Integer atpApId) {
        this.atpApId = atpApId;
    }

    @NonNull
    public Integer getAtpTpId() {
        return atpTpId;
    }

    public void setAtpTpId(@NonNull Integer atpTpId) {
        this.atpTpId = atpTpId;
    }
}
