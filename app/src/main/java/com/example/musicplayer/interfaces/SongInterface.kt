package com.example.musicplayer.interfaces

import com.example.musicplayer.database.entity.Track

interface SongInterface {
    fun onSongSelectedListener(track: Track)
    fun onSongListCreatedListener(trackList: List<Track>)
    fun onAddSongsToSonglistListener(trackList: List<Track>)
}