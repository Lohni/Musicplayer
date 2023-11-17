package com.lohni.musicplayer.adapter;

import com.lohni.musicplayer.dto.EqualizerProperties;
import com.lohni.musicplayer.ui.audioeffects.EffectFragment;
import com.lohni.musicplayer.ui.audioeffects.EqualizerFragment;
import com.lohni.musicplayer.ui.audioeffects.ReverbFragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class EqualizerViewPagerAdapter extends FragmentStateAdapter {

    private final short[] equalizerBandLevels;
    private final EqualizerProperties equalizerProperties;

    public EqualizerViewPagerAdapter(@NonNull Fragment fragment,
                                     short[] equalizerBandLevels,
                                     EqualizerProperties equalizerProperties) {
        super(fragment);
        this.equalizerBandLevels = equalizerBandLevels;
        this.equalizerProperties = equalizerProperties;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: {
                EqualizerFragment fragment = new EqualizerFragment();
                fragment.initEqualizerFragment(equalizerBandLevels, equalizerProperties);
                return fragment;
            }
            case 1: {
                return new EffectFragment();
            }
            case 2: {
                return new ReverbFragment();
            }
            default: {
                return new Fragment();
            }
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
