package com.example.musicplayer.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.musicplayer.database.dao.AudioEffectDataAccess
import com.example.musicplayer.database.dao.MusicplayerDataAccess
import com.example.musicplayer.database.dao.PlaylistDataAccess
import com.example.musicplayer.database.entity.*
import kotlinx.coroutines.CoroutineScope

/**
 * @author Andreas Lohninger
 */
@Database(entities = [AdvancedReverbPreset::class, EqualizerPreset::class, Playlist::class,
    PlaylistItem::class, PlaylistTagMtc::class, Tag::class, Track::class, TrackTagMtc::class], version = 1)
abstract class MusicplayerDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDataAccess
    abstract fun musicplayerDao(): MusicplayerDataAccess
    abstract fun audioEffectDao(): AudioEffectDataAccess

    private class MusicplayerDatabaseCallback(
        private val scope: CoroutineScope
    )   : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: MusicplayerDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): MusicplayerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MusicplayerDatabase::class.java,
                    "musicplayer1.db")
                    .createFromAsset("database/musicplayer1.db")
                    .addCallback(MusicplayerDatabaseCallback(scope))
                    .build()

                INSTANCE = instance
                return instance
            }
        }
    }
}