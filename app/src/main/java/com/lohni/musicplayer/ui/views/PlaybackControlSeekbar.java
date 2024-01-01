package com.lohni.musicplayer.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.lohni.musicplayer.R;

public class PlaybackControlSeekbar extends View {

    private boolean mIsDragged = false, fromUser = true;
    private OnSeekbarChangeListener onSeekbarChangeListener;
    private int size, progress, width;
    private final Rect clipBounds;
    private Paint progressPrimaryBackgroundTint;
    private Paint progressSecondaryBackgroundTint;
    private Path progressPrimaryPath;
    private Path progressSecondaryPath;

    public PlaybackControlSeekbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        clipBounds = new Rect();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.getClipBounds(clipBounds);
        width = clipBounds.right;

        float scale = 0;
        if (size > 0) {
            scale = (float) width / size;
        }

        //Draw Primary Progress
        progressPrimaryPath.reset();
        progressPrimaryPath.moveTo(clipBounds.left, clipBounds.top);
        progressPrimaryPath.lineTo(scale * progress, clipBounds.top);
        progressPrimaryPath.lineTo(scale * progress, clipBounds.bottom);
        progressPrimaryPath.lineTo(clipBounds.left, clipBounds.bottom);
        progressPrimaryPath.close();

        //Draw seconary progress
        progressSecondaryPath.reset();
        progressSecondaryPath.moveTo(scale * progress, clipBounds.top);
        progressSecondaryPath.lineTo(clipBounds.right, clipBounds.top);
        progressSecondaryPath.lineTo(clipBounds.right, clipBounds.bottom);
        progressSecondaryPath.lineTo(scale * progress, clipBounds.bottom);
        progressSecondaryPath.close();

        canvas.drawPath(progressPrimaryPath, progressPrimaryBackgroundTint);
        canvas.drawPath(progressSecondaryPath, progressSecondaryBackgroundTint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (clipBounds.contains((int) event.getX(), (int) event.getY()) || fromUser) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    fromUser = true;
                    startDrag(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    fromUser = true;
                    if (mIsDragged) trackingTouch(event);
                    else startDrag(event);
                    return false;

                case MotionEvent.ACTION_UP:
                    fromUser = true;
                    trackingTouch(event);
                    onStopTrackingTouch();
                    invalidate();
                    break;
            }
        }
        return true;
    }

    private void startDrag(MotionEvent event) {
        onStartTrackingTouch();
        trackingTouch(event);
    }

    public void setMax(int max) {
        this.size = max;
    }

    public void setProgress(int progress) {
        fromUser = false;
        this.progress = progress;
        invalidate();
    }

    public void init(int progressPrimaryBackgroundTint, int progressSecondaryBackgroundTint) {
        progressPrimaryPath = new Path();
        progressSecondaryPath = new Path();

        this.progressPrimaryBackgroundTint = new Paint();
        this.progressSecondaryBackgroundTint = new Paint();
        this.progressPrimaryBackgroundTint.setColor(getContext().getResources().getColor(progressPrimaryBackgroundTint, null));
        this.progressSecondaryBackgroundTint.setColor(getContext().getResources().getColor(progressSecondaryBackgroundTint, null));
    }

    public void onStartTrackingTouch() {
        mIsDragged = true;
        if (onSeekbarChangeListener != null) onSeekbarChangeListener.onStartTrackingTouch(this);
    }

    public void onStopTrackingTouch() {
        mIsDragged = false;
        if (onSeekbarChangeListener != null)
            onSeekbarChangeListener.onStopTrackingTouch(this, progress);
    }

    private void trackingTouch(MotionEvent event) {
        int x = Math.round(event.getX());
        float p = x / ((float) width);
        progress = (int) (p * size);
        if (onSeekbarChangeListener != null)
            onSeekbarChangeListener.onProgressChanged(this, progress, fromUser);
        invalidate();
    }

    public void setSeekbarChangeListener(OnSeekbarChangeListener onSeekbarChangeListener) {
        this.onSeekbarChangeListener = onSeekbarChangeListener;
    }

    public interface OnSeekbarChangeListener {
        default void onProgressChanged(PlaybackControlSeekbar seekbar, int progress, boolean fromUser) {
        }

        void onStartTrackingTouch(PlaybackControlSeekbar seekbar);

        void onStopTrackingTouch(PlaybackControlSeekbar seekbar, int progress);
    }
}