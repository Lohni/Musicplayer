package com.example.musicplayer.ui.expandedplaybackcontrol;

import androidx.transition.ChangeBounds;
import androidx.transition.ChangeClipBounds;
import androidx.transition.ChangeImageTransform;
import androidx.transition.ChangeTransform;
import androidx.transition.TransitionSet;

public class ExpandedTransition extends TransitionSet {
    public ExpandedTransition(){
        setOrdering(ORDERING_TOGETHER);
        addTransition(new ChangeBounds().setDuration(200)).
                addTransition(new ChangeTransform().setDuration(200)).addTransition(new ChangeClipBounds().setDuration(200));
    }
}
