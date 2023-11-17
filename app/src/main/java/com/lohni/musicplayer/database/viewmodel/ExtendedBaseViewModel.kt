package com.lohni.musicplayer.database.viewmodel

import androidx.lifecycle.*

open class ExtendedBaseViewModel : ViewModel() {
    open fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, Observer {
            removeObserver(observer)
            observer.onChanged(it)
        })
    }

    open fun <T> MutableLiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, Observer {
            removeObserver(observer)
            observer.onChanged(it)
        })
    }
}