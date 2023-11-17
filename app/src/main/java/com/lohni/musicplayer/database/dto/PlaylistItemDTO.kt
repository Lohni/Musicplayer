package com.lohni.musicplayer.database.dto

import androidx.room.Embedded
import com.lohni.musicplayer.database.entity.PlaylistItem
import com.lohni.musicplayer.database.entity.Track

data class PlaylistItemDTO (
    @Embedded val playlistItem: PlaylistItem,
    @Embedded val track: Track
)