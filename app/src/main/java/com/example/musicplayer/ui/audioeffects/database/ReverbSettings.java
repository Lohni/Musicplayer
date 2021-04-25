package com.example.musicplayer.ui.audioeffects.database;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ReverbSettings {

    @ColumnInfo(name = "preset_id")
    @PrimaryKey(autoGenerate = true)
    private int preset_id;

    @NonNull
    private String reverb_preset_name;

    @ColumnInfo(defaultValue = "0")
    private int isSelected;


    private short masterLevel;

    private short roomHFLevel;

    private short reverbLevel;

    private int reverbDelay;

    private short reflectionLevel;

    private int reflectionDelay;

    private short diffusion;

    private short density;

    private short decayHFRatio;

    private int decayTime;


    public int getPreset_id() {
        return preset_id;
    }

    public void setPreset_id(int preset_id) {
        this.preset_id = preset_id;
    }

    public int getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(int isSelected) {
        this.isSelected = isSelected;
    }

    public short getMasterLevel() {
        return masterLevel;
    }

    public void setMasterLevel(short masterLevel) {
        this.masterLevel = masterLevel;
    }

    public short getRoomHFLevel() {
        return roomHFLevel;
    }

    public void setRoomHFLevel(short roomHFLevel) {
        this.roomHFLevel = roomHFLevel;
    }

    public short getReverbLevel() {
        return reverbLevel;
    }

    public void setReverbLevel(short reverbLevel) {
        this.reverbLevel = reverbLevel;
    }

    public int getReverbDelay() {
        return reverbDelay;
    }

    public void setReverbDelay(int reverbDelay) {
        this.reverbDelay = reverbDelay;
    }

    public short getReflectionLevel() {
        return reflectionLevel;
    }

    public void setReflectionLevel(short reflectionLevel) {
        this.reflectionLevel = reflectionLevel;
    }

    public int getReflectionDelay() {
        return reflectionDelay;
    }

    public void setReflectionDelay(int reflectionDelay) {
        this.reflectionDelay = reflectionDelay;
    }

    public short getDiffusion() {
        return diffusion;
    }

    public void setDiffusion(short diffusion) {
        this.diffusion = diffusion;
    }

    public short getDensity() {
        return density;
    }

    public void setDensity(short density) {
        this.density = density;
    }

    public short getDecayHFRatio() {
        return decayHFRatio;
    }

    public void setDecayHFRatio(short decayHFRatio) {
        this.decayHFRatio = decayHFRatio;
    }

    public int getDecayTime() {
        return decayTime;
    }

    public void setDecayTime(int decayTime) {
        this.decayTime = decayTime;
    }

    public void setReverb_preset_name(@NonNull String equalizer_preset_name) {
        this.reverb_preset_name = equalizer_preset_name;
    }

    @NonNull
    public String getReverb_preset_name() {
        return reverb_preset_name;
    }

    @Override
    public String toString() {
        return reverb_preset_name;
    }

}

