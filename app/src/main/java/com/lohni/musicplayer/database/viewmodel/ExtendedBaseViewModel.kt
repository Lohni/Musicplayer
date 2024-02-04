package com.lohni.musicplayer.database.viewmodel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel

open class ExtendedBaseViewModel : ViewModel() {
    open fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        var isFirst = true
        observe(lifecycleOwner, Observer {
            removeObserver(observer)
            if (isFirst) {
                isFirst = false
                observer.onChanged(it)
            }
        })
    }

    open fun <T> MutableLiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        var isFirst = true
        observe(lifecycleOwner, Observer {
            removeObserver(observer)
            if (isFirst) {
                isFirst = false
                observer.onChanged(it)
            }
        })
    }
}