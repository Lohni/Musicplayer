package com.example.musicplayer.entities;

import com.example.musicplayer.adapter.AlbumAdapter;

public class AlbumResolver {

    private long albumId, artistId;
    private String albumName, artistName, albumArtUri;
    private int numSongs = 0;

    public AlbumResolver(long albumId, long artistId, int numSongs, String albumName, String artistName, String albumArtUri){
        this.albumId = albumId;
        this.artistId = artistId;
        this.numSongs = numSongs;
        this.albumName = albumName;
        this.artistName = artistName;
        this.albumArtUri = albumArtUri;
    }

    public String getAlbumArtUri() {
        return albumArtUri;
    }

    public void setAlbumArtUri(String albumArtUri) {
        this.albumArtUri = albumArtUri;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public long getArtistId() {
        return artistId;
    }

    public void setArtistId(long artistId) {
        this.artistId = artistId;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public int getNumSongs() {
        return numSongs;
    }

    public void setNumSongs(int numSongs) {
        this.numSongs = numSongs;
    }
}
