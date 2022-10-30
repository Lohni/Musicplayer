package com.lohni.musicplayer.database.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Track implements Parcelable {

    public Track(){}

    @NonNull
    @ColumnInfo(name = "t_id")
    @PrimaryKey(autoGenerate = false)
    private Integer tId;

    @ColumnInfo(name = "t_title")
    private String tTitle;

    @ColumnInfo(name = "t_artist")
    private String tArtist;

    @ColumnInfo(name = "t_created")
    private String tCreated;

    @ColumnInfo(name = "t_isFavourite", defaultValue = "0")
    private Integer tIsFavourite;

    @ColumnInfo(name = "t_album_id")
    private Integer tAlbumId;

    @ColumnInfo(name = "t_duration")
    private Integer tDuration;

    @ColumnInfo(name = "t_track_nr")
    private Integer tTrackNr;

    @ColumnInfo(name = "t_deleted")
    private Integer tDeleted;

    public Integer getTId() {
        return tId;
    }

    public void setTId(Integer tId) {
        this.tId = tId;
    }

    public String getTTitle() {
        return tTitle;
    }

    public void setTTitle(String tTitle) {
        this.tTitle = tTitle;
    }

    public String getTArtist() {
        return tArtist;
    }

    public void setTArtist(String tArtist) {
        this.tArtist = tArtist;
    }

    public String getTCreated() {
        return tCreated;
    }

    public void setTCreated(String tCreated) {
        this.tCreated = tCreated;
    }

    public Integer getTIsFavourite() {
        return tIsFavourite;
    }

    public void setTIsFavourite(Integer tIsFavourite) {
        this.tIsFavourite = tIsFavourite;
    }

    public Integer getTAlbumId() {
        return tAlbumId;
    }

    public void setTAlbumId(Integer tAlbumId) {
        this.tAlbumId = tAlbumId;
    }

    public Integer getTDuration() {
        return tDuration;
    }

    public void setTDuration(Integer tDuration) {
        this.tDuration = tDuration;
    }

    public Integer getTTrackNr() {
        return tTrackNr;
    }

    public void setTTrackNr(Integer tTrackNr) {
        this.tTrackNr = tTrackNr;
    }

    public Integer getTDeleted() {
        return tDeleted;
    }

    public void setTDeleted(Integer tDeleted) {
        this.tDeleted = tDeleted;
    }

    protected Track(Parcel in) {
        in.readByte();
        tId = in.readInt();

        in.readByte();
        tTitle = in.readString();

        in.readByte();
        tArtist = in.readString();

        tCreated = (in.readByte() == 0) ? "" : in.readString();

        tIsFavourite = (in.readByte() == 0) ? 0 : in.readInt();
        tAlbumId = (in.readByte() == 0) ? null : in.readInt();
        tDuration = (in.readByte() == 0) ? null : in.readInt();
        tTrackNr = (in.readByte() == 0) ? null : in.readInt();
        tDeleted = (in.readByte() == 0) ? 0 : in.readInt();
    }

    public static final Creator<Track> CREATOR = new Creator<Track>() {
        @Override
        public Track createFromParcel(Parcel in) {
            return new Track(in);
        }

        @Override
        public Track[] newArray(int size) {
            return new Track[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
  
    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeByte((byte) 1);
        parcel.writeInt(tId);

        parcel.writeByte((byte) 1);
        parcel.writeString(tTitle);

        parcel.writeByte((byte) 1);
        parcel.writeString(tArtist);

        if (tCreated == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeString(tCreated);
        }
        if (tIsFavourite == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(tIsFavourite);
        }
        if (tAlbumId == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(tAlbumId);
        }
        if (tDuration == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(tDuration);
        }
        if (tTrackNr == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(tTrackNr);
        }
        if (tDeleted == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(tDeleted);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Track track = (Track) o;
        return tId.equals(track.tId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tId);
    }
}
