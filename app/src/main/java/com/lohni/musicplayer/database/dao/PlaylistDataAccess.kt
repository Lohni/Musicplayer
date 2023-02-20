package com.lohni.musicplayer.database.dao

import androidx.room.*
import com.lohni.musicplayer.database.dto.PlaylistDTO
import com.lohni.musicplayer.database.entity.Playlist
import com.lohni.musicplayer.database.entity.PlaylistItem
import com.lohni.musicplayer.database.entity.PlaylistPlayed
import kotlinx.coroutines.flow.Flow

/**
 * @author Andreas Lohninger
 */
@Dao
interface PlaylistDataAccess {

    @Transaction
    @Query("SELECT * FROM Playlist")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Transaction
    @Query("SELECT * FROM Playlist WHERE p_id = :playlistId")
    fun getPlaylistById(playlistId: Int): Flow<Playlist>

    @Query("SELECT * FROM PlaylistItem WHERE pi_p_id = :playlistId ORDER BY pi_custom_ordinal asc")
    fun getAllPlaylistItemsForPlaylist(playlistId: Int): Flow<List<PlaylistItem>>

    @Insert
    fun insertPlaylist(playlist: Playlist)

    @Delete
    fun deletePlaylist(playlist: Playlist)

    @Update
    fun renamePlaylist(playlist: Playlist)

    @Delete
    fun deletePlaylistItem(playlistMtc: PlaylistItem)

    @Update
    fun updatePlaylistItemList(playlistItemList: List<PlaylistItem>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertPlaylistItems(playlistItemList: List<PlaylistItem>)

    @Query("SELECT *, count(pi_p_id) as 'size' FROM Playlist LEFT JOIN PlaylistItem ON p_id = pi_p_id GROUP BY p_id")
    fun getAllPlaylistsWithSize(): Flow<List<PlaylistDTO>>

    @Insert
    fun insertPlaylistPlayed(playlistPlayed: PlaylistPlayed)

    @Transaction
    @Query("SELECT p.*, null FROM Playlist p JOIN PlaylistPlayed pp on pp_p_id = p.p_id GROUP BY p.p_id ORDER BY max(datetime(pp_played)) DESC")
    fun getPlaylistsByLastPlayed(): Flow<List<PlaylistDTO>>

    @Transaction
    @Query("SELECT p.*, count(p.p_id) FROM Playlist p JOIN PlaylistPlayed on pp_p_id = p.p_id GROUP BY p.p_id ORDER BY count(p.p_id) DESC")
    fun getPlaylistsByTimesPlayed(): Flow<List<PlaylistDTO>>

    @Transaction
    @Query("SELECT *, null FROM Playlist WHERE p_favourite = 1 ORDER BY p_custom_ordinal ASC")
    fun getFavouritePlaylists(): Flow<List<PlaylistDTO>>

    @Query("SELECT * FROM PlaylistPlayed ORDER BY pp_played DESC LIMIT 1")
    fun getLastPlaylistPlayed(): Flow<PlaylistPlayed>

    @Update
    fun updatePlaylistPlayed(playlistPlayed: PlaylistPlayed)

    @Query("SELECT p.* FROM Playlist p JOIN PlaylistItem on pi_p_id = p_id WHERE pi_t_id = :id")
    fun getPlaylistsByTrackId(id: Int): Flow<List<Playlist>>


}