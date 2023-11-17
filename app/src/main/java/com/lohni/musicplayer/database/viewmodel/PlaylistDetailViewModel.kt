package com.lohni.musicplayer.database.viewmodel

import androidx.lifecycle.*
import com.lohni.musicplayer.database.dao.PlaylistDataAccess
import com.lohni.musicplayer.database.dto.PlaylistItemDTO
import com.lohni.musicplayer.database.entity.Playlist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PlaylistDetailViewModel(private val dao: PlaylistDataAccess) : ExtendedBaseViewModel() {
    val playlist: MutableLiveData<Playlist> = MutableLiveData()

    val playlistItems: LiveData<List<PlaylistItemDTO>> = liveData {
        playlist.asFlow().collect {
            emitSource(dao.getPlaylistItemsWithTrackByPlaylistItemOrdinal(it.pId).asLiveData())
        }
    }

    fun setPlaylistById(playlistId: Int) = viewModelScope.launch(Dispatchers.IO) {
        playlist.postValue(dao.getPlaylistById(playlistId).first())
    }

    class PlaylistDetailViewModelFactory(private val dataAccess: PlaylistDataAccess) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PlaylistDetailViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PlaylistDetailViewModel(dataAccess) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}