package com.lohni.musicplayer.database.viewmodel

import androidx.lifecycle.*
import com.lohni.musicplayer.database.dao.MusicplayerDataAccess
import com.lohni.musicplayer.database.dao.PreferenceDataAccess
import com.lohni.musicplayer.database.dto.TrackDTO
import com.lohni.musicplayer.utils.enums.ListFilterType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SonglistViewModel(private val musicplayerDao: MusicplayerDataAccess, private val preferenceDao: PreferenceDataAccess) : ViewModel() {
    val filter: MutableLiveData<ListFilterType> = MutableLiveData(ListFilterType.ALPHABETICAL)

    val songList: LiveData<List<TrackDTO>> = liveData {
        filter.asFlow().collect {
            when (it) {
                ListFilterType.FAVOURITE -> emitSource(musicplayerDao.getFavouriteTracks().asLiveData())
                ListFilterType.TIMES_PLAYED -> emitSource(musicplayerDao.getTracksByTimesPlayed().asLiveData())
                ListFilterType.ALPHABETICAL -> emitSource(musicplayerDao.getTracksAlphabetical().asLiveData())
                ListFilterType.TIME_PLAYED -> emitSource(musicplayerDao.getTracksByTimePlayed().asLiveData())
                ListFilterType.LAST_CREATED -> emitSource(musicplayerDao.getAllTracksByCreated().asLiveData())
                else -> emitSource(musicplayerDao.getTracksByLastPlayed().asLiveData())
            }
        }
    }

    fun setListFilterType(filterType: ListFilterType) = viewModelScope.launch(Dispatchers.IO) {
        filter.postValue(filterType)
    }

    class SonglistViewModelFactory(private val musicplayerDao: MusicplayerDataAccess, private val preferenceDao: PreferenceDataAccess) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SonglistViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SonglistViewModel(musicplayerDao, preferenceDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}