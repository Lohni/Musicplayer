package com.example.musicplayer.ui.views;

import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.example.musicplayer.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.constraintlayout.motion.widget.MotionScene;
import androidx.constraintlayout.widget.ConstraintSet;

public class TestMotionLayout extends MotionLayout {
    private float mDownX;
    private float mDownY;
    private boolean touchOnTarget = false, isInExpandedState = false;
    RectF bounds = new RectF();

    public TestMotionLayout(@NonNull Context context) {
        super(context);
    }

    public TestMotionLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TestMotionLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!isInExpandedState){
            View child = getTargetView();
            if (child != null){
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:{
                        if (getViewBounds(child).contains(event.getX(), event.getY())){
                            mDownX = event.getX();
                            mDownY = event.getY();
                            touchOnTarget = true;
                        }
                        break;
                    }
                    case MotionEvent.ACTION_MOVE:{
                        if (touchOnTarget){
                            float x = event.getX();
                            float y = event.getY();
                            ViewConfiguration vc = ViewConfiguration.get(this.getContext());

                            float xDelta = Math.abs(x - mDownX);
                            float yDelta = Math.abs(y - mDownY);
                            if (yDelta > vc.getScaledTouchSlop() && yDelta / 2 > xDelta){
                                //transitionToEnd();
                                super.onInterceptTouchEvent(event);
                                //setState(R.id.motion_start,-1,-1);
                                return true;
                            }
                        }
                        break;
                    }
                }
                return super.onInterceptTouchEvent(event);
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isInExpandedState){
            if (event.getAction() == MotionEvent.ACTION_UP){
                touchOnTarget = false;
            }
            return super.onTouchEvent(event);
        }
        return false;
    }

    private RectF getViewBounds(View view){
        RectF rectF = new RectF();
        rectF.set(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        return rectF;
    }

    private View getTargetView(){
        for (int i = 0; i<getChildCount(); i++){
            View child = getChildAt(i);
            if (child.getTag().toString().equals("playbackcontrol_holder"))return child;
        }
        return null;
    }

    public void setInExpandedState(boolean isExpanded){
        isInExpandedState = isExpanded;
    }

}
