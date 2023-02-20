package com.lohni.musicplayer.database.dto;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

public class AlbumDTO extends DashboardDTO {
    @Embedded
    private AlbumTrackDTO album;

    @ColumnInfo(name = "size")
    private String size;

    public AlbumDTO(AlbumTrackDTO album, String size) {
        this.album = album;
        this.size = size;
    }

    public AlbumTrackDTO getAlbum() {
        return album;
    }

    public void setAlbum(AlbumTrackDTO album) {
        this.album = album;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    @Override
    public Integer getId() {
        return getAlbum().album.getAId();
    }
}
