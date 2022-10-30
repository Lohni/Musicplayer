package com.example.musicplayer.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.example.musicplayer.R
import com.example.musicplayer.database.MusicplayerDatabase
import com.example.musicplayer.database.dao.MusicplayerDataAccess
import com.example.musicplayer.database.entity.TrackPlayed
import com.example.musicplayer.utils.GeneralUtils
import com.example.musicplayer.utils.converter.DashboardEnumDeserializer
import com.example.musicplayer.utils.enums.DashboardListType
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
                val type = DashboardEnumDeserializer.getDashboardListType(bundle.getInt("TYPE"))
                CoroutineScope(Dispatchers.IO).launch { updateTrackPlayedWithDuration(trackId, duration, type, db) }
            } else if (intent.action.equals(p0.resources.getString(R.string.musicservice_song_prepared))) {
                val bundle: Bundle = intent.extras!!
                val trackId: Int = bundle.getInt("ID")
                CoroutineScope(Dispatchers.IO).launch { createNewTrackPlayed(trackId, db) }
            }
        }
    }

    private suspend fun updateTrackPlayedWithDuration(trackId: Int, duration: Long, type: DashboardListType, db: MusicplayerDataAccess) {
        val trackPlayed: TrackPlayed = db.getLastTrackPlayed().first()
        if (trackPlayed.tpTId.equals(trackId)) {
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
}