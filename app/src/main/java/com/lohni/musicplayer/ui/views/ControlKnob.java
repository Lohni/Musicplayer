package com.lohni.musicplayer.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.lohni.musicplayer.R;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class ControlKnob extends View {

    private Rect clipBounds;
    private float width, height, progressThickness, paddingKnobToProgress;
    private float angle = 0;
    private Paint primaryStroke, primary, secondary, progressSecondary, disabledPrimary, disabledPrimaryStroke;
    private OnControlKnobChangeListener onControlKnobChangeListener;
    private OnInfoClickedListener onInfoClickedListener;
    private OnControlKnobActionUpListener onControlKnobActionUpListener;
    private Drawable info;
    private boolean isInfoDrawn=false, isInfoCliced=false, isEnabled = true, isInfoEnabled = true;
    private int min=0, max=0, range=0, currentValue=0;

    private String infoText = "";

    public interface OnControlKnobChangeListener{
        void onChange(View view, int value);
    }

    public interface OnInfoClickedListener{
        void onInfoClick(String info);
    }

    public interface OnControlKnobActionUpListener{
        void onControlKnobActionUp();
    }

    public ControlKnob(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        clipBounds = new Rect();
        primaryStroke = new Paint();
        primary = new Paint();
        secondary = new Paint();
        progressSecondary = new Paint();
        disabledPrimary = new Paint();
        disabledPrimaryStroke = new Paint();
        primaryStroke.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
        primaryStroke.setStyle(Paint.Style.STROKE);
        primary.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
        secondary.setColor(ContextCompat.getColor(context, R.color.colorOnPrimary));
        secondary.setStyle(Paint.Style.STROKE);
        disabledPrimary.setColor(ContextCompat.getColor(context, R.color.colorSurface));
        disabledPrimaryStroke.setColor(ContextCompat.getColor(context, R.color.colorSurface));
        disabledPrimaryStroke.setStyle(Paint.Style.STROKE);
        progressThickness = dipTOPx(2f,context);
        paddingKnobToProgress = dipTOPx(6f,context);
        primaryStroke.setStrokeWidth(progressThickness);
        secondary.setStrokeWidth(progressThickness);
        progressSecondary.setColor(ContextCompat.getColor(context, R.color.colorSurfaceVariant));
        progressSecondary.setStyle(Paint.Style.STROKE);
        progressSecondary.setStrokeWidth(progressThickness);
        info = context.getResources().getDrawable(R.drawable.ic_baseline_info_24, null);
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

        angle = ((float) (range-(max-currentValue)) / range) * 270;

        canvas.drawArc(rectF,135,270,false, progressSecondary);

        if (isEnabled){
            canvas.drawArc(rectF,135,angle,false, primaryStroke);
            canvas.drawCircle(width/2,height/2,radius, primary);

        } else {
            canvas.drawArc(rectF,135,angle,false, disabledPrimaryStroke);
            canvas.drawCircle(width/2,height/2,radius, disabledPrimary);
        }
        canvas.drawLine(width/2,height/2,(width/2)+(float)Math.cos(Math.toRadians(225-angle))*radius,(height/2)-(float)Math.sin(Math.toRadians(225-angle))*radius,secondary);

        if (!isInfoDrawn){
            isInfoDrawn = true;
            int width_info = (int) (Math.sqrt(2*Math.pow(width/2,2)) - (width/2) - paddingKnobToProgress*1.2);
            info.setBounds((int) (width-width_info), clipBounds.top,(int) width, width_info);

        }
        if (isInfoEnabled)info.draw(canvas);
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
                Rect infoBounds = info.getBounds();
                if (event.getX() > infoBounds.left && event.getY() < infoBounds.bottom){
                    isInfoCliced = true;
                } else {
                    isInfoCliced = false;
                    trackingTouch(event);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                trackingTouch(event);
                break;
            case MotionEvent.ACTION_UP:
                Rect infoBound = info.getBounds();
                if (event.getX() > infoBound.left && event.getY() < infoBound.bottom && isInfoCliced && isInfoEnabled){
                    isInfoCliced = false;
                    onInfoClickedListener.onInfoClick(infoText);
                } else trackingTouch(event);
                break;
        }
        return true;
    }

    private void trackingTouch(MotionEvent event){
        if (isEnabled){
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
                currentValue = (int) (max - ((270-angle)/270)*range);
                invalidate();
                onControlKnobChangeListener.onChange(this, currentValue);
                if (event.getAction() == MotionEvent.ACTION_UP)onControlKnobActionUpListener.onControlKnobActionUp();
            }
        }
    }

    public void setOnControlKnobChangeListener(OnControlKnobChangeListener onControlKnobChangeListener){
        this.onControlKnobChangeListener = onControlKnobChangeListener;
    }

    public void setOnInfoClickListener(OnInfoClickedListener onInfoClickListener){
        this.onInfoClickedListener = onInfoClickListener;
    }

    public void setOnControlKnobActionUpListener(OnControlKnobActionUpListener onControlKnobActionUpListener){
        this.onControlKnobActionUpListener = onControlKnobActionUpListener;
    }

    public void setInfoText(String text){infoText = text;}

    public void setRange(int min, int max){
        this.min = min;
        this.max = max;
        range = max - min;
    }

    public boolean setCurrentValue(int value){
        if (value >= min && value <= max){
            currentValue = value;
            invalidate();
            return true;
        }
        return false;
    }

    public void isEnabled(boolean state){
        this.isEnabled = state;
        invalidate();
    }

    public void isInfoDrawn(boolean state){
        this.isInfoEnabled = state;
        invalidate();
    }
}
