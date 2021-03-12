package com.example.musicplayer.ui.songlist;

import com.example.musicplayer.entities.MusicResolver;

import java.util.ArrayList;

public interface SongListInterface {
    void OnSongListCreatedListener(ArrayList<MusicResolver> songList);
    void OnSongSelectedListener(int index);
    void OnSonglistShuffleClickListener();
}
