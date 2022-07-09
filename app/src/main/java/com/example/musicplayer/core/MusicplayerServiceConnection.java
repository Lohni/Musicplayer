package com.example.musicplayer.core;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;

import com.example.musicplayer.MusicService;
import com.example.musicplayer.R;
import com.example.musicplayer.interfaces.ServiceConnectionListener;

public class MusicplayerServiceConnection implements ServiceConnection {

    private final Context context;
    private MusicService musicService;
    private final SharedPreferences sharedPreferences;
    private final ServiceConnectionListener serviceConnectionListener;

    public MusicplayerServiceConnection(Context context, SharedPreferences preferences, ServiceConnectionListener serviceConnectionListener) {
        this.context = context;
        this.sharedPreferences = preferences;
        this.serviceConnectionListener = serviceConnectionListener;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        MusicService.MusicBinder binder = (MusicService.MusicBinder) iBinder;
        musicService = binder.getServiceInstance();

        //Init AudioEffects
        musicService.setReverbEnabled(sharedPreferences.getBoolean(context.getResources().getString(R.string.preference_reverb_isenabled), false));
        musicService.setEqualizerEnabled(sharedPreferences.getBoolean(context.getResources().getString(R.string.preference_equalizer_isenabled), false));
        musicService.setBassBoostEnabled(sharedPreferences.getBoolean(context.getResources().getString(R.string.preference_bassboost_isenabled), false));
        musicService.setVirtualizerEnabled(sharedPreferences.getBoolean(context.getResources().getString(R.string.preference_virtualizer_isenabled), false));
        musicService.setLoudnessEnhancerEnabled(sharedPreferences.getBoolean(context.getResources().getString(R.string.preference_loudnessenhancer_isenabled), false));

        musicService.setBassBoostStrength((short) sharedPreferences.getInt(context.getResources().getString(R.string.preference_bassboost_strength), 0));
        musicService.setVirtualizerStrength((short) sharedPreferences.getInt(context.getResources().getString(R.string.preference_virtualizer_strength), 0));
        musicService.setLoudnessEnhancerGain(sharedPreferences.getInt(context.getResources().getString(R.string.preference_loudnessenhancer_strength), 0));

        serviceConnectionListener.onServiceConnected(musicService);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        musicService.pause();
        musicService = null;
    }
}
