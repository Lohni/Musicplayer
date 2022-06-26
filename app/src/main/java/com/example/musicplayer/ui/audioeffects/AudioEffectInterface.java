package com.example.musicplayer.ui.audioeffects;

import android.media.audiofx.EnvironmentalReverb;

import com.example.musicplayer.database.entity.EqualizerPreset;

public interface AudioEffectInterface {
    void onEnvironmentalReverbChanged(EnvironmentalReverb.Settings settings);
    void onEnvironmentalReverbStatusChanged(boolean state);
    void onEqualizerStatusChanged(boolean state);
    void onBassBoostChanged(int strength);
    void onBassBoostStatusChanged(boolean state);
    void onVirtualizerChanged(int strength);
    void onVirtualizerStatusChanged(boolean state);
    void onLoudnessEnhancerChanged(int strength);
    void onLoudnessEnhancerStatusChanged(boolean state);
}
