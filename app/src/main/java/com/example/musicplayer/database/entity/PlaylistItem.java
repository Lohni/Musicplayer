package com.example.musicplayer.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(primaryKeys = {"pi_p_id", "pi_t_id"})
public class PlaylistItem {

    @NonNull
    @ColumnInfo(name = "pi_p_id")
    private Integer piPId;

    @NonNull
    @ColumnInfo(name = "pi_t_id")
    private Integer piTId;

    @ColumnInfo(name = "pi_custom_ordinal")
    private Integer piCustomOrdinal;

    public Integer getPiPId() {
        return piPId;
    }

    public void setPiPId(Integer piPId) {
        this.piPId = piPId;
    }

    public Integer getPiTId() {
        return piTId;
    }

    public void setPiTId(Integer piTId) {
        this.piTId = piTId;
    }

    public Integer getPiCustomOrdinal() {
        return piCustomOrdinal;
    }

    public void setPiCustomOrdinal(Integer piCustomOrdinal) {
        this.piCustomOrdinal = piCustomOrdinal;
    }
}
