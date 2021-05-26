package com.example.musicplayer.utils;

import android.content.Context;

import com.example.musicplayer.R;

import java.util.ArrayList;
import java.util.Arrays;

public class AlbumOptions {
    public static final int OPTION_PLAY = 0, OPTION_SHUFFLE = 1, OPTION_QUEUE = 2;

    public static ArrayList<String> getAlbumOptions(Context context){
        String[] stringArray = context.getResources().getStringArray(R.array.AlbumOption);
        return new ArrayList<>(Arrays.asList(stringArray));
    }
}
