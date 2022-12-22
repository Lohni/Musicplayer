package com.lohni.musicplayer.transition;

import androidx.transition.ChangeBounds;
import androidx.transition.ChangeClipBounds;
import androidx.transition.ChangeImageTransform;
import androidx.transition.ChangeTransform;
import androidx.transition.TransitionSet;

public class AlbumDetailTransition extends TransitionSet {
    public static final int DURATION = 300;

    public AlbumDetailTransition(){
        setOrdering(ORDERING_TOGETHER);
        addTransition(new ChangeBounds());
        addTransition(new ChangeTransform());
        addTransition(new ChangeImageTransform());
        addTransition(new ChangeClipBounds());
        setDuration(DURATION);
    }

}
