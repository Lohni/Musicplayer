package com.example.musicplayer.database.entity;

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

    @NonNull
    @ColumnInfo(name = "pref_key")
    private String prefKey;

    @ColumnInfo(name = "pref_value")
    private String prefValue;

    @NonNull
    public Integer getPrefId() {
        return prefId;
    }

    public void setPrefId(@NonNull Integer prefId) {
        this.prefId = prefId;
    }

    @NonNull
    public String getPrefKey() {
        return prefKey;
    }

    public void setPrefKey(@NonNull String prefKey) {
        this.prefKey = prefKey;
    }

    public String getPrefValue() {
        return prefValue;
    }

    public void setPrefValue(String prefValue) {
        this.prefValue = prefValue;
    }
}
