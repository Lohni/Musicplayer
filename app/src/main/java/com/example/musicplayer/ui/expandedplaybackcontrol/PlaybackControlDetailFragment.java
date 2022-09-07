package com.example.musicplayer.ui.expandedplaybackcontrol;

import com.example.musicplayer.database.entity.Track;

import androidx.fragment.app.Fragment;

public abstract class PlaybackControlDetailFragment extends Fragment {
    protected Track currentTrack;

    public PlaybackControlDetailFragment() {
    }

    public void setCurrentTrack(Track currentTrack) {
        this.currentTrack = currentTrack;
    }
}