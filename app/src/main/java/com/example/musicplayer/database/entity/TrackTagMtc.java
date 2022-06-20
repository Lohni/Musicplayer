package com.example.musicplayer.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(primaryKeys = {"ttm_t_id", "ttm_tag_id"})
public class TrackTagMtc {

    @NonNull
    @ColumnInfo(name = "ttm_t_id")
    private Integer ttmTId;

    @NonNull
    @ColumnInfo(name = "ttm_tag_id")
    private Integer ttmTagId;

    public Integer getTtmTId() {
        return ttmTId;
    }

    public void setTtmTId(Integer ttmTId) {
        this.ttmTId = ttmTId;
    }

    public Integer getTtmTagId() {
        return ttmTagId;
    }

    public void setTtmTagId(Integer ttmTagId) {
        this.ttmTagId = ttmTagId;
    }
}
