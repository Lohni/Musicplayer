package com.example.musicplayer.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.musicplayer.database.entity.PlaylistItem
import com.example.musicplayer.database.entity.Track
import kotlinx.coroutines.flow.Flow


/**
 * @author Andreas Lohninger
 */
@Dao
interface MusicplayerDataAccess {

    @Query("SELECT * FROM Track ORDER BY t_title ASC")
    fun getAllTracks(): Flow<List<Track>>

    @Query("SELECT t.* FROM PlaylistItem JOIN TRACK as t on pi_t_id = t_id WHERE pi_p_id = :playlistId ORDER BY pi_custom_ordinal ASC")
    fun getTracksByIdsOrderByPlaylistItemOrdinal(playlistId: Int): Flow<List<Track>>

    @Insert
    fun insertTracks(trackList: List<Track>)
}