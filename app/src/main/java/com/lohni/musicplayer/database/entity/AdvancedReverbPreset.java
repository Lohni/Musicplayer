package com.lohni.musicplayer.database.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class AdvancedReverbPreset implements Parcelable {

    public AdvancedReverbPreset() {
    }

    @ColumnInfo(name = "ar_id")
    @PrimaryKey(autoGenerate = true)
    @NonNull
    private Integer arId;

    @ColumnInfo(name = "ar_name")
    private String arName;

    @ColumnInfo(name = "ar_active", defaultValue = "0")
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

    @NonNull
    @Override
    public AdvancedReverbPreset clone() {
        AdvancedReverbPreset advancedReverbPreset = new AdvancedReverbPreset();
        try {
            advancedReverbPreset = (AdvancedReverbPreset) super.clone();
        } catch (CloneNotSupportedException e) {
            advancedReverbPreset.setArId(this.getArId());
            advancedReverbPreset.setArName(this.getArName());
            advancedReverbPreset.setArActive(this.getArActive());
            advancedReverbPreset.setArReverbLevel(this.getArReverbLevel());
            advancedReverbPreset.setArReverbDelay(this.getArReverbDelay());
            advancedReverbPreset.setArRoomHfLevel(this.getArRoomHfLevel());
            advancedReverbPreset.setArReflectionLevel(this.getArReflectionLevel());
            advancedReverbPreset.setArReflectionDelay(this.getArReflectionDelay());
            advancedReverbPreset.setArDensity(this.getArDensity());
            advancedReverbPreset.setArDiffusion(this.getArDiffusion());
            advancedReverbPreset.setArDecayTime(this.getArDecayTime());
            advancedReverbPreset.setArDecayHfRatio(this.getArDecayHfRatio());
            advancedReverbPreset.setArMasterLevel(this.getArMasterLevel());
        }
        return advancedReverbPreset;
    }

    @Override
    public String toString() {
        return this.arName;
    }

    protected AdvancedReverbPreset(Parcel in) {
        in.readByte();
        arId = in.readInt();
        in.readByte();
        arName = in.readString();
        in.readByte();
        arActive = in.readInt();
        in.readByte();
        arMasterLevel = in.readInt();
        in.readByte();
        arRoomHfLevel = in.readInt();
        in.readByte();
        arReverbLevel = in.readInt();
        in.readByte();
        arReverbDelay = in.readInt();
        in.readByte();
        arReflectionLevel = in.readInt();
        in.readByte();
        arReflectionDelay = in.readInt();
        in.readByte();
        arDiffusion = in.readInt();
        in.readByte();
        arDensity = in.readInt();
        in.readByte();
        arDecayHfRatio = in.readInt();
        in.readByte();
        arDecayTime = in.readInt();
    }

    public static final Creator<AdvancedReverbPreset> CREATOR = new Creator<AdvancedReverbPreset>() {
        @Override
        public AdvancedReverbPreset createFromParcel(Parcel source) {
            return new AdvancedReverbPreset(source);
        }

        @Override
        public AdvancedReverbPreset[] newArray(int size) {
            return new AdvancedReverbPreset[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        parcel.writeByte((byte) 1);
        parcel.writeInt(arId);
        parcel.writeByte((byte) 1);
        parcel.writeString(arName);
        parcel.writeByte((byte) 1);
        parcel.writeInt(arActive);
        parcel.writeByte((byte) 1);
        parcel.writeInt(arMasterLevel);
        parcel.writeByte((byte) 1);
        parcel.writeInt(arRoomHfLevel);
        parcel.writeByte((byte) 1);
        parcel.writeInt(arReverbLevel);
        parcel.writeByte((byte) 1);
        parcel.writeInt(arReverbDelay);
        parcel.writeByte((byte) 1);
        parcel.writeInt(arReflectionLevel);
        parcel.writeByte((byte) 1);
        parcel.writeInt(arReflectionDelay);
        parcel.writeByte((byte) 1);
        parcel.writeInt(arDiffusion);
        parcel.writeByte((byte) 1);
        parcel.writeInt(arDensity);
        parcel.writeByte((byte) 1);
        parcel.writeInt(arDecayHfRatio);
        parcel.writeByte((byte) 1);
        parcel.writeInt(arDecayTime);
    }
}
