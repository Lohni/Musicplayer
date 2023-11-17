package com.lohni.musicplayer.database.dto;

import com.lohni.musicplayer.database.entity.Track;
import com.lohni.musicplayer.utils.GeneralUtils;
import com.lohni.musicplayer.utils.enums.ListFilterType;

import java.time.LocalDateTime;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

public class TrackDTO extends DashboardDTO {
    @Embedded
    private Track track;

    @ColumnInfo(name = "size")
    private String size;

    public TrackDTO(Track track, String size) {
        this.track = track;
        this.size = size;
    }

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    @Override
    public Integer getId() {
        return track.getTId();
    }

    public String getAsInfoText(ListFilterType listFilterType) {
        if (size != null || listFilterType.equals(ListFilterType.LAST_CREATED)) {
            if (listFilterType.equals(ListFilterType.LAST_PLAYED)) {
                LocalDateTime ldt = LocalDateTime.parse(size, GeneralUtils.DB_TIMESTAMP);
                return GeneralUtils.getTimeDiffAsText(ldt);
            } else if (listFilterType.equals(ListFilterType.LAST_CREATED) && track.getTCreated() != null) {
                LocalDateTime dbTime = LocalDateTime.parse(track.getTCreated(), GeneralUtils.DB_TIMESTAMP);
                return GeneralUtils.getTimeDiffAsText(dbTime);
            } else if (listFilterType.equals(ListFilterType.TIMES_PLAYED)) {
                return size;
            } else if (listFilterType.equals(ListFilterType.TIME_PLAYED)) {
                return GeneralUtils.convertTimeWithUnit(Integer.parseInt(size));
            }
        }
        return "";
    }
}
