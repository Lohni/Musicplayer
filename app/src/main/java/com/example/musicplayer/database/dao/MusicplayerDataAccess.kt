package com.example.musicplayer.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.musicplayer.database.entity.Track
import kotlinx.coroutines.flow.Flow


/**
 * @author Andreas Lohninger
 */
@Dao
interface MusicplayerDataAccess {

    @Query("SELECT * FROM Track")
    fun getAllTracks(): Flow<List<Track>>

    @Query("SELECT * FROM Track where t_id in (:trackList)")
    fun getTracksByIds(trackList: List<Int>): Flow<List<Track>>

    @Insert
    fun insertTracks(trackList: List<Track>)
}