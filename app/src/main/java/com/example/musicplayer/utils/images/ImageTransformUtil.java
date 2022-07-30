package com.example.musicplayer.utils.images;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

public class ImageTransformUtil {
    public static RoundedBitmapDrawable roundCorners(Bitmap drawable, Resources resources) {
        RoundedBitmapDrawable rbd = RoundedBitmapDrawableFactory.create(resources, drawable);
        rbd.setCornerRadius(convertDpToPixel(5, resources));
        return rbd;
    }

    public static float convertDpToPixel(float dp, Resources resources) {
        return dp * ((float) resources.getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}
