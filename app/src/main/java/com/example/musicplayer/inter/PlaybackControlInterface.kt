package com.example.musicplayer.inter

import com.example.musicplayer.utils.enums.PlaybackBehaviour

interface PlaybackControlInterface {
    fun onNextClickListener()
    fun onPreviousClickListener()
    fun onProgressChangeListener(progress: Int)
    fun onPlaybackBehaviourChangeListener(behaviour: PlaybackBehaviour.PlaybackBehaviourState)
    fun onStateChangeListener()
}