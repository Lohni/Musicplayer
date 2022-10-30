package com.lohni.musicplayer.database.dao

import androidx.room.*
import com.lohni.musicplayer.database.entity.AdvancedReverbPreset
import com.lohni.musicplayer.database.entity.EqualizerPreset
import kotlinx.coroutines.flow.Flow

/**
 * @author Andreas Lohninger
 */
@Dao
interface AudioEffectDataAccess {

    @Transaction
    @Query("SELECT * FROM EqualizerPreset")
    fun getAllEqualizerPresets(): Flow<List<EqualizerPreset>>

    @Query("SELECT * FROM EqualizerPreset WHERE eq_active = CASE WHEN (SELECT count(*) FROM EqualizerPreset where eq_active = 1) > 0 then 1 else 2 end")
    fun getActiveEqualizerPreset(): Flow<EqualizerPreset>

    @Update
    fun updateEqualizerPreset(equalizerPreset: EqualizerPreset)

    @Update
    fun updateEqualizerPresets(equalizerPreset: List<EqualizerPreset>)

    @Delete
    fun deleteEqualizerPreset(equalizerPreset: EqualizerPreset)

    @Insert
    fun insertEqualizerPreset(equalizerPreset: EqualizerPreset)

    @Transaction
    @Query("SELECT * FROM AdvancedReverbPreset WHERE ar_active = 1")
    fun getActiveAdvancedReverbPreset(): Flow<AdvancedReverbPreset>

    @Transaction
    @Query("SELECT * FROM AdvancedReverbPreset")
    fun getAllAdvancedReverbPresets(): Flow<List<AdvancedReverbPreset>>

    @Update
    fun updateAdvancedReverbPreset(advancedReverbPreset: AdvancedReverbPreset)

    @Delete
    fun deleteAdvancedReverbPreset(advancedReverbPreset: AdvancedReverbPreset)

    @Insert
    fun insertAdvancedReverbPreset(advancedReverbPreset: AdvancedReverbPreset)
}