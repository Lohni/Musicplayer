package com.lohni.musicplayer.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(primaryKeys = {"ptm_p_id", "ptm_tag_id"})
public class PlaylistTagMtc {

    @NonNull
    @ColumnInfo(name = "ptm_p_id")
    private Integer ptmPId;

    @NonNull
    @ColumnInfo(name = "ptm_tag_id")
    private Integer ptmTagId;

    public Integer getPtmPId() {
        return ptmPId;
    }

    public void setPtmPId(Integer ptmPId) {
        this.ptmPId = ptmPId;
    }

    public Integer getPtmTagId() {
        return ptmTagId;
    }

    public void setPtmTagId(Integer ptmTagId) {
        this.ptmTagId = ptmTagId;
    }
}
