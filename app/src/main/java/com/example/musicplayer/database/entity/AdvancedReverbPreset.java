package com.example.musicplayer.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class AdvancedReverbPreset {

    @ColumnInfo(name = "ar_id")
    @PrimaryKey(autoGenerate = true)
    private Integer arId;

    @ColumnInfo(name = "ar_name")
    private String arName;

    @ColumnInfo(name = "ar_active")
    private Integer arActive;

    @ColumnInfo(name = "ar_master_level")
    private Integer arMasterLevel;

    @ColumnInfo(name = "ar_room_hf_level")
    private Integer arRoomHfLevel;

    @ColumnInfo(name = "ar_reverb_level")
    private Integer arReverbLevel;

    @ColumnInfo(name = "ar_reverb_delay")
    private Integer arReverbDelay;

    @ColumnInfo(name = "ar_reflection_level")
    private Integer arReflectionLevel;

    @ColumnInfo(name = "ar_reflection_delay")
    private Integer arReflectionDelay;

    @ColumnInfo(name = "ar_diffusion")
    private Integer arDiffusion;

    @ColumnInfo(name = "ar_density")
    private Integer arDensity;

    @ColumnInfo(name = "ar_decay_hf_ratio")
    private Integer arDecayHfRatio;

    @ColumnInfo(name = "ar_decay_time")
    private Integer arDecayTime;

    public Integer getArId() {
        return arId;
    }

    public void setArId(Integer arId) {
        this.arId = arId;
    }

    public String getArName() {
        return arName;
    }

    public void setArName(String arName) {
        this.arName = arName;
    }

    public Integer getArActive() {
        return arActive;
    }

    public void setArActive(Integer arActive) {
        this.arActive = arActive;
    }

    public Integer getArMasterLevel() {
        return arMasterLevel;
    }

    public void setArMasterLevel(Integer arMasterLevel) {
        this.arMasterLevel = arMasterLevel;
    }

    public Integer getArRoomHfLevel() {
        return arRoomHfLevel;
    }

    public void setArRoomHfLevel(Integer arRoomHfLevel) {
        this.arRoomHfLevel = arRoomHfLevel;
    }

    public Integer getArReverbLevel() {
        return arReverbLevel;
    }

    public void setArReverbLevel(Integer arReverbLevel) {
        this.arReverbLevel = arReverbLevel;
    }

    public Integer getArReverbDelay() {
        return arReverbDelay;
    }

    public void setArReverbDelay(Integer arReverbDelay) {
        this.arReverbDelay = arReverbDelay;
    }

    public Integer getArReflectionLevel() {
        return arReflectionLevel;
    }

    public void setArReflectionLevel(Integer arReflectionLevel) {
        this.arReflectionLevel = arReflectionLevel;
    }

    public Integer getArDiffusion() {
        return arDiffusion;
    }

    public void setArDiffusion(Integer arDiffusion) {
        this.arDiffusion = arDiffusion;
    }

    public Integer getArDensity() {
        return arDensity;
    }

    public void setArDensity(Integer arDensity) {
        this.arDensity = arDensity;
    }

    public Integer getArDecayHfRatio() {
        return arDecayHfRatio;
    }

    public void setArDecayHfRatio(Integer arDecayHfRatio) {
        this.arDecayHfRatio = arDecayHfRatio;
    }

    public Integer getArDecayTime() {
        return arDecayTime;
    }

    public void setArDecayTime(Integer arDecayTime) {
        this.arDecayTime = arDecayTime;
    }

    public Integer getArReflectionDelay() {
        return arReflectionDelay;
    }

    public void setArReflectionDelay(Integer arReflectionDelay) {
        this.arReflectionDelay = arReflectionDelay;
    }
}
