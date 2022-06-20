package com.example.musicplayer.core;

import android.content.Context;
import android.transition.Fade;

import com.example.musicplayer.R;
import com.example.musicplayer.transition.AlbumDetailTransition;
import com.example.musicplayer.ui.expandedplaybackcontrol.ExpandedPlaybackControl;
import com.example.musicplayer.ui.playbackcontrol.PlaybackControl;

import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class MotionLayoutTransitionListenerImpl implements MotionLayout.TransitionListener {
    private final FragmentManager fragmentManager;
    private final Context context;
    private boolean isTransitionFragmentChanged = false;
    private boolean expand;

    public MotionLayoutTransitionListenerImpl(Context context, FragmentManager fragmentManager) {
        this.context = context;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public void onTransitionStarted(MotionLayout motionLayout, int i, int i1) {
        Fragment playbackControl = fragmentManager.findFragmentByTag(context.getString(R.string.fragment_playbackControl));
        expand = playbackControl != null;
    }

    @Override
    public void onTransitionChange(MotionLayout layout, int i, int i1, float progress) {
        if (expand) {
            if (progress > 0.7 && !isTransitionFragmentChanged && layout.getTargetPosition() == 1) {
                ExpandedPlaybackControl expandedPlaybackControl = new ExpandedPlaybackControl();
                expandedPlaybackControl.setEnterTransition(new Fade().setDuration(500));
                expandedPlaybackControl.setSharedElementEnterTransition(new AlbumDetailTransition());
                expandedPlaybackControl.setSharedElementReturnTransition(new AlbumDetailTransition());
                expandedPlaybackControl.setExitTransition(new Fade().setDuration(200));

                PlaybackControl playbackControl = (PlaybackControl) fragmentManager.findFragmentByTag(context.getString(R.string.fragment_playbackControl));
                playbackControl.setExitTransition(new Fade().setDuration(500));
                fragmentManager.beginTransaction()
                        .addSharedElement(playbackControl.getParentView(), context.getResources().getString(R.string.transition_playback_layout))
                        .addSharedElement(playbackControl.getTitleView(), context.getResources().getString(R.string.transition_playback_title))
                        .replace(R.id.playbackcontrol_holder, expandedPlaybackControl, context.getString(R.string.fragment_expandedPlaybackControl))
                        .commit();

                isTransitionFragmentChanged = true;
                System.out.println("Transition target Position: " + layout.getTargetPosition());
            }
        } else {
            if (progress < 0.8 && !isTransitionFragmentChanged && layout.getTargetPosition() == 0) {
                PlaybackControl playcontrol = new PlaybackControl();
                fragmentManager.beginTransaction()
                        .replace(R.id.playbackcontrol_holder, playcontrol, context.getString(R.string.fragment_playbackControl))
                        .commit();
                isTransitionFragmentChanged = true;
                //motionLayout.transitionToStart();
            }
        }
    }

    @Override
    public void onTransitionCompleted(MotionLayout motionLayout, int reachedState) {
    }

    @Override
    public void onTransitionTrigger(MotionLayout motionLayout, int i, boolean b, float v) {
    }
}
