package com.lohni.musicplayer.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Album {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "a_id")
    private Integer aId;

    @ColumnInfo(name = "a_name")
    private String aName;

    @ColumnInfo(name = "a_artist_name")
    private String aArtistName;

    @ColumnInfo(name = "a_artist_id")
    private Integer aArtistId;

    @ColumnInfo(name = "a_art_uri")
    private String aArtUri;

    @ColumnInfo(name = "a_is_favourite", defaultValue = "0")
    private Integer aIsFavourite;

    @ColumnInfo(name = "a_num_songs")
    private Integer aNumSongs;

    public Integer getAId() {
        return aId;
    }

    public void setAId(Integer aId) {
        this.aId = aId;
    }

    public String getAName() {
        return aName;
    }

    public void setAName(String aName) {
        this.aName = aName;
    }

    public String getAArtistName() {
        return aArtistName;
    }

    public void setAArtistName(String aArtistName) {
        this.aArtistName = aArtistName;
    }

    public Integer getAArtistId() {
        return aArtistId;
    }

    public void setAArtistId(Integer aArtistId) {
        this.aArtistId = aArtistId;
    }

    public String getAArtUri() {
        return aArtUri;
    }

    public void setAArtUri(String aArtUri) {
        this.aArtUri = aArtUri;
    }

    public Integer getAIsFavourite() {
        return aIsFavourite;
    }

    public void setAIsFavourite(Integer aIsFavourite) {
        this.aIsFavourite = aIsFavourite;
    }

    public Integer getANumSongs() {
        return aNumSongs;
    }

    public void setANumSongs(Integer aNumSongs) {
        this.aNumSongs = aNumSongs;
    }
}
