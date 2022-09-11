package com.example.musicplayer.ui.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.media.audiofx.Visualizer;
import android.util.AttributeSet;
import android.view.View;

import com.example.musicplayer.R;

import androidx.annotation.Nullable;

public class AudioVisualizerView extends View {

    private Visualizer visualizer = null;
    private Rect clipBounds;
    private Paint waveFormColor;
    private Path wavePath;
    private int width;
    private int height;
    private int density = 20;
    private float scaleRatio;
    private final int colorResource;

    private byte[] rawAudioData;

    public AudioVisualizerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray attrib = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AudioVisualizerView,0,0);
        try {
            colorResource = attrib.getColor(R.styleable.AudioVisualizerView_visualizerColor,context.getResources().getColor(R.color.colorBackground));
        } finally {
            attrib.recycle();
        }
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (visualizer != null){
            canvas.getClipBounds(clipBounds);

            if (rawAudioData != null) {
                int n = rawAudioData.length;

                float[] magnitudes = new float[n / 4 - 1];
                //magnitudes[0] = (float) Math.abs(rawAudioData[0]);      // DC
                //magnitudes[n/2] = (float) Math.abs(rawAudioData[1]);  // Nyquist

                for (int k = 2; k<n/4-1; k++){
                    int i = k * 2;
                    float abs = (float) Math.hypot(rawAudioData[i],rawAudioData[i+1]);
                    magnitudes[k] = abs;
                }

                float[] reducedMagnitudes = new float[density];
                int averageCount = magnitudes.length/density;
                for (int k = 0;k<density;k++){
                    int index = k * averageCount;
                    float average = 0;
                    for (int i = 0; i<averageCount;i++){
                        if (magnitudes[index + i] > average)average = magnitudes[index + i];
                    }
                    reducedMagnitudes[k] = average;
                }

                // Draw
                float xStep = width / (reducedMagnitudes.length-1);

                int bottom = clipBounds.bottom;
                int left = clipBounds.left;
                int right = clipBounds.right;

                wavePath.reset();
                wavePath.moveTo(left,bottom-scaleRatio*reducedMagnitudes[0]);

                for (int i = 1;i<reducedMagnitudes.length;i++){
                    float mag0 = scaleRatio*reducedMagnitudes[i-1];
                    float mag1 = scaleRatio*reducedMagnitudes[i];
                    float x = xStep * i;
                    wavePath.cubicTo(x - xStep/2, bottom - mag0,
                            x - xStep/2, bottom - mag1,
                            x, bottom - mag1);
                }

                wavePath.lineTo(right,bottom);
                wavePath.lineTo(left,bottom);

                canvas.drawPath(wavePath,waveFormColor);
            }
        } else {
            super.onDraw(canvas);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width=w;
        height=h;
        //Why 150: Magnitude experiments with Audiospectrum
        scaleRatio=(float)height/150;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public void initVisualizer(int audioSessionId){
        if (visualizer == null){
            visualizer = new Visualizer(audioSessionId);
            visualizer.setEnabled(false);
            visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
            visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
                @Override
                public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int i) {
                }

                @Override
                public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int i) {
                    rawAudioData = bytes;
                    invalidate();
                }
            }, Visualizer.getMaxCaptureRate() / 2, false, true);
            visualizer.setEnabled(true);
        }
    }

    public void setenableVisualizer(boolean state) {
        if (visualizer != null){
            visualizer.setEnabled(state);
        }
    }

    private void init(){
        clipBounds = new Rect();
        wavePath = new Path();
        waveFormColor = new Paint();
        waveFormColor.setColor(colorResource);
    }

    public void release(){
        if (visualizer != null)visualizer.release();
    }
}
