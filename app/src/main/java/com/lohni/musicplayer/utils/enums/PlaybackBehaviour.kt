package com.lohni.musicplayer.utils.enums

import com.lohni.musicplayer.R

enum class PlaybackBehaviour {
    SHUFFLE, REPEAT_LIST, REPEAT_SONG;

    companion object {
        fun getNextState(state: PlaybackBehaviour): PlaybackBehaviour {
            return when (state) {
                SHUFFLE -> REPEAT_LIST
                REPEAT_LIST -> REPEAT_SONG
                REPEAT_SONG -> SHUFFLE
            }
        }

        fun getStateAsInteger(state: PlaybackBehaviour?): Int {
            return when (state) {
                SHUFFLE -> 0
                REPEAT_LIST -> 1
                REPEAT_SONG -> 2
                else -> 3
            }
        }

        fun getStateFromInteger(state: Int): PlaybackBehaviour? {
            return when (state) {
                0 -> SHUFFLE
                1 -> REPEAT_LIST
                2 -> REPEAT_SONG
                else -> REPEAT_LIST
            }
        }

        fun getDrawableResourceIdForState(state: PlaybackBehaviour?): Int {
            return when (state) {
                SHUFFLE -> R.drawable.ic_round_shuffle_24
                REPEAT_SONG -> R.drawable.ic_round_repeat_one_24
                else -> R.drawable.ic_round_repeat_24
            }
        }
    }
}