package com.example.musicplayer.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.example.musicplayer.R;
import com.example.musicplayer.database.dto.StatisticDTO;
import com.example.musicplayer.utils.GeneralUtils;
import com.example.musicplayer.utils.enums.StatisticType;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import androidx.annotation.Nullable;

public class XYGraphView extends View {
    private List<StatisticDTO> values;
    private int width = 0;
    private int height = 0;
    private float textHeight = 0;
    private float padding = 0;
    float yScale = 0;
    private Paint paint = new Paint();

    private float[] xScaledValues, yScaledValues;
    private String[] xDesc;
    private StatisticType statisticType = StatisticType.PER_PERIOD;

    public XYGraphView(Context context) {
        super(context);
        paint.setColor(context.getResources().getColor(R.color.colorPrimary, null));
    }

    public XYGraphView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        textHeight = convertSpToPixel(14, context);
        padding = convertDpToPixel(16, context);
        paint.setColor(context.getResources().getColor(R.color.colorPrimary, null));
        paint.setStrokeWidth(5f);
        paint.setAntiAlias(true);
        paint.setTextSize(textHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (values != null) {
            switch (statisticType) {
                case PER_PERIOD: perPeriod(canvas);break;
                case OVER_TIME: overTime(canvas);break;
            }
        } else {
            super.onDraw(canvas);
        }
    }

    private void perPeriod(Canvas canvas) {
        float barThickness = convertDpToPixel(8, getContext());

        if (xScaledValues.length > 1) {
            float halfBarThickness = barThickness / 2;
            for (int i = 0; i < yScaledValues.length; i++) {
                String text = GeneralUtils.convertTime((int) yScaledValues[i]);
                float textStart = padding;
                if ((width - barThickness) < (xScaledValues[i] + (paint.measureText(text)) / 2)) {
                    textStart = (width - barThickness - paint.measureText(text));
                } else if (i != 0) {
                    textStart = xScaledValues[i] - (paint.measureText(text) / 2);
                }

                String date = xDesc[i];
                canvas.drawText(text, textStart, textHeight, paint);
                canvas.drawText(date, textStart, height, paint);
                canvas.drawRoundRect(xScaledValues[i] - halfBarThickness,
                        height - (textHeight * 2) - (yScaledValues[i] * yScale),
                        xScaledValues[i] + halfBarThickness,
                        height - (textHeight * 2),
                        5, 5, paint);
            }
        }
    }

    private void overTime(Canvas canvas) {
        int amountOfPoints = xScaledValues.length + 1;
        if (amountOfPoints > 1) {

        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    public void setValues(List<StatisticDTO> list, int sampleSize) {
        this.values = list;
        this.xScaledValues = new float[sampleSize];
        this.yScaledValues = new float[sampleSize];
        this.xDesc = new String[sampleSize];
        initPeriodValues();
        invalidate();
    }

    public static float convertSpToPixel(float sp, Context context) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }

    public static float convertDpToPixel(float dp, Context context) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    private void initPeriodValues() {
        long maxValue = 0;
        for (StatisticDTO value : values) {
            if (value.getTime_played() > maxValue) maxValue = value.getTime_played();
        }

        float xStep = (width - padding*4) / (xScaledValues.length - 1);
        yScale = (height - (textHeight * 4)) / (float) (maxValue);

        LocalDate ld = LocalDate.now(ZoneOffset.UTC);
        LocalDate startDate = ld.minusDays(6L);
        LocalDate currDate = startDate;
        for (int i = 0; currDate.isBefore(ld); i++) {
            currDate = startDate.plusDays(i);
            xScaledValues[i] = (xScaledValues.length > 1)
                    ? (xStep * i) + (padding * 2)
                    : xStep;

            yScaledValues[i] = getValueByLocalDate(currDate);

            xDesc[i] = currDate.format(DateTimeFormatter.ofPattern("MM-dd"));
        }
    }

    private void calculateWholePlaytime() {

    }

    private Long getValueByLocalDate(LocalDate localDate) {
        for (StatisticDTO value : values) {
            LocalDate valueDate = LocalDate.parse(value.getTimestamp(), GeneralUtils.DB_TIMESTAMP);

            if (localDate.equals(valueDate)) {
                return value.getTime_played();
            }
        }

        return 0L;
    }
}
