package com.lohni.musicplayer.database.dao

import androidx.room.*
import com.lohni.musicplayer.database.entity.DashboardListConfiguration
import com.lohni.musicplayer.database.entity.Preference
import kotlinx.coroutines.flow.Flow

/**
 * @author Andreas Lohninger
 */
@Dao
interface PreferenceDataAccess {

    @Query("SELECT * FROM Preference WHERE pref_id = :id")
    fun getPreferenceById(id: Int): Flow<Preference>

    @Query("SELECT * FROM Preference ORDER BY pref_id ASC")
    fun getAllPreferences(): Flow<List<Preference>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun updatePreference(preference: Preference)

    @Query("SELECT * FROM DashboardListConfiguration WHERE dlc_id = :id")
    fun getDashboardListConfiguration(id: Int): Flow<DashboardListConfiguration>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDashboardListConfiguration(listConfig: DashboardListConfiguration)
}