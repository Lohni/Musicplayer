package com.lohni.musicplayer.database.viewmodel

import androidx.lifecycle.*
import com.lohni.musicplayer.database.dao.AudioEffectDataAccess
import com.lohni.musicplayer.database.entity.AdvancedReverbPreset
import com.lohni.musicplayer.database.entity.EqualizerPreset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AudioEffectViewModel(private val dao: AudioEffectDataAccess) : ViewModel() {

    val allEqualizerPresets : LiveData<List<EqualizerPreset>> = dao.getAllEqualizerPresets().asLiveData()

    val activeEqualizerPreset: LiveData<EqualizerPreset> = dao.getActiveEqualizerPreset().asLiveData()

    val allAdvancedReverbPresets: LiveData<List<AdvancedReverbPreset>> = dao.getAllAdvancedReverbPresets().asLiveData()

    val activeAdvancedReverbPreset: LiveData<AdvancedReverbPreset> = dao.getActiveAdvancedReverbPreset().asLiveData()

    fun updateEqualizerPreset(equalizerPreset: EqualizerPreset) = viewModelScope.launch(Dispatchers.IO) {
        dao.updateEqualizerPreset(equalizerPreset)
    }

    fun deleteEqualizerPreset(equalizerPreset: EqualizerPreset) = viewModelScope.launch(Dispatchers.IO) {
        dao.deleteEqualizerPreset(equalizerPreset)
    }

    fun insertEqualizerPreset(equlizerPreset: EqualizerPreset) = viewModelScope.launch(Dispatchers.IO) {
        dao.insertEqualizerPreset(equlizerPreset)
    }

    fun updateAdvancedReverbPreset(advancedReverbPreset: AdvancedReverbPreset) = viewModelScope.launch(Dispatchers.IO) {
        dao.updateAdvancedReverbPreset(advancedReverbPreset)
    }

    fun deleteAdvancedReverbPreset(advancedReverbPreset: AdvancedReverbPreset) = viewModelScope.launch(Dispatchers.IO) {
        dao.deleteAdvancedReverbPreset(advancedReverbPreset)
    }

    fun insertAdvancedReverbPreset(advancedReverbPreset: AdvancedReverbPreset) = viewModelScope.launch(Dispatchers.IO) {
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