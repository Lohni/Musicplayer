package com.lohni.musicplayer.core

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import android.view.KeyEvent
import com.lohni.musicplayer.utils.enums.PlaybackAction

@Suppress("DEPRECATION")
class MediaSessionCallback : MediaSessionCompat.Callback() {
    private var MEDIA_BUTTON_DOWN_COUNT : Int = 0
    private var onSkipListener: OnSkipListener? = null
    private var onPauseListener: OnPauseListener? = null
    private var onPlayListener: OnPlayListener? = null
    private var onMediaButtonListener: OnMediaButtonListener? = null

    interface OnSkipListener {
        fun onSkip(action: PlaybackAction)
    }

    interface OnPauseListener {
        fun onPause()
    }

    interface OnPlayListener {
        fun onPlay()
    }

    interface OnMediaButtonListener {
        fun onMediaButtonClick()
    }

    fun setOnSkipListener(onSkipListener: OnSkipListener) {
        this.onSkipListener = onSkipListener
    }

    fun setOnPauseListener(onPauseListener: OnPauseListener) {
        this.onPauseListener = onPauseListener
    }

    fun setOnPlayListener(onPlayListener: OnPlayListener) {
        this.onPlayListener = onPlayListener
    }

    fun setOnMediaButtonListener(onMediaButtonListener: OnMediaButtonListener) {
        this.onMediaButtonListener = onMediaButtonListener
    }

    override fun onPlay() {
        onPlayListener?.onPlay()
    }

    override fun onPause() {
        onPauseListener?.onPause()
    }

    override fun onSkipToNext() {
        onSkipListener?.onSkip(PlaybackAction.SKIP_NEXT)
    }

    override fun onSkipToPrevious() {
        onSkipListener?.onSkip(PlaybackAction.SKIP_PREVIOUS)
    }

    override fun onMediaButtonEvent(mediaButtonIntent: Intent?): Boolean {
        val intentAction: String? = mediaButtonIntent?.action
        if (Intent.ACTION_MEDIA_BUTTON == intentAction) {
            val event: KeyEvent? = mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
            if (event != null && event.action == KeyEvent.ACTION_DOWN) {
                if (MEDIA_BUTTON_DOWN_COUNT == 0) Handler(Looper.getMainLooper()).postDelayed({MEDIA_BUTTON_DOWN_COUNT = 0}, 600)
                MEDIA_BUTTON_DOWN_COUNT++
                if (MEDIA_BUTTON_DOWN_COUNT < 2) onMediaButtonListener?.onMediaButtonClick()
                else onSkipListener?.onSkip(PlaybackAction.SKIP_NEXT)
            }
        }
        return true
    }
}