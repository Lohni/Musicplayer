package com.lohni.musicplayer.core

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.lohni.musicplayer.database.entity.Album
import com.lohni.musicplayer.database.entity.Preference
import com.lohni.musicplayer.database.entity.Track
import com.lohni.musicplayer.utils.GeneralUtils

class MediaStoreSync {
    companion object {
        fun syncMediaStoreTracks(applicationContext: Context, tracksDB: List<Track>, excludeWaPref: Preference): ArrayList<Track> {
            val waRegex = if (excludeWaPref.prefValue.isNotEmpty()) Regex(excludeWaPref.prefValue) else null

            val fromMediaStore = ArrayList<Track>()
            val musicUri: Uri =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

            val musicCursor = applicationContext.contentResolver.query(musicUri, null, null, null, null) ?: throw Exception()
            musicCursor.moveToFirst()

            val titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val albumid = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
            val durationColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
            val trackIdColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TRACK)

            do {
                val thisalbumid = musicCursor.getLong(albumid)
                val thisId = musicCursor.getLong(idColumn)
                val duration = musicCursor.getLong(durationColumn)
                val thisTitle = musicCursor.getString(titleColumn)
                val thisArtist = musicCursor.getString(artistColumn)
                val trackId = musicCursor.getInt(trackIdColumn)

                val track = Track()
                track.tId = thisId.toInt()
                track.tAlbumId = thisalbumid.toInt()
                track.tTitle = thisTitle
                track.tArtist = thisArtist
                track.tDuration = duration.toInt()
                track.tTrackNr = trackId
                track.tIsFavourite = 0
                track.tDeleted = 0
                track.tCreated = GeneralUtils.getCurrentUTCTimestamp()

                tracksDB.stream().filter {
                        trackDB: Track -> trackDB.tId == thisId.toInt()
                }.findFirst().ifPresent {
                    track.tIsFavourite = it.tIsFavourite
                    track.tCreated = it.tCreated
                    track.tDeleted = it.tDeleted
                }

                if (waRegex == null || !track.tTitle.matches(waRegex)) {
                    fromMediaStore.add(track)
                }
            } while (musicCursor.moveToNext())
            musicCursor.close()
            return fromMediaStore
        }

        fun syncMediaStoreAlbums(applicationContext: Context, albumsDB: List<Album>) : ArrayList<Album> {
            val toInsert = java.util.ArrayList<Album>()
            val musicUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI

            val _id = MediaStore.Audio.Albums._ID
            val album_name = MediaStore.Audio.Albums.ALBUM
            val totSongs = MediaStore.Audio.Albums.NUMBER_OF_SONGS
            val artist_Name = MediaStore.Audio.Albums.ARTIST

            val cursor: Cursor = applicationContext.contentResolver.query(musicUri, null, null, null, null) ?: throw Exception()
            cursor.moveToFirst()
            val albumIdColum = cursor.getColumnIndex(_id)
            val albumArtistColumn = cursor.getColumnIndex(artist_Name)
            val albumNameColumn = cursor.getColumnIndex(album_name)
            val albumTotSongsColumn = cursor.getColumnIndex(totSongs)
            do {
                val albumId = cursor.getLong(albumIdColum)
                val albumName = cursor.getString(albumNameColumn)
                val albumArtist = cursor.getString(albumArtistColumn)
                val totalSongs = cursor.getInt(albumTotSongsColumn)
                val album = Album()
                album.aId = albumId.toInt()
                album.aNumSongs = totalSongs
                album.aArtistName = albumArtist
                album.aName = albumName
                album.aCreated = GeneralUtils.getCurrentUTCTimestamp()
                toInsert.add(album)
            } while (cursor.moveToNext())

            cursor.close()
            return toInsert
        }
    }
}