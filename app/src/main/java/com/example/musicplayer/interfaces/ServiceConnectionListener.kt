package com.example.musicplayer.interfaces

import com.example.musicplayer.MusicService

interface ServiceConnectionListener {
    fun onServiceConnected(musicService: MusicService)
}