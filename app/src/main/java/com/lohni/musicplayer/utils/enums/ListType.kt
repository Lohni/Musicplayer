package com.lohni.musicplayer.utils.enums

import com.lohni.musicplayer.R

enum class ListType {
    TRACK, PLAYLIST, ALBUM;

    companion object {
        fun getListTypeById(type: Int): ListType {
            return when (type) {
                1 -> PLAYLIST
                2 -> ALBUM
                else -> TRACK
            }
        }

        fun getIdFromListType(listType: ListType) : Int {
            return when (listType) {
                ALBUM -> 2
                PLAYLIST -> 1
                else -> 0
            }
        }

        fun getTitleForListType(type: ListType?): String {
            return when (type) {
                ALBUM -> "Album"
                PLAYLIST -> "Playlist"
                else -> "Track"
            }
        }

        fun getDrawableIdForListType(type: ListType?): Int {
            return when (type) {
                ALBUM -> R.drawable.ic_album_black_24dp
                PLAYLIST -> R.drawable.ic_playlist_play_black_24dp
                else -> R.drawable.ic_baseline_music_note_24
            }
        }
    }
}