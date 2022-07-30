package com.example.musicplayer.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class EqualizerPreset {

    @ColumnInfo(name = "eq_id")
    @PrimaryKey(autoGenerate = true)
    @NonNull
    private Integer eqId;

    @ColumnInfo(name = "eq_name")
    private String eqName;

    @ColumnInfo(name = "eq_active", defaultValue = "0")
    private Integer eqActive;

    @ColumnInfo(name = "eq_level1")
    private Integer eqLevel1;

    @ColumnInfo(name = "eq_level2")
    private Integer eqLevel2;

    @ColumnInfo(name = "eq_level3")
    private Integer eqLevel3;

    @ColumnInfo(name = "eq_level4")
    private Integer eqLevel4;

    @ColumnInfo(name = "eq_level5")
    private Integer eqLevel5;

    public Integer getEqId() {
        return eqId;
    }

    public void setEqId(Integer eqId) {
        this.eqId = eqId;
    }

    public String getEqName() {
        return eqName;
    }

    public void setEqName(String eqName) {
        this.eqName = eqName;
    }

    public Integer getEqActive() {
        return eqActive;
    }

    public void setEqActive(Integer eqActive) {
        this.eqActive = eqActive;
    }

    public Integer getEqLevel1() {
        return eqLevel1;
    }

    public void setEqLevel1(Integer eqLevel1) {
        this.eqLevel1 = eqLevel1;
    }

    public Integer getEqLevel2() {
        return eqLevel2;
    }

    public void setEqLevel2(Integer eqLevel2) {
        this.eqLevel2 = eqLevel2;
    }

    public Integer getEqLevel3() {
        return eqLevel3;
    }

    public void setEqLevel3(Integer eqLevel3) {
        this.eqLevel3 = eqLevel3;
    }

    public Integer getEqLevel4() {
        return eqLevel4;
    }

    public void setEqLevel4(Integer eqLevel4) {
        this.eqLevel4 = eqLevel4;
    }

    public Integer getEqLevel5() {
        return eqLevel5;
    }

    public void setEqLevel5(Integer eqLevel5) {
        this.eqLevel5 = eqLevel5;
    }

    @NonNull
    @Override
    public String toString() {
        return this.eqName;
    }
}
