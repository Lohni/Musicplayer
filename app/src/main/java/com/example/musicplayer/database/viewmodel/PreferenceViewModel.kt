package com.example.musicplayer.database.viewmodel

import androidx.lifecycle.*
import com.example.musicplayer.database.dao.PreferenceDataAccess
import com.example.musicplayer.database.entity.Preference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PreferenceViewModel(private val dao: PreferenceDataAccess) : ViewModel() {

    fun getAllPreferences(): LiveData<List<Preference>> {
        return dao.getAllPreferences().asLiveData()
    }

    fun getPreferenceByKey(key: String): LiveData<Preference> {
        return dao.getPreferenceByKey(key).asLiveData()
    }

    fun updatePreference(preference: Preference) = viewModelScope.launch(Dispatchers.IO) {
        dao.updatePreference(preference)
    }

    class PreferenceViewModelFactory(private val dataAccess: PreferenceDataAccess) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PreferenceViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PreferenceViewModel(dataAccess) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}