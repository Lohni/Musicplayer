package com.lohni.musicplayer.database.dto;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.lohni.musicplayer.database.entity.Album;
import com.lohni.musicplayer.database.entity.Track;

import java.util.List;

public class AlbumTrackDTO {
    @Embedded
    public Album album;

    @Relation(parentColumn = "a_id", entityColumn = "t_album_id")
    public List<Track> trackList;
}
