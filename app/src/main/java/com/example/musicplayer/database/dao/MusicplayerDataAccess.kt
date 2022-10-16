package com.example.musicplayer.database.dao

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.example.musicplayer.database.dto.StatisticDTO
import com.example.musicplayer.database.dto.TrackDTO
import com.example.musicplayer.database.entity.Album
import com.example.musicplayer.database.entity.Tag
import com.example.musicplayer.database.entity.Track
import com.example.musicplayer.database.entity.TrackPlayed
import kotlinx.coroutines.flow.Flow


/**
 * @author Andreas Lohninger
 */
@Dao
interface MusicplayerDataAccess {

    @Query("SELECT t.*, null FROM Track as t JOIN Preference as p1 JOIN Preference as p2 " +
            "WHERE p1.pref_key = 'INCLUDE_DURATION_FROM' and p2.pref_key = 'INCLUDE_DURATION_TO' " +
            "and t_duration BETWEEN p1.pref_value AND p2.pref_value ORDER BY t_title ASC")
    fun getAllTracks(): Flow<List<TrackDTO>>

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
    @Query("SELECT t.*, tp_played as size FROM Track t " +
            "JOIN TrackPlayed tp on tp_t_id = t.t_id " +
            "JOIN Preference as p1 JOIN Preference p2 " +
            "WHERE p1.pref_key = 'INCLUDE_DURATION_FROM' and p2.pref_key = 'INCLUDE_DURATION_TO' " +
            "and t_duration BETWEEN p1.pref_value AND p2.pref_value " +
            "GROUP BY t.t_id ORDER BY max(datetime(tp_played)) DESC")
    fun getTracksByLastPlayed(): Flow<List<TrackDTO>>

    @Transaction
    @Query("SELECT t.*, count(t.t_id) as size FROM Track t JOIN TrackPlayed on tp_t_id = t.t_id " +
            "JOIN Preference as p1 JOIN Preference p2 " +
            "WHERE p1.pref_key = 'INCLUDE_DURATION_FROM' and p2.pref_key = 'INCLUDE_DURATION_TO' " +
            "and t_duration BETWEEN p1.pref_value AND p2.pref_value " +
            "GROUP BY t.t_id ORDER BY count(t.t_id) DESC")
    fun getTracksByTimesPlayed(): Flow<List<TrackDTO>>

    @Transaction
    @Query("SELECT t.*, sum(tp_time_played) as size FROM TrackPlayed JOIN Track as t on tp_t_id = t.t_id " +
            "JOIN Preference as p1 JOIN Preference p2 " +
            "WHERE p1.pref_key = 'INCLUDE_DURATION_FROM' and p2.pref_key = 'INCLUDE_DURATION_TO' " +
            "and t_duration BETWEEN p1.pref_value AND p2.pref_value " +
            "GROUP BY t_id ORDER BY sum(tp_time_played) DESC")
    fun getTracksbyTimePlayed(): Flow<List<TrackDTO>>

    @Transaction
    @Query("SELECT *, null FROM Track WHERE t_isFavourite = 1")
    fun getFavouriteTracks(): Flow<List<TrackDTO>>

    @Query("SELECT * FROM TrackPlayed ORDER BY tp_played DESC LIMIT 1")
    fun getLastTrackPlayed(): Flow<TrackPlayed>

    @Update
    fun updateTrackPlayed(trackPlayed: TrackPlayed)

    @Query("SELECT tp_played as time, sum(tp_time_played) as total_time, count(tp_t_id) as amount FROM TrackPlayed WHERE tp_played > :date GROUP BY date(tp_played)")
    fun getAllTrackPlayedInDaySteps(date: String): Flow<List<StatisticDTO>>

    @Query("SELECT *, null FROM Track " +
            "JOIN Preference as p1 JOIN Preference p2 " +
            "WHERE p1.pref_key = 'INCLUDE_DURATION_FROM' and p2.pref_key = 'INCLUDE_DURATION_TO' " +
            "and t_duration BETWEEN p1.pref_value AND p2.pref_value " +
            "ORDER BY t_created DESC")
    fun getAllTracksByCreated(): Flow<List<TrackDTO>>

    @Transaction
    @Query("SELECT count(tp_t_id) FROM TrackPlayed WHERE tp_t_id = :id")
    fun getTimesPlayed(id: Int): Flow<String>

    @Transaction
    @Query("SELECT sum(tp_time_played) FROM TrackPlayed WHERE tp_t_id = :id")
    fun getTimePlayed(id: Int): Flow<String>

    @Transaction
    @Query("SELECT tp_played FROM TrackPlayed WHERE tp_t_id = :id ORDER BY tp_played DESC")
    fun getLastPlayed(id: Int): Flow<String>

    @Transaction
    @Query("SELECT t.* FROM Tag t JOIN TrackTagMtc ON t.tag_id = ttm_tag_id WHERE ttm_t_id = :id")
    fun getTagByTrackId(id: Int): Flow<List<Tag>>
}