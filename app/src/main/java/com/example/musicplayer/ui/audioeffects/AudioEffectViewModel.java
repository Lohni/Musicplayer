package com.example.musicplayer.ui.audioeffects;

import android.app.Application;
import android.util.Log;

import com.example.musicplayer.ui.audioeffects.database.AudioEffectDatabase;
import com.example.musicplayer.ui.audioeffects.database.EqualizerSettings;
import com.example.musicplayer.ui.audioeffects.database.ReverbSettings;

import java.util.List;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Room;

public class AudioEffectViewModel extends AndroidViewModel {

    private AudioEffectDatabase db;
    private LiveData<List<ReverbSettings>> reverbPresetList;
    private LiveData<List<EqualizerSettings>> equalizerPresetList;

    public AudioEffectViewModel(@NonNull Application application) {
        super(application);
        db = Room.databaseBuilder(application.getApplicationContext(), AudioEffectDatabase.class, "audioEffectDatabase.db").createFromAsset("database/audioEffectPresets.db").build();
        //Todo: remove log database path
        //Log.e("DatabasePath", application.getDatabasePath("audioEffectDatabase.db").getAbsolutePath());
    }

    /*
    Reverb Methods
     */

    public LiveData<List<ReverbSettings>> getAllReverbPresets(){
        if (reverbPresetList==null){
            reverbPresetList = new MutableLiveData<>();
        }
        getAllReverbPresetsFromDb();
        return reverbPresetList;
    }

    public void updateReverbPreset(ReverbSettings reverbSettings){
       db.audioEffectDAO().updateReverbPreset(reverbSettings);
    }

    public void updateReverbPresets(ReverbSettings reverbSettings1, ReverbSettings reverbSettings2){
        db.audioEffectDAO().updateReverbPresets(reverbSettings1, reverbSettings2);
    }

    public void createNewReverbPreset(ReverbSettings reverbSettings){
        db.audioEffectDAO().insertReverbPreset(reverbSettings).addListener(new Runnable() {
            @Override
            public void run() {
                getAllReverbPresetsFromDb();
            }
        }, Executors.newSingleThreadExecutor());
    }

    public LiveData<ReverbSettings> getCurrentActivePreset(){
        return db.audioEffectDAO().getActiveReverbPreset();
    }

    public void deletePreset(ReverbSettings reverbSettings){
        db.audioEffectDAO().deleteReverbPreset(reverbSettings).addListener(new Runnable() {
            @Override
            public void run() {
                getAllReverbPresetsFromDb();
            }
        }, Executors.newSingleThreadExecutor());
    }

    private void getAllReverbPresetsFromDb(){
        reverbPresetList = db.audioEffectDAO().getAllReverbPresets();
    }

    /*
    Equalizer Methods
     */

    public LiveData<List<EqualizerSettings>> getAllEqualizerPresets(){
        if (equalizerPresetList==null){
            equalizerPresetList = new MutableLiveData<>();
        }
        getAllEqualizerPresetsFromDb();
        return equalizerPresetList;
    }

    public void updateEqualizerSetting(EqualizerSettings equalizerSettings){
        db.audioEffectDAO().updateEqualizerPreset(equalizerSettings);
    }

    public void updateEqualizerSettings(EqualizerSettings equalizerSettings1, EqualizerSettings equalizerSettings2){
        db.audioEffectDAO().updateEqualizerPresets(equalizerSettings1, equalizerSettings2);
    }

    public void createNewEqualizerPreset(EqualizerSettings equalizerSettings){
        db.audioEffectDAO().insertEqualizerPreset(equalizerSettings).addListener(new Runnable() {
            @Override
            public void run() {
                getAllEqualizerPresetsFromDb();
            }
        }, Executors.newSingleThreadExecutor());
    }

    public LiveData<EqualizerSettings> getCurrentActiveEqualizerPreset(){
        return db.audioEffectDAO().getActiveEqualizerPreset();
    }

    public void deleteEqualizerPreset(EqualizerSettings equalizerSettings){
        db.audioEffectDAO().deleteEqualizerPreset(equalizerSettings).addListener(new Runnable() {
            @Override
            public void run() {
                getAllEqualizerPresetsFromDb();
            }
        }, Executors.newSingleThreadExecutor());
    }

    private void getAllEqualizerPresetsFromDb(){
        equalizerPresetList = db.audioEffectDAO().getAllEqualizerPresets();
    }

}
