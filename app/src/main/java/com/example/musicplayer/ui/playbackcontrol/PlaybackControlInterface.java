package com.example.musicplayer.ui.playbackcontrol;

import android.view.View;
import android.widget.SeekBar;

public interface PlaybackControlInterface {
    void OnStateChangeListener();
    void OnSeekbarChangeListener(int  progress);
    void OnSkipPressedListener();
    void OnExpandListener(SeekBar view, View text);
}
