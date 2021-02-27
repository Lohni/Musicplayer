package com.example.musicplayer.adapter;

import android.media.audiofx.Equalizer;

import com.example.musicplayer.ui.equalizer.EffectFragment;
import com.example.musicplayer.ui.equalizer.EqualizerFragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class EqualizerViewPagerAdapter extends FragmentStateAdapter {

    private Equalizer equalizer;
    private int audioSessionID;

    public EqualizerViewPagerAdapter(@NonNull Fragment fragment, Equalizer equalizer, int sessionID) {
        super(fragment);
        this.equalizer = equalizer;
        this.audioSessionID = sessionID;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1){
            EffectFragment fragment = new EffectFragment();
            fragment.init(audioSessionID);
            return fragment;
        }
        else {
            EqualizerFragment fragment = new EqualizerFragment();
            fragment.initEqualizerFragment(equalizer);
            return fragment;
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
