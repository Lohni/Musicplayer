package com.example.musicplayer.ui.equalizer;

import android.content.Context;
import android.media.audiofx.Equalizer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.example.musicplayer.R;
import com.example.musicplayer.adapter.EqualizerViewPagerAdapter;
import com.example.musicplayer.utils.NavigationControlInterface;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class EqualizerViewPager extends Fragment {

    private ViewPager2 viewPager2;
    private EqualizerViewPagerAdapter mAdapter;
    private Equalizer equalizer;
    private int audioSessionID;
    private NavigationControlInterface navigationControlInterface;

    public EqualizerViewPager() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        try {
            navigationControlInterface = (NavigationControlInterface) context;
        } catch (ClassCastException e){
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
        mAdapter = new EqualizerViewPagerAdapter(this,equalizer,audioSessionID);
        viewPager2.setAdapter(mAdapter);
        viewPager2.setUserInputEnabled(false);

        TabLayout tabLayout = view.findViewById(R.id.equalizer_tablayout);
        new TabLayoutMediator(tabLayout, viewPager2, ((tab, position) -> {
            if (position == 0) {
                tab.setText("Frequenzy");
                tab.setIcon(ContextCompat.getDrawable(requireContext(),R.drawable.ic_equalizer_black_24dp));
            }
            else{
                tab.setText("Effects");
                tab.setIcon(ContextCompat.getDrawable(requireContext(),R.drawable.ic_baseline_hearing_24));
            }
        })).attach();
        return view;
    }

    public void setEqualizer(Equalizer equalizer){
        this.equalizer = equalizer;
    }

    public void setAudioSessionID(int id){
        audioSessionID = id;
    }
}