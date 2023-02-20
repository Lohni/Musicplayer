package com.lohni.musicplayer.ui.audioeffects;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.lohni.musicplayer.R;
import com.lohni.musicplayer.adapter.EqualizerViewPagerAdapter;
import com.lohni.musicplayer.dto.EqualizerProperties;
import com.lohni.musicplayer.interfaces.NavigationControlInterface;
import com.lohni.musicplayer.utils.Permissions;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

public class EqualizerViewPager extends Fragment {

    private ViewPager2 viewPager2;
    private EqualizerViewPagerAdapter mAdapter;
    private short[] equalizerBandLevels;
    private NavigationControlInterface navigationControlInterface;
    private EqualizerProperties equalizerProperties;

    public EqualizerViewPager() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        try {
            navigationControlInterface = (NavigationControlInterface) context;
        } catch (ClassCastException e) {
            Log.e("EQUALIZER_CASTERROR", e.toString());
        }
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_equalizer_view_pager, container, false);

        navigationControlInterface.setToolbarTitle("Equalizer");
        navigationControlInterface.setHomeAsUpEnabled(false);
        navigationControlInterface.isDrawerEnabledListener(true);

        viewPager2 = view.findViewById(R.id.eualizer_viewpager);
        viewPager2.setUserInputEnabled(false);

        TabLayout tabLayout = view.findViewById(R.id.equalizer_tablayout);

        if (Permissions.permission(requireActivity(), this, Manifest.permission.MODIFY_AUDIO_SETTINGS)) {
            mAdapter = new EqualizerViewPagerAdapter(this, equalizerBandLevels, equalizerProperties);
            viewPager2.setAdapter(mAdapter);
        }

        new TabLayoutMediator(tabLayout, viewPager2, ((tab, position) -> {
            if (position == 0) {
                tab.setText("Frequenzy");
                tab.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_equalizer_black_24dp));
            } else if (position == 1) {
                tab.setText("Effects");
                tab.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_hearing_24));
            } else {
                tab.setText("Reverb");
                tab.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_surround_sound_24));
            }
        })).attach();
        return view;
    }

    public void setSettings(short[] equalizerBandLevels, EqualizerProperties equalizerProperties) {
        this.equalizerBandLevels = equalizerBandLevels;
        this.equalizerProperties = equalizerProperties;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && permissions[0].equals(Manifest.permission.MODIFY_AUDIO_SETTINGS)) {
            mAdapter = new EqualizerViewPagerAdapter(this, equalizerBandLevels, equalizerProperties);
            viewPager2.setAdapter(mAdapter);
        }
    }
}