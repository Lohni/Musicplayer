package com.lohni.musicplayer.database.dto;

import com.lohni.musicplayer.database.entity.Playlist;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

public class PlaylistDTO extends DashboardDTO {
    @Embedded
    private Playlist playlist;

    @ColumnInfo(name = "size")
    private String size;

    public PlaylistDTO(Playlist playlist, String size) {
        this.playlist = playlist;
        this.size = size;
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    @Override
    public Integer getId() {
        return playlist.getPId();
    }
}
