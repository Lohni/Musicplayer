package com.lohni.musicplayer.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(primaryKeys = {"ptp_tp_id", "ptp_pp_id"})
public class PlaylistTrackPlayed {

    @NonNull
    @ColumnInfo(name = "ptp_tp_id")
    private Integer ptpTpId;

    @NonNull
    @ColumnInfo(name = "ptp_pp_id")
    private Integer ptpPpId;

    @NonNull
    public Integer getPtpPpId() {
        return ptpPpId;
    }

    public void setPtpPpId(@NonNull Integer ptpPpId) {
        this.ptpPpId = ptpPpId;
    }

    @NonNull
    public Integer getPtpTpId() {
        return ptpTpId;
    }

    public void setPtpTpId(@NonNull Integer ptpTpId) {
        this.ptpTpId = ptpTpId;
    }
}
