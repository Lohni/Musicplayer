package com.example.musicplayer.database.dao

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.example.musicplayer.database.dto.TrackDTO
import com.example.musicplayer.database.entity.Album
import com.example.musicplayer.database.entity.Track
import com.example.musicplayer.database.entity.TrackPlayed
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

    @Insert(onConflict = REPLACE)
    fun insertTracks(trackList: List<Track>)

    @Transaction
    @Query("SELECT * FROM Track WHERE t_id = :trackId")
    fun getTrackById(trackId: Int): Flow<Track>

    @Transaction
    @Query("SELECT * FROM Album ORDER BY a_name ASC")
    fun getAllAbums(): Flow<List<Album>>

    @Insert(onConflict = REPLACE)
    fun insertAlbums(albumList: List<Album>)

    @Transaction
    @Query("SELECT * FROM Track where t_album_id = :albumId")
    fun getTracksByAlbumId(albumId: Int): Flow<List<Track>>

    @Delete
    fun deleteTracks(trackList: List<Track>)

    @Query("DELETE FROM PlaylistItem where pi_t_id in (:trackIdList)")
    fun deletePlaylistItemsByTrackIds(trackIdList: List<Int>)

    @Query("DELETE FROM TrackTagMtc where ttm_t_id in (:trackIdList)")
    fun deleteTrackTagMtcByTrackIds(trackIdList: List<Int>)

    @Query("SELECT * FROM Album WHERE a_id = :albumId")
    fun getAlbumByAlbumId(albumId: Int): Flow<Album>

    @Update
    fun updateTrack(track: Track)

    @Insert
    fun insertTrackPlayed(trackPlayed: TrackPlayed)

    @Transaction
    @Query("SELECT t.*, null FROM Track t JOIN TrackPlayed tp on tp_t_id = t.t_id GROUP BY t.t_id ORDER BY max(datetime(tp_played)) DESC")
    fun getTracksByLastPlayed(): Flow<List<TrackDTO>>

    @Transaction
    @Query("SELECT t.*, count(t.t_id) FROM Track t JOIN TrackPlayed on tp_t_id = t.t_id GROUP BY t.t_id ORDER BY count(t.t_id) DESC")
    fun getTracksByTimesPlayed(): Flow<List<TrackDTO>>

    @Transaction
    @Query("SELECT *, null FROM Track WHERE t_isFavourite = 1")
    fun getFavouriteTracks(): Flow<List<TrackDTO>>
}