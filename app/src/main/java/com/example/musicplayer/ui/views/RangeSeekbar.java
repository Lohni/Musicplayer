package com.example.musicplayer.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.musicplayer.R;
import com.example.musicplayer.utils.GeneralUtils;
import com.example.musicplayer.utils.images.ImageTransformUtil;

import androidx.annotation.Nullable;

public class RangeSeekbar extends View {
    private final long[][] snappingPoints = {{10000, 1000}, {30000, 5000}, {60000, 10000}, {300000, 30000},
            {1800000, 60000}, {3600000, 300000}, {7200000, 600000}, {10800000, 900000}, {14400000, 1800000}};
    private final Rect clipBounds;
    private final Paint innerRangeColor, outerRangeColor, text;
    private final Path backgroundPath;
    private final Path innerPath;
    private long from = 0, to = 14400000, max = 14400000;
    private final float touchMargin, height;
    private DragHandle dragHandle = DragHandle.NONE;
    private OnValueChangedListener onValueChangedListener;

    public interface OnValueChangedListener {
        void onValueChanged(long newValue, DragHandle dragHandle);
    }

    public RangeSeekbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        innerRangeColor = new Paint();
        outerRangeColor = new Paint();
        text = new Paint();

        TypedArray attrib = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RangeSeekbar, 0, 0);
        innerRangeColor.setColor(attrib.getColor(R.styleable.RangeSeekbar_innerColor, context.getColor(R.color.colorSecondary)));
        outerRangeColor.setColor(attrib.getColor(R.styleable.RangeSeekbar_outerColor, context.getColor(R.color.colorSurfaceVariant)));
        attrib.recycle();

        text.setColor(context.getResources().getColor(R.color.colorOnBackground, null));
        text.setStrokeWidth(5f);
        text.setAntiAlias(true);
        text.setTextSize(ImageTransformUtil.convertSpToPixel(16, context));

        clipBounds = new Rect();
        touchMargin = ImageTransformUtil.convertDpToPixel(8f, context.getResources());
        height = ImageTransformUtil.convertSpToPixel(10f, context);
        backgroundPath = new Path();
        innerPath = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.getClipBounds(clipBounds);
        long bottom = Math.min(clipBounds.bottom, Math.round(height));

        backgroundPath.reset();
        backgroundPath.addRoundRect(clipBounds.left, clipBounds.top, clipBounds.right, bottom, touchMargin / 4, touchMargin / 4, Path.Direction.CW);
        backgroundPath.close();

        long xStart = (long) Math.cbrt((((float) from / (float) max) * Math.pow(clipBounds.right, 3)));
        long xEnd = (long) Math.cbrt((((float) to / (float) max) * Math.pow(clipBounds.right, 3)));

        innerPath.reset();
        innerPath.addRoundRect(xStart, clipBounds.top, xEnd, bottom, touchMargin / 4, touchMargin / 4, Path.Direction.CW);
        innerPath.close();

        canvas.drawPath(backgroundPath, outerRangeColor);
        canvas.drawPath(innerPath, innerRangeColor);

        String fromTimestamp = GeneralUtils.convertTimeWithUnit((int) from);
        canvas.drawText(fromTimestamp, clipBounds.left, clipBounds.bottom, text);

        String toTimestamp = GeneralUtils.convertTimeWithUnit((int) to);
        float x = text.measureText(toTimestamp);
        canvas.drawText(toTimestamp, clipBounds.right - x, clipBounds.bottom, text);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (clipBounds.contains((int) event.getX(), (int) event.getY()) || dragHandle != DragHandle.NONE) {
            long xStart = (long) Math.cbrt((((float) from / (float) max) * Math.pow(clipBounds.right, 3)));
            long xEnd = (long) Math.cbrt((((float) to / (float) max) * Math.pow(clipBounds.right, 3)));

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (xStart - touchMargin < event.getX() && event.getX() < xStart + touchMargin) {
                        dragHandle = DragHandle.FROM;
                        handleDrag(event);
                    } else if (xEnd - touchMargin < event.getX() && event.getX() < xEnd + touchMargin) {
                        dragHandle = DragHandle.TO;
                        handleDrag(event);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    handleDrag(event);
                    return false;

                case MotionEvent.ACTION_UP:
                    handleDrag(event);
                    if (onValueChangedListener != null && dragHandle != DragHandle.NONE) {
                        onValueChangedListener.onValueChanged((dragHandle == DragHandle.FROM) ? from : to, dragHandle);
                    }
                    dragHandle = DragHandle.NONE;
                    break;
            }
        }
        return true;
    }

    private void handleDrag(MotionEvent event) {
        double t = (Math.pow(event.getX(), 3)) / Math.pow(clipBounds.right, 3);
        long newProgress = snapTo(Math.round(t * max));
        long margin = Math.round((touchMargin / clipBounds.width()) * max);
        if (dragHandle == DragHandle.FROM && newProgress < (to - margin)) {
            from = Math.max(Math.round(newProgress), 0);
        } else if (dragHandle == DragHandle.TO && newProgress > (from + margin)) {
            to = Math.min(Math.round(newProgress), max);
        }
        invalidate();
    }

    private long snapTo(long newPosition) {
        for (int i = 0; i < snappingPoints.length; i++) {
            if (snappingPoints[i][0] >= newPosition) {
                return Math.round(((float) newPosition) / (float) snappingPoints[i][1]) * snappingPoints[i][1];
            }
        }
        return newPosition;
    }

    public void setFrom(long from) {
        this.from = from;
        invalidate();
    }

    public void setTo(long to) {
        if (to > 0) {
            this.to = to;
            invalidate();
        }
    }

    public void setMax(long max) {
        this.max = max;
    }

    public void setOnValueChangedListener(OnValueChangedListener onValueChangedListener) {
        this.onValueChangedListener = onValueChangedListener;
    }

    public enum DragHandle {
        FROM, TO, NONE
    }
}
