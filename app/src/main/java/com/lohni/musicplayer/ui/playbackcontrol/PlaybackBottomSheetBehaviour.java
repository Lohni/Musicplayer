package com.lohni.musicplayer.ui.playbackcontrol;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class PlaybackBottomSheetBehaviour<V extends View> extends BottomSheetBehavior<V> {
    private float touchOriginX = -1, touchOriginY = -1;

    public PlaybackBottomSheetBehaviour() {
    }

    public PlaybackBottomSheetBehaviour(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull CoordinatorLayout parent, @NonNull V child, @NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            touchOriginX = event.getRawX();
            touchOriginY = event.getRawY();
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE
                && touchOriginX > -1 && touchOriginY > -1) {
            if ((parent.getHeight() / 2) < touchOriginY) return false;
            if (isDownMotion(event.getRawX(), event.getRawY())) {
                return true;
            }
        }

        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            touchOriginX = -1;
            touchOriginY = -1;
        }

        return super.onInterceptTouchEvent(parent, child, event);
    }

    private boolean isDownMotion(float currX, float currY) {
        float vX = currX - touchOriginX;
        float vY = currY - touchOriginY;

        double num = (double) vY;
        double denum = Math.sqrt(Math.pow(vX, 2) + Math.pow(vY, 2));

        double angle = (Math.acos(num / denum) * 180) / Math.PI;
        return angle <= 10;
    }

}
