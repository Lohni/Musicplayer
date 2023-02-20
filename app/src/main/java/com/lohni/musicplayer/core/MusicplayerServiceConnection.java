package com.lohni.musicplayer.core;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.lohni.musicplayer.interfaces.ServiceConnectionListener;

public class MusicplayerServiceConnection implements ServiceConnection {
    private MusicService musicService;
    private final ServiceConnectionListener serviceConnectionListener;

    public MusicplayerServiceConnection(ServiceConnectionListener serviceConnectionListener) {
        this.serviceConnectionListener = serviceConnectionListener;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        MusicService.MusicBinder binder = (MusicService.MusicBinder) iBinder;
        musicService = binder.getServiceInstance();
        serviceConnectionListener.onServiceConnected(musicService);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        musicService.pause();
        musicService = null;
    }
}
