package com.example.musicplayer.inter

import com.example.musicplayer.MusicService

interface ServiceConnectionListener {
    fun onServiceConnected(musicService: MusicService)
}