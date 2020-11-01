package com.example.musicplayer.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.example.musicplayer.R;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class ControlKnob extends View {

    private Rect clipBounds;
    private float width, height, progressThickness, paddingKnobToProgress;
    private float angle = 0;
    private Paint primaryStroke, primary, secondary, progressSecondary;
    private OnControlKnobChangeListener onControlKnobChangeListener;

    public interface OnControlKnobChangeListener{
        void onChange(float percent);
    }

    public ControlKnob(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        clipBounds = new Rect();
        primaryStroke = new Paint();
        primary = new Paint();
        secondary = new Paint();
        progressSecondary = new Paint();
        primaryStroke.setColor(ContextCompat.getColor(context, R.color.colorSecondaryLight));
        primaryStroke.setStyle(Paint.Style.STROKE);
        primary.setColor(ContextCompat.getColor(context, R.color.colorSecondaryLight));
        secondary.setColor(ContextCompat.getColor(context, R.color.colorPrimaryNight));
        secondary.setStyle(Paint.Style.STROKE);
        progressThickness = dipTOPx(2f,context);
        paddingKnobToProgress = dipTOPx(8f,context);
        primaryStroke.setStrokeWidth(progressThickness);
        secondary.setStrokeWidth(progressThickness);
        progressSecondary.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
        progressSecondary.setStyle(Paint.Style.STROKE);
        progressSecondary.setStrokeWidth(progressThickness);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.getClipBounds(clipBounds);
        width = clipBounds.right;
        height = clipBounds.bottom;
        //Draw Progress indicator
        RectF rectF = new RectF();
        rectF.top = clipBounds.top + progressThickness;
        rectF.left = clipBounds.left + progressThickness;
        rectF.bottom = clipBounds.bottom - progressThickness;
        rectF.right = clipBounds.right - progressThickness;

        float radius = ((width/2)-progressThickness-paddingKnobToProgress);

        canvas.drawArc(rectF,135,270,false, progressSecondary);
        canvas.drawArc(rectF,135,angle,false, primaryStroke);
        canvas.drawCircle(width/2,height/2,radius, primary);
        canvas.drawLine(width/2,height/2,(width/2)+(float)Math.cos(Math.toRadians(225-angle))*radius,(height/2)-(float)Math.sin(Math.toRadians(225-angle))*radius,secondary);
    }

    private float dipTOPx(float dip, Context context){
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                context.getResources().getDisplayMetrics()
        );
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                trackingTouch(event);
                break;
            case MotionEvent.ACTION_MOVE:
                trackingTouch(event);
                break;
            case MotionEvent.ACTION_UP:
                trackingTouch(event);
                break;
        }
        return true;
    }

    private void trackingTouch(MotionEvent event){
        float xDiff = event.getX() - (width/2);
        float yDiff = (height/2) - event.getY();

        if (xDiff < 0){
            float tan = (float)Math.atan(yDiff/xDiff);
            angle = 180 + (float) ((tan * 180)/Math.PI);
        }
        else if (yDiff < 0) angle = 360 + (float) ((Math.atan(yDiff/xDiff) * 180)/Math.PI);
        else angle = (float) ((Math.atan(yDiff/xDiff) * 180)/Math.PI);

        if (angle >= 315) angle = 225 + (360 - angle);
        else if (angle <= 225) angle = 225 - angle;
        else angle = -1;

        if (angle >=0 && angle <=270){
            invalidate();
            onControlKnobChangeListener.onChange(angle);
        }
    }

    public void setOnControlKnobChangeListener(OnControlKnobChangeListener onControlKnobChangeListener){
        this.onControlKnobChangeListener = onControlKnobChangeListener;
    }
}
