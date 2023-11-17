package com.lohni.musicplayer.core

import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first

class ApplicationDataViewModel : ViewModel() {
    private val _trackImages = MutableLiveData<HashMap<Int, Drawable>>(HashMap())
    private val _albumCovers = MutableLiveData<HashMap<Int, Drawable>>(HashMap())

    val trackImages: LiveData<HashMap<Int, Drawable>> = _trackImages
    val albumCovers: LiveData<HashMap<Int, Drawable>> = _albumCovers

    fun getImageForTrack(trackId: Int) : Drawable? {
        return trackImages.value?.getOrDefault(trackId, null)
    }

    fun addImageDrawables(hm: HashMap<Int, Drawable>) {
        _trackImages.value?.let {
            hm.putAll(it)
            _trackImages.postValue(hm)
        }
    }

    fun addAlbumDrawable(map: HashMap<Int, Drawable>) {
        _albumCovers.postValue(map)
    }
}