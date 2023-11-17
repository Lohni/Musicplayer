package com.lohni.musicplayer.ui.dashboard

import androidx.lifecycle.*
import com.lohni.musicplayer.database.dao.PreferenceDataAccess
import com.lohni.musicplayer.database.entity.DashboardListConfiguration
import com.lohni.musicplayer.utils.enums.ListFilterType
import com.lohni.musicplayer.utils.enums.ListType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DashboardViewModel(private val preferenceDao: PreferenceDataAccess) : ViewModel() {
    private var _firstListConfiguration = DashboardListConfiguration(0, ListType.ALBUM, ListFilterType.LAST_CREATED, 5)
    private var _secondListConfiguration = DashboardListConfiguration(1, ListType.TRACK, ListFilterType.LAST_CREATED, 5)

    val firstListConfiguration: LiveData<DashboardListConfiguration> = liveData {
        emit(_firstListConfiguration)
        preferenceDao.getDashboardListConfiguration(0)
            .filterNotNull()
            .collect { _firstListConfiguration = it; emit(_firstListConfiguration) }
    }

    val secondListConfiguration: LiveData<DashboardListConfiguration> = liveData {
        emit(_secondListConfiguration)
        preferenceDao.getDashboardListConfiguration(1)
            .filterNotNull()
            .collect { _secondListConfiguration = it; emit(_secondListConfiguration) }
    }


    fun updateListConfiguration(listConfiguration: DashboardListConfiguration) = viewModelScope.launch(Dispatchers.IO) {
        preferenceDao.insertDashboardListConfiguration(listConfiguration)
    }

    class DashboardViewModelFactory(private val preferenceDao: PreferenceDataAccess) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DashboardViewModel(preferenceDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}