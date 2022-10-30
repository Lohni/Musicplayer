package com.lohni.musicplayer.interfaces

import com.lohni.musicplayer.database.entity.Track
import com.lohni.musicplayer.utils.enums.DashboardListType

interface SongInterface {
    fun onSongSelectedListener(track: Track)
    fun onSongListCreatedListener(trackList: List<Track>, listType: DashboardListType)
    fun onAddSongsToSonglistListener(trackList: List<Track>, next: Boolean)
    fun onSongsRemoveListener(track: List<Track>)
    fun onOrderChangeListener(fromPosition : Int, toPosition: Int)
    fun onRemoveAllSongsListener()
}