package com.example.musicplayer.ui.audioeffects.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity
public class EqualizerSettings {

    @PrimaryKey(autoGenerate = true)
    private int equalizer_id = 0;

    @NonNull
    private String equalizer_preset_name;

    @ColumnInfo(defaultValue = "0")
    private int isSelected;

    @TypeConverters(ConvertArrayToString.class)
    private short[] bandLevels;


    public int getEqualizer_id() {
        return equalizer_id;
    }

    public void setEqualizer_id(int equalizer_id) {
        this.equalizer_id = equalizer_id;
    }

    @NonNull
    public String getEqualizer_preset_name() {
        return equalizer_preset_name;
    }

    public void setEqualizer_preset_name(@NonNull String equalizer_preset_name) {
        this.equalizer_preset_name = equalizer_preset_name;
    }

    public int getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(int isSelected) {
        this.isSelected = isSelected;
    }

    public short[] getBandLevels() {
        return bandLevels;
    }

    public void setBandLevels(short[] bandLevels) {
        this.bandLevels = bandLevels;
    }

    @Override
    public String toString() {
        return equalizer_preset_name;
    }
}
