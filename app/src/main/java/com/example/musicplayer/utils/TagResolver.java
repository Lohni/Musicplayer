package com.example.musicplayer.utils;

public class TagResolver {

    private String title, artist, album, genre, year, composer, trackid;
    private int titlePos = 0, artistPos = 0, albumPos = 0, genrePos = 0, yearPos = 0, composerPos = 0;
    private ID3V4Frame titleFrame, artistFrame, albumFrame, genreFrame, yearFrame, composerFrame;

    public TagResolver(){}

    public int getTitlePos() {
        return titlePos;
    }

    public void setTitlePos(int titlePos) {
        this.titlePos = titlePos;
    }

    public int getArtistPos() {
        return artistPos;
    }

    public void setArtistPos(int artistPos) {
        this.artistPos = artistPos;
    }

    public int getAlbumPos() {
        return albumPos;
    }

    public void setAlbumPos(int albumPos) {
        this.albumPos = albumPos;
    }

    public int getGenrePos() {
        return genrePos;
    }

    public void setGenrePos(int genrePos) {
        this.genrePos = genrePos;
    }

    public int getYearPos() {
        return yearPos;
    }

    public void setYearPos(int yearPos) {
        this.yearPos = yearPos;
    }

    public int getComposerPos() {
        return composerPos;
    }

    public void setComposerPos(int composerPos) {
        this.composerPos = composerPos;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getComposer() {
        return composer;
    }

    public void setComposer(String comoser) {
        this.composer = comoser;
    }

    public String getTrackid() {
        return trackid;
    }

    public void setTrackid(String trackid) {
        this.trackid = trackid;
    }
}
