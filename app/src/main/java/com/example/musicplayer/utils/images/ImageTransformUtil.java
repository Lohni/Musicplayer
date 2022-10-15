package com.example.musicplayer.utils.images;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import androidx.core.content.ContextCompat;
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

    public static float convertPixelToDp(float pixel, Resources resources) {
        return pixel / ((float) resources.getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static float convertPixelToSp(float pixel, Resources resources) {
        return pixel / (resources.getDisplayMetrics().scaledDensity);
    }

    public static float convertSpToPixel(float sp, Context context) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static Drawable getDrawableFromVectorDrawable(Context context, int resId) {
        return ContextCompat.getDrawable(context, resId);
    }
}
