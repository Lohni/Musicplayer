package com.example.musicplayer.ui.audioeffects;

import android.media.audiofx.EnvironmentalReverb;

import com.example.musicplayer.database.entity.AdvancedReverbPreset;

public class AudioEffectSettingsHelper {

    public static AdvancedReverbPreset updateReverbSettingsValues(AdvancedReverbPreset reverbSettings, EnvironmentalReverb.Settings settings) {
        return setReverbValues(reverbSettings, settings);
    }

    //Todo: Check if Preset name exists
    public static AdvancedReverbPreset createReverbSettings(EnvironmentalReverb.Settings settings) {
        AdvancedReverbPreset reverbSettings = new AdvancedReverbPreset();
        reverbSettings.setArName("Custom");
        return setReverbValues(reverbSettings, settings);
    }

    public static AdvancedReverbPreset createReverbSettings(EnvironmentalReverb.Settings settings, String presetName) {
        AdvancedReverbPreset reverbSettings = new AdvancedReverbPreset();
        reverbSettings.setArName(presetName);
        return setReverbValues(reverbSettings, settings);
    }

    public static EnvironmentalReverb.Settings extractReverbValues(AdvancedReverbPreset reverbSettings) {
        EnvironmentalReverb.Settings settings = new EnvironmentalReverb.Settings();
        settings.roomLevel = reverbSettings.getArMasterLevel().shortValue();
        settings.roomHFLevel = reverbSettings.getArRoomHfLevel().shortValue();
        settings.reverbLevel = reverbSettings.getArReverbLevel().shortValue();
        settings.reverbDelay = reverbSettings.getArReverbDelay();
        settings.reflectionsLevel = reverbSettings.getArReflectionLevel().shortValue();
        settings.reflectionsDelay = reverbSettings.getArReflectionDelay();
        settings.diffusion = reverbSettings.getArDiffusion().shortValue();
        settings.density = reverbSettings.getArDensity().shortValue();
        settings.decayHFRatio = reverbSettings.getArDecayHfRatio().shortValue();
        settings.decayTime = reverbSettings.getArDecayTime();
        return settings;
    }

    private static AdvancedReverbPreset setReverbValues(AdvancedReverbPreset reverbSettings, EnvironmentalReverb.Settings settings) {
        reverbSettings.setArMasterLevel((int) settings.roomLevel);
        reverbSettings.setArRoomHfLevel((int) settings.roomHFLevel);
        reverbSettings.setArReverbLevel((int) settings.reverbLevel);
        reverbSettings.setArReverbDelay(settings.reverbDelay);
        reverbSettings.setArReflectionLevel((int) settings.reflectionsLevel);
        reverbSettings.setArReflectionDelay(settings.reflectionsDelay);
        reverbSettings.setArDiffusion((int) settings.diffusion);
        reverbSettings.setArDensity((int) settings.density);
        reverbSettings.setArDecayHfRatio((int) settings.decayHFRatio);
        reverbSettings.setArDecayTime(settings.decayTime);
        return reverbSettings;
    }

}
