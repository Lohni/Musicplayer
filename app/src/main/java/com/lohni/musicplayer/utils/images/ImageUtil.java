package com.lohni.musicplayer.utils.images;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.lohni.musicplayer.R;

import java.net.ConnectException;
import java.util.List;
import java.util.Optional;

public class ImageUtil {
    public static RoundedBitmapDrawable roundCorners(Bitmap drawable, Resources resources) {
        RoundedBitmapDrawable rbd = RoundedBitmapDrawableFactory.create(resources, drawable);
        rbd.setCornerRadius(convertDpToPixel(8, resources));
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
        return getBitmapFromDrawable(context, drawable);
    }

    public static Bitmap getBitmapFromDrawable(Context context, Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static Drawable getDrawableFromVectorDrawable(Context context, int resId) {
        return ContextCompat.getDrawable(context, resId);
    }

    public static Optional<Drawable> createBitmapCollection(List<Bitmap> coverList, Context context) {
        Bitmap customCover = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);
        Drawable icon = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_album_black_24dp, null);
        icon.setTintList(ContextCompat.getColorStateList(context, R.color.colorOnSurfaceVariant));
        Canvas customCoverCanvas = new Canvas(customCover);
        icon.draw(customCoverCanvas);

        if (coverList.size() > 0) {
            int[] x = {0, 512, 0, 512};
            int[] y = {0, 0, 512, 512};

            if (coverList.size() > 1) {
                for (int i = 0; i < 4 - coverList.size(); i++) {
                    coverList.add(customCover);
                }
            }

            Bitmap preview = Bitmap.createBitmap(1024, 1024, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(preview);

            int i = 0;
            for (Bitmap cover : coverList) {
                int scale = coverList.size() == 1 ? 1024 : 512;
                canvas.drawBitmap(Bitmap.createScaledBitmap(cover, scale, scale, false), x[i], y[i], null);
                i++;
            }

            return Optional.of(roundCorners(preview, context.getResources()));
        }
        return Optional.empty();
    }

    public static double calSimilarity(Bitmap bmp1, Bitmap bmp2) {
        bmp1 = ThumbnailUtils.extractThumbnail(toGrayscale(bmp1), 32, 32);
        bmp2 = ThumbnailUtils.extractThumbnail(toGrayscale(bmp2), 32, 32);

        int[] pixels1 = new int[bmp1.getWidth() * bmp1.getHeight()];
        int[] pixels2 = new int[bmp2.getWidth() * bmp2.getHeight()];
        bmp1.getPixels(pixels1, 0, bmp1.getWidth(), 0, 0, bmp1.getWidth(), bmp1.getHeight());
        bmp2.getPixels(pixels2, 0, bmp2.getWidth(), 0, 0, bmp2.getWidth(), bmp2.getHeight());

        int averageColor1 = getAverageOfPixelArray(pixels1);
        int averageColor2 = getAverageOfPixelArray(pixels2);

        int[] weights1 = getPixelDeviateWeightsArray(pixels1, averageColor1);
        int[] weights2 = getPixelDeviateWeightsArray(pixels2, averageColor2);

        return calSimilarity(getHammingDistance(weights1, weights2));
    }

    private static double calSimilarity(int hammingDistance) {
        int length = 32 * 32;
        double similarity = (length - hammingDistance) / (double) length;
        return Math.pow(similarity, 2);
    }

    private static Bitmap toGrayscale(Bitmap bmpOriginal) {
        int height = bmpOriginal.getHeight();
        int width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    private static int getAverageOfPixelArray(int[] pixels) {
        long sumRed = 0;
        for (int pixel : pixels) {
            sumRed += Color.red(pixel);
        }
        return (int) (sumRed / pixels.length);
    }

    private static int[] getPixelDeviateWeightsArray(int[] pixels, final int averageColor) {
        int[] dest = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            dest[i] = Color.red(pixels[i]) - averageColor > 0 ? 1 : 0;
        }
        return dest;
    }

    private static int getHammingDistance(int[] a, int[] b) {
        int sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] == b[i] ? 0 : 1;
        }
        return sum;
    }
}
