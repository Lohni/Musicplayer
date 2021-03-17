package com.example.musicplayer.entities;

public class MusicResolver {
    private long id, album_id, duration=0;
    private String artist,title;

    private boolean isSelected;

    public MusicResolver(){}

    public MusicResolver(long id, long album_id, String artist, String title) {
        this.id = id;
        this.album_id = album_id;
        this.artist = artist;
        this.title = title;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAlbum_id() {
        return album_id;
    }

    public void setAlbum_id(long album_id) {
        this.album_id = album_id;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public void setDuration(long duration){this.duration = duration;}

    public long getDuration(){return duration;}
}
