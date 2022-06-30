package com.example.musicplayer.database.viewmodel

import androidx.lifecycle.*
import com.example.musicplayer.database.dao.MusicplayerDataAccess
import com.example.musicplayer.database.entity.Album
import com.example.musicplayer.database.entity.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.streams.toList

class MusicplayerViewModel(private val dao: MusicplayerDataAccess) : ViewModel() {

    val allTracks: LiveData<List<Track>> = dao.getAllTracks().asLiveData()

    val allAlbums: LiveData<List<Album>> = dao.getAllAbums().asLiveData()

    fun getTracksByIdsOrderByPlaylistItemOrdinal(playlistId: Int): LiveData<List<Track>> {
        return dao.getTracksByIdsOrderByPlaylistItemOrdinal(playlistId).asLiveData()
    }

    fun getTrackById(trackId: Int): LiveData<Track> {
        return dao.getTrackById(trackId).asLiveData()
    }

    fun insertTracks(trackList: List<Track>) = viewModelScope.launch(Dispatchers.IO) {
        dao.insertTracks(trackList)
    }

    fun insertAlbums(albumList: List<Album>) = viewModelScope.launch(Dispatchers.IO) {
        dao.insertAlbums(albumList)
    }

    fun getTracksByAlbumId(albumId: Int): LiveData<List<Track>> {
        return dao.getTracksByAlbumId(albumId).asLiveData()
    }

    fun deleteTracks(trackList: List<Track>) = viewModelScope.launch(Dispatchers.IO) {
        val trackIds: List<Int> = trackList.stream().map(Track::getTId).toList()
        dao.deleteTracks(trackList)
        dao.deletePlaylistItemsByTrackIds(trackIds)
        dao.deleteTrackTagMtcByTrackIds(trackIds)
    }

    fun getAlbumByAlbumId(albumId: Int): LiveData<Album> {
        return dao.getAlbumByAlbumId(albumId).asLiveData()
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