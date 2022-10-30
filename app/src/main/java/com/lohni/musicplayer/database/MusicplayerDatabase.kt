package com.lohni.musicplayer.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lohni.musicplayer.database.dao.AudioEffectDataAccess
import com.lohni.musicplayer.database.dao.MusicplayerDataAccess
import com.lohni.musicplayer.database.dao.PlaylistDataAccess
import com.lohni.musicplayer.database.dao.PreferenceDataAccess
import com.lohni.musicplayer.database.entity.*
import kotlinx.coroutines.CoroutineScope

/**
 * @author Andreas Lohninger
 */
@Database(
    entities = [AdvancedReverbPreset::class, EqualizerPreset::class, Playlist::class, PlaylistItem::class,
        PlaylistTagMtc::class, Tag::class, Track::class, TrackTagMtc::class, Album::class,
        AlbumPlayed::class, PlaylistPlayed::class, TrackPlayed::class, Preference::class],
    version = 1
)
abstract class MusicplayerDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDataAccess
    abstract fun musicplayerDao(): MusicplayerDataAccess
    abstract fun audioEffectDao(): AudioEffectDataAccess
    abstract fun preferenceDao(): PreferenceDataAccess

    private class MusicplayerDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
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
                    "musicplayer.db"
                )
                    .createFromAsset("database/musicplayer.db")
                    .addCallback(MusicplayerDatabaseCallback(scope))
                    .build()

                INSTANCE = instance
                return instance
            }
        }
    }
}