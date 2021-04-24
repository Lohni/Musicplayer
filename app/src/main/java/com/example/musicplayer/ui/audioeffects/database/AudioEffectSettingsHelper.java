package com.example.musicplayer.ui.audioeffects.database;

import android.media.audiofx.EnvironmentalReverb;

public class AudioEffectSettingsHelper {

    public static ReverbSettings updateReverbSettingsValues(ReverbSettings reverbSettings, EnvironmentalReverb.Settings settings){
        return setReverbValues(reverbSettings, settings);
    }

    //Todo: Check if Preset name exists
    public static ReverbSettings createReverbSettings(EnvironmentalReverb.Settings settings){
        ReverbSettings reverbSettings = new ReverbSettings();
        reverbSettings.setReverb_preset_name("Custom");
        return setReverbValues(reverbSettings, settings);
    }

    public static ReverbSettings createReverbSettings(EnvironmentalReverb.Settings settings, String presetName){
        ReverbSettings reverbSettings = new ReverbSettings();
        reverbSettings.setReverb_preset_name(presetName);
        return setReverbValues(reverbSettings, settings);
    }

    public static EnvironmentalReverb.Settings extractReverbValues(ReverbSettings reverbSettings){
        EnvironmentalReverb.Settings settings = new EnvironmentalReverb.Settings();
        settings.roomLevel = reverbSettings.getMasterLevel();
        settings.roomHFLevel = reverbSettings.getRoomHFLevel();
        settings.reverbLevel = reverbSettings.getReverbLevel();
        settings.reverbDelay = reverbSettings.getReverbDelay();
        settings.reflectionsLevel = reverbSettings.getReflectionLevel();
        settings.reflectionsDelay = reverbSettings.getReflectionDelay();
        settings.diffusion = reverbSettings.getDiffusion();
        settings.density = reverbSettings.getDensity();
        settings.decayHFRatio = reverbSettings.getDecayHFRatio();
        settings.decayTime = reverbSettings.getDecayTime();
        return settings;
    }

    private static ReverbSettings setReverbValues(ReverbSettings reverbSettings, EnvironmentalReverb.Settings settings){
        reverbSettings.setMasterLevel(settings.roomLevel);
        reverbSettings.setRoomHFLevel(settings.roomHFLevel);
        reverbSettings.setReverbLevel(settings.reverbLevel);
        reverbSettings.setReverbDelay(settings.reverbDelay);
        reverbSettings.setReflectionLevel(settings.reflectionsLevel);
        reverbSettings.setReflectionDelay(settings.reflectionsDelay);
        reverbSettings.setDiffusion(settings.diffusion);
        reverbSettings.setDensity(settings.density);
        reverbSettings.setDecayHFRatio(settings.decayHFRatio);
        reverbSettings.setDecayTime(settings.decayTime);
        return reverbSettings;
    }

}
