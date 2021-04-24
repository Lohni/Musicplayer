package com.example.musicplayer.ui.audioeffects.database;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface AudioEffectDAO {

    @Query("SELECT * FROM reverbsettings")
    LiveData<List<ReverbSettings>> getAllReverbPresets();

    @Query("SELECT * FROM reverbsettings WHERE isSelected IS 1")
    LiveData<ReverbSettings> getActiveReverbPreset();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    ListenableFuture<Long> insertReverbPreset(ReverbSettings reverbSettings);

    @Update
    ListenableFuture<Integer> updateReverbPreset(ReverbSettings reverb_settings);

    @Update
    ListenableFuture<Integer> updateReverbPresets(ReverbSettings reverb_settings1, ReverbSettings reverbSettings2);

    @Delete
    ListenableFuture<Integer> deleteReverbPreset(ReverbSettings reverbSettings);


    @Query("SELECT * FROM equalizersettings")
    LiveData<List<EqualizerSettings>> getAllEqualizerPresets();

    @Query("SELECT * FROM equalizersettings WHERE isSelected IS 1")
    LiveData<EqualizerSettings> getActiveEqualizerPreset();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    ListenableFuture<Long> insertEqualizerPreset(EqualizerSettings equalizerSettings);

    @Update
    ListenableFuture<Integer> updateEqualizerPreset(EqualizerSettings equalizerSettings);

    @Update
    ListenableFuture<Integer> updateEqualizerPresets(EqualizerSettings equalizerSettings1, EqualizerSettings equalizerSettings2);

    @Delete
    ListenableFuture<Integer> deleteEqualizerPreset(EqualizerSettings equalizerSettings);

}
