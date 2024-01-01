package com.lohni.musicplayer.interfaces

import com.lohni.musicplayer.utils.enums.PlaybackBehaviour

interface PlaybackControlInterface {
    fun onNextClickListener()
    fun onPreviousClickListener()
    fun onPlaybackBehaviourChangeListener(behaviour: PlaybackBehaviour)
    fun onStateChangeListener()
}