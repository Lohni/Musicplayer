package com.example.musicplayer.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.example.musicplayer.database.entity.Preference
import kotlinx.coroutines.flow.Flow

/**
 * @author Andreas Lohninger
 */
@Dao
interface PreferenceDataAccess {
    @Query("SELECT * FROM Preference WHERE pref_key = :key")
    fun getPreferenceByKey(key: String): Flow<Preference>

    @Query("SELECT * FROM Preference ORDER BY pref_id ASC")
    fun getAllPreferences(): Flow<List<Preference>>

    @Update
    fun updatePreference(preference: Preference)
}