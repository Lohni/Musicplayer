package com.lohni.musicplayer.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Preference {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "pref_id")
    private Integer prefId;

    @ColumnInfo(name = "pref_value")
    private String prefValue;

    @NonNull
    public Integer getPrefId() {
        return prefId;
    }

    public void setPrefId(@NonNull Integer prefId) {
        this.prefId = prefId;
    }

    public String getPrefValue() {
        return prefValue;
    }

    public void setPrefValue(String prefValue) {
        this.prefValue = prefValue;
    }
}
