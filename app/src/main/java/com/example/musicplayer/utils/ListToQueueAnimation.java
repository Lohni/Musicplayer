package com.example.musicplayer.utils;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;

public class ListToQueueAnimation {
    private static final int DEFAUL_DURATION = 300, DEFAULT_FADE_DURATION = 500;
    private ArrayList<View> targetViewList = new ArrayList<>();
    private ArrayList<View> animatedViewList = new ArrayList<>();

    private int[] dest;

    private WeakReference<Activity> mContextReference;
    private Animator.AnimatorListener mAnimationListener;

    public ListToQueueAnimation(){}

    public ListToQueueAnimation attachActivity(Activity activity){
        mContextReference = new WeakReference<>(activity);
        return this;
    }

    public ListToQueueAnimation setTargetView(View view){
        this.targetViewList.add(view);
        return this;
    }

    public ListToQueueAnimation setTargetView(ArrayList<View> targetViewList){
        this.targetViewList = targetViewList;
        return this;
    }

    public ListToQueueAnimation setDestCoord(int[] dest){
        this.dest = dest;
        return this;
    }

    private boolean prepare(){
        if (mContextReference.get() != null){
            ViewGroup decoreView = (ViewGroup) mContextReference.get().getWindow().getDecorView();
            for (int i = 0; i < targetViewList.size(); i++){
                View targetView = targetViewList.get(i);
                Bitmap bitmap = drawViewToBitmap(targetView, targetView.getWidth(), targetView.getHeight());
                ImageView animatedView = new ImageView(mContextReference.get());

                animatedView.setImageBitmap(bitmap);

                int[] src = new int[2];
                targetView.getLocationOnScreen(src);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(targetView.getWidth(), targetView.getHeight());
                if (animatedView.getParent() == null)
                    decoreView.addView(animatedView, params);
                animatedViewList.add(animatedView);
            }
        }
        return true;
    }

    public void startAnimation() {
        if (prepare()) {
            getListToQueueAnimator().start();
        }
    }

    private AnimatorSet getListToQueueAnimator(){
        Collection<Animator> animatorCollection = new ArrayList<>();
        AnimatorSet finalAnimatorSet = new AnimatorSet();
        for (int i = 0; i<targetViewList.size(); i++){
            View targetView = targetViewList.get(i);
            View animatedView = animatedViewList.get(i);

            int[] src = new int[2];
            targetView.getLocationOnScreen(src);

            Animator translatorX = ObjectAnimator.ofFloat(animatedView, View.X, src[0], dest[0]);
            translatorX.setInterpolator(new LinearInterpolator());
            Animator translatorY = ObjectAnimator.ofFloat(animatedView, View.Y, src[1], dest[1]);
            translatorY.setInterpolator(new LinearInterpolator());
            translatorX.setDuration(DEFAUL_DURATION);
            translatorY.setDuration(DEFAUL_DURATION);

            Animator disappearAnimatorY = ObjectAnimator.ofFloat(animatedView, View.SCALE_Y, 1, 0);
            Animator disappearAnimatorX = ObjectAnimator.ofFloat(animatedView, View.SCALE_X, 1, 0);
            disappearAnimatorX.setDuration(DEFAULT_FADE_DURATION);
            disappearAnimatorY.setDuration(DEFAULT_FADE_DURATION);

            animatorCollection.add(translatorX);
            animatorCollection.add(translatorY);
            animatorCollection.add(disappearAnimatorX);
            animatorCollection.add(disappearAnimatorY);
        }
        finalAnimatorSet.playTogether(animatorCollection);
        return finalAnimatorSet;
    }

    private Bitmap drawViewToBitmap(View view, int width, int height) {
        Drawable drawable = new BitmapDrawable();
//        view.layout(0, 0, width, height);
        Bitmap dest = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(dest);
        drawable.setBounds(new Rect(0, 0, width, height));
        drawable.draw(c);
        view.draw(c);
//        view.layout(0, 0, width, height);
        return dest;
    }
}
