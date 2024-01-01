package com.lohni.musicplayer.adapter;

import com.lohni.musicplayer.ui.playbackcontrol.PlaybackControlCoverFragment;
import com.lohni.musicplayer.ui.playbackcontrol.PlaybackControlInfoFragment;
import com.lohni.musicplayer.ui.playbackcontrol.PlaybackControlQueueFragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class PlaybackControlViewPagerAdapter extends FragmentStateAdapter {

    public PlaybackControlViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public PlaybackControlViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: {
                return new PlaybackControlQueueFragment();
            }
            case 2: {
                return new PlaybackControlInfoFragment();
            }
            default: {
                return new PlaybackControlCoverFragment();
            }
        }
    }

    @Override
    public void setStateRestorationPolicy(@NonNull StateRestorationPolicy strategy) {
        super.setStateRestorationPolicy(strategy);
    }

    public int getItemCount() {
        return 3;
    }
}
