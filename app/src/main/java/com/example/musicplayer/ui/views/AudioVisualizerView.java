package com.example.musicplayer.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.media.audiofx.Visualizer;
import android.util.AttributeSet;
import android.view.View;

import com.example.musicplayer.R;

import androidx.annotation.Nullable;

public class AudioVisualizerView extends View {

    private Context context;
    private Visualizer visualizer;
    private Rect clipBounds;
    private Paint waveFormColor;
    private Path wavePath;
    private int width;
    private int height;
    private float scaleRatio;

    private byte[] rawAudioData;
    public AudioVisualizerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context=context;
        TypedArray attrib = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AudioVisualizerView,0,0);
        try {

        } finally {
            attrib.recycle();
        }
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);

        canvas.getClipBounds(clipBounds);

        if (rawAudioData != null){
            int n = rawAudioData.length;

            float[] magnitudes = new float[n / 2 + 1];
            int index = n/4;
            magnitudes[0] = (float)Math.abs(rawAudioData[0]);      // DC
            magnitudes[index] = (float)Math.abs(rawAudioData[1]);  // Nyquist
            for (int k = 1; k < index; k++){
                int i = k * 2;
                magnitudes[k] = (float)Math.hypot(rawAudioData[i], rawAudioData[i + 1]);
            }

            // Draw
            int mod = (index) % 3;
            int newIndex = index - mod;
            float xStep = width/newIndex;

            wavePath.reset();
            for (int i = 0;i<newIndex;i+=3){
                wavePath.cubicTo(xStep*i,clipBounds.bottom - magnitudes[i]*scaleRatio,
                                 xStep*(i+1),clipBounds.bottom - scaleRatio*magnitudes[i+1],
                                 xStep*(i+2),clipBounds.bottom - scaleRatio*magnitudes[i+2]);
            }
            wavePath.lineTo(clipBounds.right,clipBounds.bottom);
            wavePath.lineTo(clipBounds.left,clipBounds.bottom);
            wavePath.close();

            canvas.drawPath(wavePath,waveFormColor);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width=w;
        height=h;
        scaleRatio=(float)height/255;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public void initVisualizer(int audioSessionId){
        visualizer = new Visualizer(audioSessionId);
        visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);

        visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int i) {
                //rawAudioData = bytes;
                //invalidate();
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int i) {
                rawAudioData = bytes;
                invalidate();
            }
        }, Visualizer.getMaxCaptureRate() / 2, false, true);

        visualizer.setEnabled(true);
    }

    public void setenableVisualizer(boolean state){visualizer.setEnabled(state);}

    private void init(){
        clipBounds = new Rect();
        wavePath = new Path();
        waveFormColor = new Paint();
        waveFormColor.setColor(context.getResources().getColor(R.color.colorPrimaryDark));
    }

    public void release(){
        if (visualizer != null)visualizer.release();
    }
}
