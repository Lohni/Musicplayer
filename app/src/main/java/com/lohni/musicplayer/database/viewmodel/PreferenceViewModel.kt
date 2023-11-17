package com.lohni.musicplayer.database.viewmodel

import androidx.lifecycle.*
import com.lohni.musicplayer.database.dao.PreferenceDataAccess
import com.lohni.musicplayer.database.entity.Preference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PreferenceViewModel(private val dao: PreferenceDataAccess) : ExtendedBaseViewModel() {

    fun getAllPreferences(): LiveData<List<Preference>> {
        return dao.getAllPreferences().asLiveData()
    }

    fun getPreferenceById(id: Int): LiveData<Preference> {
        return dao.getPreferenceById(id).asLiveData()
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