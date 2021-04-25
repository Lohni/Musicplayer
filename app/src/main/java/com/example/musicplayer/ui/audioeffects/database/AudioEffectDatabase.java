package com.example.musicplayer.ui.audioeffects.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {ReverbSettings.class, EqualizerSettings.class}, version = 1)
public abstract class AudioEffectDatabase extends RoomDatabase {
    public abstract AudioEffectDAO audioEffectDAO();

}
