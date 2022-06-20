package com.example.musicplayer.database.viewmodel

import androidx.lifecycle.*
import com.example.musicplayer.database.dao.AudioEffectDataAccess
import com.example.musicplayer.database.dao.PlaylistDataAccess
import com.example.musicplayer.database.entity.AdvancedReverbPreset
import com.example.musicplayer.database.entity.EqualizerPreset
import kotlinx.coroutines.launch

class AudioEffectViewModel(private val dao: AudioEffectDataAccess) : ViewModel() {

    val allEqualizerPresets : LiveData<List<EqualizerPreset>> = dao.getAllEqualizerPresets().asLiveData()

    val activeEqualizerPreset: LiveData<EqualizerPreset> = dao.getActiveEqualizerPreset().asLiveData()

    val allAdvancedReverbPresets: LiveData<List<AdvancedReverbPreset>> = dao.getAllAdvancedReverbPresets().asLiveData()

    val activeAdvancedReverbPreset: LiveData<AdvancedReverbPreset> = dao.getActiveAdvancedReverbPreset().asLiveData()

    fun updateEqualizerPreset(equalizerPreset: EqualizerPreset) = viewModelScope.launch {
        dao.updateEqualizerPreset(equalizerPreset)
    }

    fun deleteEqualizerPreset(equalizerPreset: EqualizerPreset) = viewModelScope.launch {
        dao.deleteEqualizerPreset(equalizerPreset)
    }

    fun insertEqualizerPreset(equlizerPreset: EqualizerPreset) = viewModelScope.launch {
        dao.insertEqualizerPreset(equlizerPreset)
    }

    fun updateAdvancedReverbPreset(advancedReverbPreset: AdvancedReverbPreset) = viewModelScope.launch {
        dao.updateAdvancedReverbPreset(advancedReverbPreset)
    }

    fun deleteAdvancedReverbPreset(advancedReverbPreset: AdvancedReverbPreset) = viewModelScope.launch {
        dao.deleteAdvancedReverbPreset(advancedReverbPreset)
    }

    fun insertAdvancedReverbPreset(advancedReverbPreset: AdvancedReverbPreset) = viewModelScope.launch {
        dao.insertAdvancedReverbPreset(advancedReverbPreset)
    }

    class AudioEffectViewModelFactory(private val dataAccess: AudioEffectDataAccess) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AudioEffectViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AudioEffectViewModel(dataAccess) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}