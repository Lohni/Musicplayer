package com.lohni.musicplayer.core

import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Optional

class ApplicationDataViewModel : ViewModel() {
    private val _trackImages = MutableLiveData<HashMap<Int, Drawable>>(HashMap())
    private val _albumCovers = MutableLiveData<HashMap<Int, Drawable>>(HashMap())

    val trackImages: LiveData<HashMap<Int, Drawable>> = _trackImages
    val albumCovers: LiveData<HashMap<Int, Drawable>> = _albumCovers

    fun getImageForTrack(trackId: Int): Optional<Drawable> {
        return Optional.ofNullable(trackImages.value?.get(trackId))
    }

    fun addImageDrawables(hm: HashMap<Int, Drawable>) {
        _trackImages.postValue(hm)
    }

    fun addAlbumDrawable(map: HashMap<Int, Drawable>) {
        _albumCovers.postValue(map)
    }
}