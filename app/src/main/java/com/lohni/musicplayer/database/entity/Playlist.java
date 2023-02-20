package com.lohni.musicplayer.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Playlist {

    @NonNull
    @ColumnInfo(name = "p_id")
    @PrimaryKey(autoGenerate = true)
    private Integer pId;

    @ColumnInfo(name = "p_name")
    private String pName;

    @ColumnInfo(name = "p_custom_ordinal")
    private Integer pCustomOrdinal;

    @ColumnInfo(name = "p_favourite", defaultValue = "0")
    private Integer pFavourite;

    @ColumnInfo(name = "p_created")
    private String pCreated;

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

    public String getPCreated() {
        return pCreated;
    }

    public void setPCreated(String pCreated) {
        this.pCreated = pCreated;
    }
}
