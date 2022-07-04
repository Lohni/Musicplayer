package com.example.musicplayer.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Track {

    @ColumnInfo(name = "t_id")
    @PrimaryKey(autoGenerate = false)
    private Integer tId;

    @ColumnInfo(name = "t_title")
    private String tTitle;

    @ColumnInfo(name = "t_artist")
    private String tArtist;

    @ColumnInfo(name = "t_created")
    private String tCreated;

    @ColumnInfo(name = "t_last_played")
    private String tLastPlayed;

    @ColumnInfo(name = "t_isFavourite", defaultValue = "0")
    @NonNull
    private Integer tIsFavourite;

    @ColumnInfo(name = "t_album_id")
    private Integer tAlbumId;

    @ColumnInfo(name = "t_duration")
    private Integer tDuration;

    @ColumnInfo(name = "t_times_played", defaultValue = "0")
    @NonNull
    private Integer tTimesPlayed;

    @ColumnInfo(name = "t_track_nr")
    private Integer tTrackNr;

    public Integer getTId() {
        return tId;
    }

    public void setTId(Integer tId) {
        this.tId = tId;
    }

    public String getTTitle() {
        return tTitle;
    }

    public void setTTitle(String tTitle) {
        this.tTitle = tTitle;
    }

    public String getTArtist() {
        return tArtist;
    }

    public void setTArtist(String tArtist) {
        this.tArtist = tArtist;
    }

    public String getTCreated() {
        return tCreated;
    }

    public void setTCreated(String tCreated) {
        this.tCreated = tCreated;
    }

    public String getTLastPlayed() {
        return tLastPlayed;
    }

    public void setTLastPlayed(String tLastPlayed) {
        this.tLastPlayed = tLastPlayed;
    }

    public Integer getTIsFavourite() {
        return tIsFavourite;
    }

    public void setTIsFavourite(Integer tIsFavourite) {
        this.tIsFavourite = tIsFavourite;
    }

    public Integer getTAlbumId() {
        return tAlbumId;
    }

    public void setTAlbumId(Integer tAlbumId) {
        this.tAlbumId = tAlbumId;
    }

    public Integer getTDuration() {
        return tDuration;
    }

    public void setTDuration(Integer tDuration) {
        this.tDuration = tDuration;
    }

    public Integer getTTimesPlayed() {
        return tTimesPlayed;
    }

    public void setTTimesPlayed(Integer tTimesPlayed) {
        this.tTimesPlayed = tTimesPlayed;
    }

    public Integer getTTrackNr() {
        return tTrackNr;
    }

    public void setTTrackNr(Integer tTrackNr) {
        this.tTrackNr = tTrackNr;
    }
}
