package com.example.musicplayer.adapter;

import android.media.audiofx.EnvironmentalReverb;

import com.example.musicplayer.ui.audioeffects.EffectFragment;
import com.example.musicplayer.ui.audioeffects.EqualizerFragment;
import com.example.musicplayer.utils.enums.EqualizerProperties;
import com.example.musicplayer.ui.audioeffects.ReverbFragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class  EqualizerViewPagerAdapter extends FragmentStateAdapter {

    private EnvironmentalReverb.Settings settings;
    private boolean reverbEnabled, equalizerEnabled;
    private short[] equalizerBandLevels;
    private EqualizerProperties equalizerProperties;

    public EqualizerViewPagerAdapter(@NonNull Fragment fragment, EnvironmentalReverb.Settings settings,
                                     boolean reverbEnabled, short[] equalizerBandLevels,
                                     boolean equalizerEnabled, EqualizerProperties equalizerProperties) {
        super(fragment);
        this.settings = settings;
        this.reverbEnabled = reverbEnabled;
        this.equalizerBandLevels = equalizerBandLevels;
        this.equalizerEnabled = equalizerEnabled;
        this.equalizerProperties = equalizerProperties;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:{
                EqualizerFragment fragment = new EqualizerFragment();
                fragment.initEqualizerFragment(equalizerBandLevels, equalizerEnabled, equalizerProperties);
                return fragment; }
            case 1:{
                EffectFragment fragment = new EffectFragment();
                return fragment; }
            case 2:{
                ReverbFragment fragment = new ReverbFragment();
                fragment.setSettings(settings, reverbEnabled);
                return fragment; }
            default:{
                return new Fragment();
            }
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
