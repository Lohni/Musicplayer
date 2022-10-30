package com.lohni.musicplayer.interfaces

import com.lohni.musicplayer.core.MusicService

interface ServiceConnectionListener {
    fun onServiceConnected(musicService: MusicService)
}