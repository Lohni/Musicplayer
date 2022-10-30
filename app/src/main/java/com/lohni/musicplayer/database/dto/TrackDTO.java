package com.lohni.musicplayer.database.dto;

import com.lohni.musicplayer.database.entity.Track;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

public class TrackDTO {
    @Embedded
    private Track track;

    @ColumnInfo(name = "size")
    private String size;

    public TrackDTO(Track track, String size) {
        this.track = track;
        this.size = size;
    }

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
