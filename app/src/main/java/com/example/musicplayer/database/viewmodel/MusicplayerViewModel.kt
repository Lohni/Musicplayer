package com.example.musicplayer.database.viewmodel

import androidx.lifecycle.*
import com.example.musicplayer.database.dao.MusicplayerDataAccess
import com.example.musicplayer.database.dao.PlaylistDataAccess
import com.example.musicplayer.database.entity.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MusicplayerViewModel(private val dao: MusicplayerDataAccess) : ViewModel() {

    val allTracks : LiveData<List<Track>> = dao.getAllTracks().asLiveData()

    fun getTracksByIds(trackIds: List<Int>) : LiveData<List<Track>> {
        return dao.getTracksByIds(trackIds).asLiveData()
    }

    fun insertTracks(trackList: List<Track>) = viewModelScope.launch(Dispatchers.IO) {
        dao.insertTracks(trackList)
    }

    class MusicplayerViewModelFactory(private val dataAccess: MusicplayerDataAccess) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MusicplayerViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MusicplayerViewModel(dataAccess) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}