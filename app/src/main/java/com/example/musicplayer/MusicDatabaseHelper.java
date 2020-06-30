package com.example.musicplayer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class MusicDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "music_player";
    private static final int DATABASE_VERSION = 1;

    //Table Names
    private static final  String TABLE_PLAYLIST="PLAYLIST";

    //Column Playlist
    private static final String PLAYLIST_COL1 = "ID";
    private static final String PLAYLIST_COL2 = "TITLE";
    private static final String PLAYLIST_COL3 = "ARTIST";
    private static final String PLAYLIST_COL4 = "ALBUM_ID";

    public MusicDatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
