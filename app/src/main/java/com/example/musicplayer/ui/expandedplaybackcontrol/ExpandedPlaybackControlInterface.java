package com.example.musicplayer.ui.expandedplaybackcontrol;

import com.example.musicplayer.utils.enums.PlaybackBehaviour;

public interface ExpandedPlaybackControlInterface {
    void OnStateChangeListener();
    void OnSeekbarChangeListener(int  progress);
    void OnSkipPressedListener();
    void OnSkipPreviousListener();
    void OnBehaviourChangedListener(PlaybackBehaviour.PlaybackBehaviourState newState);
    void OnCloseListener();
}
