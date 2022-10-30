package com.example.musicplayer.interfaces

import com.example.musicplayer.core.MusicService

interface ServiceConnectionListener {
    fun onServiceConnected(musicService: MusicService)
}