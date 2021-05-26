package com.example.musicplayer.ui.playlist;

import android.view.View;

import com.example.musicplayer.entities.MusicResolver;

import java.util.ArrayList;

public interface PlaylistInterface {
    void OnClickListener(String table, View view);
    void OnPlaylistResumeListener();
    void OnPlaylistCreatedListener(ArrayList<MusicResolver> trackList);
    void OnPlaylistItemSelectedListener(int index);
    void OnPlaylistShuffle();
    void OnAddSongsListener(ArrayList<MusicResolver> songSelection, String title);
}
