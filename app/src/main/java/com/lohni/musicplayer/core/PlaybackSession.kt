package com.lohni.musicplayer.core

import android.os.Bundle
import com.lohni.musicplayer.database.entity.Track
import com.lohni.musicplayer.utils.enums.ListType
import com.lohni.musicplayer.utils.enums.PlaybackAction
import com.lohni.musicplayer.utils.enums.PlaybackBehaviour
import java.util.*
import kotlin.random.Random

class PlaybackSession {
    private val queueList = ArrayList<Track>()
    private var playbackBehaviour = PlaybackBehaviour.REPEAT_LIST
    private var listType = ListType.TRACK
    private var listTypeObjectId : Int = -1
    private val snapshot = StateSnapshot()
    private var onListPlayInterface : OnListPlay? = null

    interface OnListPlay {
        fun onListPlay(bundle: Bundle)
    }

    fun setOnListPlayListener(onListPlayListener: OnListPlay) {
        this.onListPlayInterface = onListPlayListener
    }

    fun getCurrentTrack(): Optional<Track> {
        return Optional.ofNullable(snapshot.currentTrack)
    }

    fun addToQueue(tracks: List<Track>, playNext: Boolean) {
        val sizePreAdd = queueList.size

        for ((i, track) in tracks.withIndex()) {
            if (playNext) {
                val startIndex = if (snapshot.currQueueIndex >= 0) snapshot.currQueueIndex + 1 else 0
                if (!queueList.contains(track)) {
                    queueList.add(startIndex + i, track)
                } else {
                    val oldIndex = queueList.indexOf(track)
                    val toPos = if (oldIndex < startIndex) snapshot.currQueueIndex else startIndex + i
                    changeOrder(oldIndex, toPos)
                }
            } else if (!queueList.contains(track)) {
                queueList.add(track)
            }
        }

        if (sizePreAdd == 0 && playNext) {
            snapshot.currQueueIndex = 0
            snapshot.currentTrack = queueList[0]
            snapshot.currentListType = listType
            if (isListTypeAlbumOrPlaylist()) {
                getSnapshotAsBundle().ifPresent{ onListPlayInterface?.onListPlay(it) }
            }
        }
    }

    fun clearQueue() {
        queueList.clear()
        snapshot.currQueueIndex = -1
    }

    fun removeTracksFromQueue(tracks: List<Track>) : Boolean {
        val deleteCurrentTrack = tracks.contains(snapshot.currentTrack)
        queueList.removeAll(tracks.toSet())
        snapshot.currQueueIndex = if (deleteCurrentTrack) -1 else queueList.indexOf(snapshot.currentTrack)
        return deleteCurrentTrack
    }

    fun nextTrack(action: PlaybackAction) {
        if (queueList.isNotEmpty() && snapshot.currQueueIndex >= 0) {
            getNextSong(action)

            if (isListTypeAlbumOrPlaylist()
                && playbackBehaviour == PlaybackBehaviour.REPEAT_LIST
                && action == PlaybackAction.SKIP_NEXT
                && snapshot.currQueueIndex == 0 ) {
                getSnapshotAsBundle().ifPresent { onListPlayInterface?.onListPlay(it) }
            }
        }
    }

    fun setTrack(track: Track) {
        val index: Int = queueList.indexOf(track)
        if (index >= 0) setTrack(index)
    }

    fun setTrack(track: Int) {
        snapshot.currQueueIndex = track
        snapshot.currentTrack = queueList[snapshot.currQueueIndex]
    }

    fun setPlaybackBehaviour(playbackBehaviour: PlaybackBehaviour) {
        this.playbackBehaviour = playbackBehaviour
    }

    fun setListType(listType: ListType) {
        this.listType = listType

        if (listType == ListType.TRACK) {
            listTypeObjectId = -1
        }
    }

    fun setListTypeObject(listTypeObjectId: Int) {
        this.listTypeObjectId = listTypeObjectId
    }

    fun isListTypeAlbumOrPlaylist() : Boolean {
        return snapshot.currentListType == ListType.ALBUM || snapshot.currentListType == ListType.PLAYLIST
    }

    fun clearSnapshot() {
        snapshot.reset()
    }

    fun getPlaybackBehaviour() : PlaybackBehaviour {
        return playbackBehaviour
    }

    fun getSnapshotAsBundle() : Optional<Bundle> {
        val track = snapshot.currentTrack
        if (track != null) {
            val bundle = Bundle()
            bundle.putInt("ID", track.tId)
            bundle.putInt("TYPE", ListType.getIdFromListType(listType))
            bundle.putInt("LIST_INDEX", snapshot.currQueueIndex)
            bundle.putInt("TYPE_OBJECT_ID", listTypeObjectId);
            return Optional.of(bundle)
        }
         return Optional.empty()
    }

    fun getStateAsBundle() : Bundle {
        val bundle = Bundle()
        val track = snapshot.currentTrack
        bundle.putString("TITLE", if (track != null) track.tTitle else "")
        bundle.putString("ARTIST", if (track != null) track.tArtist else "")
        bundle.putInt("DURATION", if (track != null) track.tDuration else 0)
        bundle.putInt("ID", if (track != null) track.tId else -1)
        bundle.putInt("QUEUE_SIZE", queueList.size)
        bundle.putInt("QUEUE_INDEX", snapshot.currQueueIndex)
        bundle.putInt("BEHAVIOUR_STATE", PlaybackBehaviour.getStateAsInteger(playbackBehaviour))
        bundle.putParcelableArrayList("PARCELABLE_TRACK_LIST", queueList)
        return bundle
    }

    fun changeOrder(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(queueList, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(queueList, i, i - 1)
            }
        }
        snapshot.currQueueIndex =  queueList.indexOf(snapshot.currentTrack)
    }

    class StateSnapshot {
        var currentTrack: Track? = null
        var currentListType: ListType = ListType.TRACK
        var currQueueIndex: Int = -1
        var currentListTypeId: Int = -1

        fun reset() {
            currentTrack = null
            currentListType = ListType.TRACK
            currQueueIndex = -1
            currentListTypeId = -1
        }
    }

    /* Private */
    private fun getNextSong(action: PlaybackAction) {
        if (snapshot.currQueueIndex < 0) {
            snapshot.reset()
            return
        }

        when(playbackBehaviour) {
            PlaybackBehaviour.SHUFFLE -> snapshot.currQueueIndex = Random.Default.nextInt(queueList.size)
            PlaybackBehaviour.REPEAT_LIST -> {
                val currIndex = snapshot.currQueueIndex
                if (action == PlaybackAction.SKIP_NEXT) {
                    snapshot.currQueueIndex = if (currIndex == queueList.size - 1) 0 else currIndex + 1
                } else {
                    snapshot.currQueueIndex = if (currIndex == 0) queueList.size - 1 else currIndex -1
                }
            }
            else -> {}
        }

        snapshot.currentTrack = queueList[snapshot.currQueueIndex]
        snapshot.currentListType = listType
        snapshot.currentListTypeId = listTypeObjectId
    }
}