package com.example.musicplayer.adapter;

import com.example.musicplayer.database.entity.Track;
import com.example.musicplayer.ui.playbackcontrol.PlaybackControlCoverFragment;
import com.example.musicplayer.ui.playbackcontrol.PlaybackControlDetailFragment;
import com.example.musicplayer.ui.playbackcontrol.PlaybackControlInfoFragment;
import com.example.musicplayer.ui.playbackcontrol.PlaybackControlQueueFragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class PlaybackControlViewPagerAdapter extends FragmentStateAdapter {
    private PlaybackControlDetailFragment currentFragment;
    private Track currentTrack;

    public PlaybackControlViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: {
                currentFragment = new PlaybackControlQueueFragment();
                currentFragment.setCurrentTrack(currentTrack);
                return currentFragment;
            }
            case 2: {
                currentFragment = new PlaybackControlInfoFragment();
                currentFragment.setCurrentTrack(currentTrack);
                return currentFragment;
            }
            default: {
                currentFragment = new PlaybackControlCoverFragment();
                currentFragment.setCurrentTrack(currentTrack);
                return currentFragment;
            }
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    public void setCurrentTrack(Track currentTrack) {
        this.currentTrack = currentTrack;
        if (currentFragment != null) {
            currentFragment.setCurrentTrack(currentTrack);
        }
    }
}
