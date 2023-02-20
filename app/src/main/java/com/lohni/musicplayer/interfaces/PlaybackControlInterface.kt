package com.lohni.musicplayer.interfaces

import com.lohni.musicplayer.utils.enums.PlaybackBehaviourState

interface PlaybackControlInterface {
    fun onNextClickListener()
    fun onPreviousClickListener()
    fun onProgressChangeListener(progress: Int)
    fun onPlaybackBehaviourChangeListener(behaviour: PlaybackBehaviourState)
    fun onStateChangeListener()
}