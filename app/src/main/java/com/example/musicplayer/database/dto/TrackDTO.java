package com.example.musicplayer.database.dto;

import com.example.musicplayer.database.entity.Track;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

public class TrackDTO {
    @Embedded
    private Track track;

    @ColumnInfo(name = "size")
    private Integer size;

    public TrackDTO(Track track, Integer size) {
        this.track = track;
        this.size = size;
    }

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
