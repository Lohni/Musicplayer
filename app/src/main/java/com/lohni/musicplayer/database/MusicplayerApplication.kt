package com.lohni.musicplayer.database

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class MusicplayerApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy { MusicplayerDatabase.getDatabase(this, applicationScope) }
}