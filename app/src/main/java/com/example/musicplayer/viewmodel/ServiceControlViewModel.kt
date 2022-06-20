package com.example.musicplayer.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.musicplayer.entities.MusicResolver

class ServiceControlViewModel : ViewModel() {

    val songList: MutableLiveData<List<MusicResolver>> = TODO()


    fun setSongList(songsToAdd: List<MusicResolver>) {
        songList.postValue(songsToAdd)
    }

}