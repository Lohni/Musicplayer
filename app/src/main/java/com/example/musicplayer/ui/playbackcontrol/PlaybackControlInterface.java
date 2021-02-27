package com.example.musicplayer.ui.playbackcontrol;

import android.view.View;
import android.widget.SeekBar;

import com.example.musicplayer.ui.views.PlaybackControlSeekbar;

public interface PlaybackControlInterface {
    void OnStateChangeListener();
    void OnSeekbarChangeListener(int  progress);
    void OnSkipPressedListener();
    void OnExpandListener(PlaybackControlSeekbar view, View text);
}
