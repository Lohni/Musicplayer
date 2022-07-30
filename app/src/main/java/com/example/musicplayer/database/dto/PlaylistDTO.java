package com.example.musicplayer.database.dto;

import com.example.musicplayer.database.entity.Playlist;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

public class PlaylistDTO {
    @Embedded
    private Playlist playlist;

    @ColumnInfo(name = "size")
    private Integer size;

    public PlaylistDTO(Playlist playlist, Integer size) {
        this.playlist = playlist;
        this.size = size;
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
