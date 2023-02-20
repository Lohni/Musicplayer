package com.lohni.musicplayer.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.lohni.musicplayer.R
import com.lohni.musicplayer.database.MusicplayerDatabase
import com.lohni.musicplayer.database.dao.MusicplayerDataAccess
import com.lohni.musicplayer.database.entity.*
import com.lohni.musicplayer.utils.GeneralUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SystemBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, intent: Intent?) {
        if (p0 != null && intent != null && intent.extras != null) {
            val db = MusicplayerDatabase.getDatabase(p0, CoroutineScope(SupervisorJob())).musicplayerDao()
            if (intent.action.equals(p0.resources.getString(R.string.musicservice_song_ended))) {
                val bundle: Bundle = intent.extras!!
                val trackId = bundle.getInt("ID")
                val duration = bundle.getLong("TIME_PLAYED")
                CoroutineScope(Dispatchers.IO).launch { updateTrackPlayedWithDuration(trackId, duration, db) }
            } else if (intent.action.equals(p0.resources.getString(R.string.musicservice_song_prepared))) {
                val bundle: Bundle = intent.extras!!
                val trackId: Int = bundle.getInt("ID")
                val albumId: Int = bundle.getInt("ALBUM_ID", -1)
                val playlistId: Int = bundle.getInt("PLAYLIST_ID", -1)
                CoroutineScope(Dispatchers.IO).launch {
                    createNewTrackPlayed(trackId, db)
                    createListPlayed(albumId, playlistId, db)
                }
            } else if (intent.action.equals(p0.resources.getString(R.string.musicservice_album_play))) {
                val bundle: Bundle = intent.extras!!
                val albumId = bundle.getInt("ALBUM_ID")
                CoroutineScope(Dispatchers.IO).launch { createNewAlbumPlayed(albumId, db) }
            } else if (intent.action.equals(p0.resources.getString(R.string.musicservice_playlist_play))) {
                val bundle: Bundle = intent.extras!!
                val playlistId = bundle.getInt("PLAYLIST_ID")
                CoroutineScope(Dispatchers.IO).launch { createNewPlaylistPlayed(playlistId, db) }
            }
        }
    }

    private suspend fun updateTrackPlayedWithDuration(trackId: Int, duration: Long, db: MusicplayerDataAccess) {
        val trackPlayed: TrackPlayed = db.getLastTrackPlayed().first()
        if (trackPlayed.tpTId == trackId) {
            trackPlayed.tpTimePlayed = duration
            db.updateTrackPlayed(trackPlayed)
        }
    }

    private fun createNewTrackPlayed(trackId: Int, db: MusicplayerDataAccess) {
        val trackPlayed = TrackPlayed()
        trackPlayed.tpTId = trackId
        trackPlayed.tpTimePlayed = 0L
        trackPlayed.tpPlayed = GeneralUtils.getCurrentUTCTimestamp()
        db.insertTrackPlayed(trackPlayed)
    }

    private suspend fun createListPlayed(albumId: Int, playlistId: Int, db: MusicplayerDataAccess) {
        val trackPlayed: TrackPlayed = db.getLastTrackPlayed().first()
        if (albumId >= 0) {
            createAlbumTrackPlayed(albumId, trackPlayed.tpId, db)
        }
        if (playlistId >= 0) {
            createPlaylistTrackPlayed(playlistId, trackPlayed.tpId, db)
        }
    }

    private fun createNewAlbumPlayed(albumId: Int, db: MusicplayerDataAccess) {
        val albumPlayed = AlbumPlayed()
        albumPlayed.apAId = albumId
        albumPlayed.apPlayed = GeneralUtils.getCurrentUTCTimestamp()
        db.insertAlbumPlayed(albumPlayed)
    }

    private fun createNewPlaylistPlayed(playlistId: Int, db: MusicplayerDataAccess) {
        val playlistPlayed = PlaylistPlayed()
        playlistPlayed.ppPId = playlistId
        playlistPlayed.ppPlayed = GeneralUtils.getCurrentUTCTimestamp()
        db.insertPlaylistPlayed(playlistPlayed)
    }

    private suspend fun createAlbumTrackPlayed(albumId: Int, trackPlayedId: Int, db: MusicplayerDataAccess) {
        val albumPlayed = db.getLastAlbumPlayed().first()
        if (albumPlayed.apAId == albumId) {
            val albumTrackPlayed =  AlbumTrackPlayed()
            albumTrackPlayed.atpTpId = trackPlayedId
            albumTrackPlayed.atpApId = albumPlayed.apId
            db.insertAlbumTrackPlayed(albumTrackPlayed)
        }
    }

    private suspend fun createPlaylistTrackPlayed(playlistId: Int, trackPlayedId: Int, db: MusicplayerDataAccess) {
        val playlistPlayed = db.getLastPlaylistPlayed().first()
        if (playlistPlayed.ppPId == playlistId) {
            val playlistTrackPlayed = PlaylistTrackPlayed()
            playlistTrackPlayed.ptpTpId = trackPlayedId
            playlistTrackPlayed.ptpPpId = playlistPlayed.ppId
            db.insertPlaylistTrackPlayed(playlistTrackPlayed)
        }
    }
}