package com.example.musicplayer.ui.expandedplaybackcontrol;

public interface ExpandedPlaybackControlInterface {
    void OnStateChangeListener();
    void OnSeekbarChangeListener(int  progress);
    void OnSkipPressedListener();
    void OnSkipPreviousListener();
    void OnCloseListener();
    void OnStartListener();
    void OnShuffleClickListener();
    void OnRepeatClickListener();
    void OnLoopClickListener();
}
