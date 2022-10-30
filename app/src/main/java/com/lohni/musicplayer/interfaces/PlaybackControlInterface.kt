package com.lohni.musicplayer.interfaces

import com.lohni.musicplayer.utils.enums.PlaybackBehaviour

interface PlaybackControlInterface {
    fun onNextClickListener()
    fun onPreviousClickListener()
    fun onProgressChangeListener(progress: Int)
    fun onPlaybackBehaviourChangeListener(behaviour: PlaybackBehaviour.PlaybackBehaviourState)
    fun onStateChangeListener()
}