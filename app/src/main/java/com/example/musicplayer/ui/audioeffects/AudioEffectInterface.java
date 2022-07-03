package com.example.musicplayer.ui.audioeffects;

import android.media.audiofx.EnvironmentalReverb;

public interface AudioEffectInterface {
    void onEnvironmentalReverbChanged(EnvironmentalReverb.Settings settings, boolean status);

    void onEqualizerStatusChanged(boolean state);

    void onBassBoostChanged(int strength, boolean state);

    void onVirtualizerChanged(int strength, boolean state);

    void onLoudnessEnhancerChanged(int strength, boolean state);
}
