package com.example.musicplayer.ui.playlist;

import android.view.View;

import com.example.musicplayer.database.entity.Playlist;

public interface PlaylistInterface {
    void OnClickListener(Playlist playlist, View view);
}
