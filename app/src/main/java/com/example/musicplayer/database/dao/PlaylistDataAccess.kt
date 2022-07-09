package com.example.musicplayer.database.dao

import androidx.room.*
import androidx.room.OnConflictStrategy.IGNORE
import com.example.musicplayer.database.entity.Playlist
import com.example.musicplayer.database.entity.PlaylistItem
import com.example.musicplayer.database.entity.PlaylistPlayed
import com.example.musicplayer.ui.playlist.PlaylistDTO
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

    @Insert(onConflict = IGNORE)
    fun insertPlaylistItems(playlistItemList: List<PlaylistItem>)

    @Query("SELECT *, count(pi_p_id) as 'size' FROM Playlist LEFT JOIN PlaylistItem ON p_id = pi_p_id GROUP BY p_id")
    fun getAllPlaylistsWithSize(): Flow<List<PlaylistDTO>>

    @Insert
    fun insertPlaylistPlayed(playlistPlayed: PlaylistPlayed)
}