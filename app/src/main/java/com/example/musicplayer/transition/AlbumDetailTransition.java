package com.example.musicplayer.transition;

import android.transition.ChangeBounds;
import android.transition.ChangeClipBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.TransitionSet;

import com.example.musicplayer.ui.album.AlbumDetailFragment;

public class AlbumDetailTransition extends TransitionSet {
    public static final int DURATION = 500;

    public AlbumDetailTransition(){
        setOrdering(ORDERING_TOGETHER);
        addTransition(new ChangeBounds());
        addTransition(new ChangeTransform());
        addTransition(new ChangeImageTransform());
        addTransition(new ChangeClipBounds());
        setDuration(DURATION);
    }

}
