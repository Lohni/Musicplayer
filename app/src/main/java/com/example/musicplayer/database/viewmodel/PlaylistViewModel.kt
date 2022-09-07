package com.example.musicplayer.database.viewmodel

import androidx.lifecycle.*
import com.example.musicplayer.database.dao.PlaylistDataAccess
import com.example.musicplayer.database.entity.Playlist
import com.example.musicplayer.database.entity.PlaylistItem
import com.example.musicplayer.database.entity.PlaylistPlayed
import com.example.musicplayer.database.dto.PlaylistDTO
import com.example.musicplayer.utils.enums.ListFilterType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlaylistViewModel(private val dao: PlaylistDataAccess) : ViewModel() {

    val allPlaylists: LiveData<List<Playlist>> = dao.getAllPlaylists().asLiveData()

    val allPlaylistsWithSize: LiveData<List<PlaylistDTO>> =
        dao.getAllPlaylistsWithSize().asLiveData()

    fun getPlaylistById(playlistId: Int): LiveData<Playlist> {
        return dao.getPlaylistById(playlistId).asLiveData()
    }

    fun getPlaylistItemsByPlaylistId(playlistId: Int): LiveData<List<PlaylistItem>> {
        return dao.getAllPlaylistItemsForPlaylist(playlistId).asLiveData()
    }

    fun insertPlaylist(playlist: Playlist) = viewModelScope.launch(Dispatchers.IO) {
        dao.insertPlaylist(playlist)
    }

    fun deletePlaylist(playlist: Playlist) = viewModelScope.launch(Dispatchers.IO) {
        dao.deletePlaylist(playlist)
    }

    fun renamePlaylist(playlist: Playlist) = viewModelScope.launch(Dispatchers.IO) {
        dao.renamePlaylist(playlist)
    }

    fun deletePlaylistItem(playlistItem: PlaylistItem) = viewModelScope.launch(Dispatchers.IO) {
        dao.deletePlaylistItem(playlistItem)
    }

    fun updatePlaylistItemList(playlistItemList: List<PlaylistItem>) =
        viewModelScope.launch(Dispatchers.IO) {
            dao.updatePlaylistItemList(playlistItemList)
        }

    fun insertPlaylistItems(playlistItemList: List<PlaylistItem>) =
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertPlaylistItems(playlistItemList)
        }

    fun insertPlaylistPlayed(playlistPlayed: PlaylistPlayed) = viewModelScope.launch(Dispatchers.IO) {
        dao.insertPlaylistPlayed(playlistPlayed)
    }

    fun getPlaylistByFilter(filterType: ListFilterType): LiveData<List<PlaylistDTO>> {
        return when (filterType) {
            ListFilterType.FAVOURITE -> dao.getFavouritePlaylists().asLiveData()
            ListFilterType.TIMES_PLAYED -> dao.getPlaylistsByTimesPlayed().asLiveData()
            else -> dao.getPlaylistsByLastPlayed().asLiveData()
        }
    }

    fun getLastPlaylistPlayed(): LiveData<PlaylistPlayed> {
        return dao.getLastPlaylistPlayed().asLiveData()
    }

    fun updatePlaylistPlayed(playlistPlayed: PlaylistPlayed) = viewModelScope.launch(Dispatchers.IO) {
        dao.updatePlaylistPlayed(playlistPlayed)
    }

    fun getPlaylistsByTrackId(trackId: Int): LiveData<List<Playlist>> {
        return dao.getPlaylistsByTrackId(trackId).asLiveData()
    }

    class PlaylistViewModelFactory(private val dataAccess: PlaylistDataAccess) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PlaylistViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PlaylistViewModel(dataAccess) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}