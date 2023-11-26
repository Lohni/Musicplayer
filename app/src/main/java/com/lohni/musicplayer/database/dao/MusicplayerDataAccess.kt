package com.lohni.musicplayer.database.dao

import androidx.room.*
import androidx.room.OnConflictStrategy.Companion.REPLACE
import com.lohni.musicplayer.database.dto.*
import com.lohni.musicplayer.database.entity.*
import kotlinx.coroutines.flow.Flow


/**
 * @author Andreas Lohninger
 */
@Dao
interface MusicplayerDataAccess {
    @Query("SELECT t.* FROM Track t")
    fun getAllTracks(): Flow<List<Track>>

    @Query("SELECT t.*, null FROM Track as t JOIN Preference as p1 JOIN Preference as p2 " +
            "WHERE t.t_deleted = 0 AND p1.pref_id = 1 and p2.pref_id = 2 " +
            "and t_duration BETWEEN p1.pref_value AND p2.pref_value ORDER BY t_title ASC")
    fun getTracksAlphabetical(): Flow<List<TrackDTO>>

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

    @Transaction
    @Query("SELECT * FROM Album ORDER BY a_name ASC")
    fun getAlbumsWithTracks(): Flow<List<AlbumTrackDTO>>

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
            "WHERE t.t_deleted = 0 AND p1.pref_id = 1 and p2.pref_id = 2 " +
            "and t_duration BETWEEN p1.pref_value AND p2.pref_value " +
            "GROUP BY t.t_id ORDER BY max(datetime(tp_played)) DESC")
    fun getTracksByLastPlayed(): Flow<List<TrackDTO>>

    @Transaction
    @Query("SELECT t.*, count(t.t_id) as size FROM Track t JOIN TrackPlayed on tp_t_id = t.t_id " +
            "JOIN Preference as p1 JOIN Preference p2 " +
            "WHERE t.t_deleted = 0 AND p1.pref_id = 1 and p2.pref_id = 2 " +
            "and t_duration BETWEEN p1.pref_value AND p2.pref_value " +
            "GROUP BY t.t_id ORDER BY count(t.t_id) DESC")
    fun getTracksByTimesPlayed(): Flow<List<TrackDTO>>

    @Transaction
    @Query("SELECT t.*, sum(tp_time_played) as size FROM TrackPlayed JOIN Track as t on tp_t_id = t.t_id " +
            "JOIN Preference as p1 JOIN Preference p2 " +
            "WHERE t.t_deleted = 0 AND p1.pref_id = 1 and p2.pref_id = 2 " +
            "and t_duration BETWEEN p1.pref_value AND p2.pref_value " +
            "GROUP BY t_id ORDER BY sum(tp_time_played) DESC")
    fun getTracksByTimePlayed(): Flow<List<TrackDTO>>

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
            "WHERE t_deleted = 0 AND p1.pref_id = 1 and p2.pref_id = 2 " +
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

    @Query("SELECT t.* FROM Track t WHERE t.t_deleted = 1 ORDER BY t.t_title ASC")
    fun getDeletedTracks(): Flow<List<Track>>

    @Insert
    fun insertAlbumPlayed(albumPlayed: AlbumPlayed)

    @Query("SELECT * FROM AlbumPlayed ORDER BY ap_played DESC LIMIT 1")
    fun getLastAlbumPlayed(): Flow<AlbumPlayed>

    @Insert
    fun insertPlaylistPlayed(playlistPlayed: PlaylistPlayed)

    @Query("SELECT * FROM PlaylistPlayed ORDER BY pp_played DESC LIMIT 1")
    fun getLastPlaylistPlayed(): Flow<PlaylistPlayed>

    @Insert
    fun insertAlbumTrackPlayed(albumTrackPlayed: AlbumTrackPlayed)

    @Insert
    fun insertPlaylistTrackPlayed(playlistPlayed: PlaylistTrackPlayed)

    @Transaction
    @Query("SELECT * FROM Album WHERE a_is_favourite = 1")
    fun getFavouriteAlbums(): Flow<List<AlbumDTO>>

    @Transaction
    @Query("SELECT a.*, ap_played as size FROM Album a " +
            "JOIN AlbumPlayed ap on ap_a_id = a.a_id " +
            "GROUP BY a.a_id ORDER BY max(datetime(ap_played)) DESC")
    fun getAlbumsByLastPlayed(): Flow<List<AlbumDTO>>

    @Transaction
    @Query("SELECT a.*, count(a.a_id) as size FROM Album a " +
            "JOIN AlbumPlayed on ap_a_id = a.a_id " +
            "GROUP BY a.a_id ORDER BY count(a.a_id) DESC")
    fun getAlbumsByTimesPlayed(): Flow<List<AlbumDTO>>

    @Transaction
    @Query("SELECT a.*, sum(tp_time_played) as size FROM Album a " +
            "JOIN AlbumPlayed ON ap_a_id = a_id " +
            "JOIN AlbumTrackPlayed ON ap_id = atp_ap_id " +
            "JOIN TrackPlayed ON tp_id = atp_tp_id " +
            "GROUP BY ap_a_id " +
            "ORDER BY sum(tp_time_played) DESC")
    fun getAlbumsByTimePlayed(): Flow<List<AlbumDTO>>

    @Transaction
    @Query("SELECT *, null as size FROM Album a " +
            "ORDER BY a.a_created DESC")
    fun getAlbumsByLastCreated(): Flow<List<AlbumDTO>>

    @Transaction
    @Query("SELECT p.*, sum(tp_time_played) as size FROM Playlist p " +
            "JOIN PlaylistPlayed ON pp_p_id = p_id " +
            "JOIN PlaylistTrackPlayed ON pp_id = ptp_pp_id " +
            "JOIN TrackPlayed ON tp_id = ptp_tp_id " +
            "GROUP BY pp_p_id " +
            "ORDER BY sum(tp_time_played) DESC")
    fun getPlaylistsByTimePlayed(): Flow<List<PlaylistDTO>>


     @Transaction
     @Query("select tp_id as id, t_title as title, t_artist as subtitle, tp_time_played as timeplayed, tp_played as credat, 0 as type, " +
             " case when atp_ap_id is not null then atp_ap_id when ptp_pp_id is not null then ptp_pp_id else null end as refid, " +
             " case when atp_ap_id is not null then 2 when ptp_pp_id is not null then 1 else null end as reftype " +
             " from TrackPlayed " +
             " join Track on tp_t_id = t_id " +
             " left join AlbumTrackPlayed on atp_tp_id = tp_id " +
             " left join PlaylistTrackPlayed on ptp_tp_id = tp_id " +
             "UNION ALL " +
             "select ap_id as id, a_name as title, a_artist_name as subtitle, null as timeplayed, ap_played as credat, 2 as type, null as refid, null as reftype " +
             " from AlbumPlayed " +
             " join Album on ap_a_id = a_id " +
             "UNION ALL " +
             " select pp_id as id, p_name as title, \"\" as subtitle, null as timeplayed, pp_played as credat, 1 as type, null as refid, null as reftype " +
             " from PlaylistPlayed " +
             " join Playlist on pp_p_id = p_id " +
             "ORDER BY credat desc")
     fun getItemPlayedInOrder(): Flow<List<ItemPlayedDTO>>
}