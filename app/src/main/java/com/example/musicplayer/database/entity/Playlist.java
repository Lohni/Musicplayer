package com.example.musicplayer.database.entity;

import java.util.List;
import java.util.Set;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Relation;

@Entity
public class Playlist {

    @ColumnInfo(name = "p_id")
    @PrimaryKey(autoGenerate = true)
    private Integer pId;

    @ColumnInfo(name = "p_name")
    private String pName;

    @ColumnInfo(name = "p_last_played")
    private String pLastPlayed;

    @ColumnInfo(name = "p_times_played")
    private Integer pTimesPlayed;

    @ColumnInfo(name = "p_custom_ordinal")
    private Integer pCustomOrdinal;

    @ColumnInfo(name = "p_favourite")
    private Integer pFavourite;

    public Integer getPId() {
        return pId;
    }

    public void setPId(Integer pId) {
        this.pId = pId;
    }

    public String getPName() {
        return pName;
    }

    public void setPName(String pName) {
        this.pName = pName;
    }

    public String getPLastPlayed() {
        return pLastPlayed;
    }

    public void setPLastPlayed(String pLastPlayed) {
        this.pLastPlayed = pLastPlayed;
    }

    public Integer getPTimesPlayed() {
        return pTimesPlayed;
    }

    public void setPTimesPlayed(Integer pTimesPlayed) {
        this.pTimesPlayed = pTimesPlayed;
    }

    public Integer getPCustomOrdinal() {
        return pCustomOrdinal;
    }

    public void setPCustomOrdinal(Integer pCustomOrdinal) {
        this.pCustomOrdinal = pCustomOrdinal;
    }

    public Integer getPFavourite() {
        return pFavourite;
    }

    public void setPFavourite(Integer pFavourite) {
        this.pFavourite = pFavourite;
    }
}
