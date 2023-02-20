package com.lohni.musicplayer.interfaces

import com.lohni.musicplayer.database.entity.Track

interface QueueControlInterface {
    fun onSongSelectedListener(track: Track)
    fun onSongListCreatedListener(trackList: List<Track>, listTypePayload: Any, play: Boolean)
    fun onAddSongsToSonglistListener(trackList: List<Track>, next: Boolean)
    fun onSongsRemoveListener(track: List<Track>)
    fun onOrderChangeListener(fromPosition : Int, toPosition: Int)
    fun onRemoveAllSongsListener()
}