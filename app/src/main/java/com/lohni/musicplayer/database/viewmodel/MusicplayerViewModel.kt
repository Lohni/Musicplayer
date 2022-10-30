package com.lohni.musicplayer.database.viewmodel

import androidx.lifecycle.*
import com.lohni.musicplayer.database.dao.MusicplayerDataAccess
import com.lohni.musicplayer.database.dto.StatisticDTO
import com.lohni.musicplayer.database.dto.TrackDTO
import com.lohni.musicplayer.database.entity.Album
import com.lohni.musicplayer.database.entity.Tag
import com.lohni.musicplayer.database.entity.Track
import com.lohni.musicplayer.database.entity.TrackPlayed
import com.lohni.musicplayer.utils.GeneralUtils
import com.lohni.musicplayer.utils.enums.ListFilterType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.streams.toList

class MusicplayerViewModel(private val dao: MusicplayerDataAccess) : ViewModel() {

    val allTracks: LiveData<List<Track>> = dao.getAllTracks().asLiveData()
    val allAlbums: LiveData<List<Album>> = dao.getAllAbums().asLiveData()
    val lastPlayedTracks: LiveData<List<TrackDTO>> = dao.getTracksByLastPlayed().asLiveData()
    val favouriteTracks: LiveData<List<TrackDTO>> = dao.getFavouriteTracks().asLiveData()
    val mostPlayedTracks: LiveData<List<TrackDTO>> = dao.getTracksByTimesPlayed().asLiveData()
    val tracksTimePlayed: LiveData<List<TrackDTO>> = dao.getTracksbyTimePlayed().asLiveData()
    val tracksCreated: LiveData<List<TrackDTO>> = dao.getAllTracksByCreated().asLiveData()
    val deletedTracks: LiveData<List<Track>> = dao.getDeletedTracks().asLiveData()
    val trackAlphabetical: LiveData<List<TrackDTO>> = dao.getTracksAlphabetical().asLiveData()

    val lastTrackPlayed: LiveData<TrackPlayed> = dao.getLastTrackPlayed().asLiveData()

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

    fun updateTrack(track: Track) = viewModelScope.launch(Dispatchers.IO) {
        dao.updateTrack(track)
    }

    fun insertTrackPlayed(trackPlayed: TrackPlayed) = viewModelScope.launch(Dispatchers.IO) {
        dao.insertTrackPlayed(trackPlayed)
    }

    fun getTrackListByFilter(filterType: ListFilterType): LiveData<List<TrackDTO>> {
        return when (filterType) {
            ListFilterType.FAVOURITE -> favouriteTracks
            ListFilterType.TIMES_PLAYED -> mostPlayedTracks
            ListFilterType.ALPHABETICAL -> trackAlphabetical
            ListFilterType.TIME_PLAYED -> tracksTimePlayed
            ListFilterType.LAST_CREATED -> tracksCreated
            else -> lastPlayedTracks
        }
    }

    fun getAllTrackPlayedInDaySteps(): LiveData<List<StatisticDTO>> {
        return dao.getAllTrackPlayedInDaySteps(GeneralUtils.getTimestampWeekBefor()).asLiveData()
    }

    fun updateTrackPlayed(trackPlayed: TrackPlayed) = viewModelScope.launch(Dispatchers.IO) {
        dao.updateTrackPlayed(trackPlayed)
    }

    fun getTimesPlayedByTrackId(id: Int): LiveData<String> {
        return dao.getTimesPlayed(id).asLiveData()
    }

    fun getTimePlayedByTrackId(id: Int): LiveData<String> {
        return dao.getTimePlayed(id).asLiveData()
    }

    fun getLastPlayedByTrackId(id: Int): LiveData<String> {
        return dao.getLastPlayed(id).asLiveData()
    }

    fun getTagsByTrackId(trackId: Int): LiveData<List<Tag>> {
        return dao.getTagByTrackId(trackId).asLiveData()
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