package com.example.musicplayer.utils.images;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.util.List;

import androidx.core.graphics.ColorUtils;
import androidx.palette.graphics.Palette;
//https://github.com/mkaflowski/Media-Style-Palette/blob/214c76f38ae4eac8cfb61c112e49cabb0ef3064d/library/src/main/java/mkaflowski/mediastylepalette/MediaNotificationProcessor.java#L38

public class BitmapColorExtractor {
    private static final float POPULATION_FRACTION_FOR_MORE_VIBRANT = 1.0f;
    private static final float MIN_SATURATION_WHEN_DECIDING = 0.19f;
    private static final double MINIMUM_IMAGE_FRACTION = 0.002;
    private static final int LIGHTNESS_TEXT_DIFFERENCE_LIGHT = 20;
    private static final int LIGHTNESS_TEXT_DIFFERENCE_DARK = -10;
    private static final float POPULATION_FRACTION_FOR_DOMINANT = 0.01f;
    private int RESIZE_BITMAP_AREA = 150 * 150;
    private static final float BLACK_MAX_LIGHTNESS = 0.08f;
    private static final float WHITE_MIN_LIGHTNESS = 0.90f;
    private static final float POPULATION_FRACTION_FOR_WHITE_OR_BLACK = 2.5f;

    private int backgroundColor, foregroundColor, primaryTextColor;
    private float[] filteredBackgroundHsl;

    private Drawable drawable;

    public BitmapColorExtractor(Context context, Bitmap bitmap) {
        backgroundColor = 0;
        if (bitmap != null) {
            drawable = new BitmapDrawable(context.getResources(), bitmap);
            getColors();
        }
    }

    public BitmapColorExtractor(Context context, Bitmap bitmap, int backgroundColor) {
        this.backgroundColor = backgroundColor;
        if (bitmap != null) {
            drawable = new BitmapDrawable(context.getResources(), bitmap);
            getColors();
        }
    }

    private void getColors() {
        if (drawable != null) {
            int width = drawable.getIntrinsicWidth();
            int height = drawable.getIntrinsicHeight();
            int imageArea = width * height;

            if (imageArea > RESIZE_BITMAP_AREA) {
                double factor = Math.sqrt((float) RESIZE_BITMAP_AREA / imageArea);
                int resize_width = (int) (factor * width);
                int resize_height = (int) (factor * height);

                Bitmap bitmap = Bitmap.createBitmap(resize_width, resize_height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, resize_width, resize_height);
                drawable.draw(canvas);

                Palette.Builder paletteBuilder = Palette.from(bitmap)
                        .setRegion(0, 0, resize_width / 2, resize_height)
                        .clearFilters()
                        .resizeBitmapArea(RESIZE_BITMAP_AREA);
                Palette palette = paletteBuilder.generate();

                if (backgroundColor == 0)getBackgroundColor(palette);

                float textColorStartWidthFraction = 0.4f;
                paletteBuilder.setRegion((int) (bitmap.getWidth() * textColorStartWidthFraction), 0,
                        bitmap.getWidth(),
                        bitmap.getHeight());
                if (filteredBackgroundHsl != null) {
                    paletteBuilder.addFilter((rgb, hsl) -> {
                        // at least 10 degrees hue difference
                        float diff = Math.abs(hsl[0] - filteredBackgroundHsl[0]);
                        return diff > 10 && diff < 350;
                    });
                }
                paletteBuilder.addFilter((rgb, hsl) -> hsl[2] > BLACK_MAX_LIGHTNESS && hsl[2] < WHITE_MIN_LIGHTNESS);
                palette = paletteBuilder.generate();
                selectForegroundColor(palette);
                ensureColors();
            }
        }
    }

    private void getBackgroundColor(Palette palette) {
        Palette.Swatch dominantSwatch = palette.getDominantSwatch();
        if (dominantSwatch != null) {
            float[] hsl = dominantSwatch.getHsl();
            if (hsl[2] > BLACK_MAX_LIGHTNESS && hsl[2] < WHITE_MIN_LIGHTNESS) {
                filteredBackgroundHsl = hsl;
                backgroundColor = dominantSwatch.getRgb();
            } else {
                List<Palette.Swatch> swatchList = palette.getSwatches();
                float highestNonWhitePopulation = -1;
                Palette.Swatch second = null;
                for (Palette.Swatch swatch : swatchList) {
                    hsl = swatch.getHsl();
                    if (swatch != dominantSwatch
                            && swatch.getPopulation() > highestNonWhitePopulation
                            && hsl[2] > BLACK_MAX_LIGHTNESS && hsl[2] < WHITE_MIN_LIGHTNESS) {
                        second = swatch;
                        highestNonWhitePopulation = swatch.getPopulation();
                    }
                }
                if (second == null || dominantSwatch.getPopulation() / highestNonWhitePopulation
                        > POPULATION_FRACTION_FOR_WHITE_OR_BLACK) {
                    filteredBackgroundHsl = null;
                    backgroundColor = dominantSwatch.getRgb();
                } else {
                    filteredBackgroundHsl = second.getHsl();
                    backgroundColor = second.getRgb();
                }
            }
        } else {
            filteredBackgroundHsl = null;
            backgroundColor = Color.WHITE;
        }
    }

    private void selectForegroundColor(Palette palette) {
        if (ColorUtils.calculateLuminance(backgroundColor) > 0.5f) {
            foregroundColor = selectForegroundColorForSwatches(palette.getDarkVibrantSwatch(),
                    palette.getVibrantSwatch(),
                    palette.getDarkMutedSwatch(),
                    palette.getMutedSwatch(),
                    palette.getDominantSwatch(),
                    Color.BLACK);
        } else {
            foregroundColor = selectForegroundColorForSwatches(palette.getLightVibrantSwatch(),
                    palette.getVibrantSwatch(),
                    palette.getLightMutedSwatch(),
                    palette.getMutedSwatch(),
                    palette.getDominantSwatch(),
                    Color.WHITE);
        }
    }

    private int selectForegroundColorForSwatches(Palette.Swatch moreVibrant,
                                                 Palette.Swatch vibrant, Palette.Swatch moreMutedSwatch, Palette.Swatch mutedSwatch,
                                                 Palette.Swatch dominantSwatch, int fallbackColor) {
        Palette.Swatch coloredCandidate = selectVibrantCandidate(moreVibrant, vibrant);
        if (coloredCandidate == null) {
            coloredCandidate = selectMutedCandidate(mutedSwatch, moreMutedSwatch);
        }
        if (coloredCandidate != null) {
            if (dominantSwatch == coloredCandidate) {
                return coloredCandidate.getRgb();
            } else if ((float) coloredCandidate.getPopulation() / dominantSwatch.getPopulation()
                    < POPULATION_FRACTION_FOR_DOMINANT
                    && dominantSwatch.getHsl()[1] > MIN_SATURATION_WHEN_DECIDING) {
                return dominantSwatch.getRgb();
            } else {
                return coloredCandidate.getRgb();
            }
        } else if (hasEnoughPopulation(dominantSwatch)) {
            return dominantSwatch.getRgb();
        } else {
            return fallbackColor;
        }
    }

    private Palette.Swatch selectMutedCandidate(Palette.Swatch first,
                                                Palette.Swatch second) {
        boolean firstValid = hasEnoughPopulation(first);
        boolean secondValid = hasEnoughPopulation(second);
        if (firstValid && secondValid) {
            float firstSaturation = first.getHsl()[1];
            float secondSaturation = second.getHsl()[1];
            float populationFraction = first.getPopulation() / (float) second.getPopulation();
            if (firstSaturation * populationFraction > secondSaturation) {
                return first;
            } else {
                return second;
            }
        } else if (firstValid) {
            return first;
        } else if (secondValid) {
            return second;
        }
        return null;
    }

    private Palette.Swatch selectVibrantCandidate(Palette.Swatch first, Palette.Swatch second) {
        boolean firstValid = hasEnoughPopulation(first);
        boolean secondValid = hasEnoughPopulation(second);
        if (firstValid && secondValid) {
            int firstPopulation = first.getPopulation();
            int secondPopulation = second.getPopulation();
            if (firstPopulation / (float) secondPopulation
                    < POPULATION_FRACTION_FOR_MORE_VIBRANT) {
                return second;
            } else {
                return first;
            }
        } else if (firstValid) {
            return first;
        } else if (secondValid) {
            return second;
        }
        return null;
    }

    private boolean hasEnoughPopulation(Palette.Swatch swatch) {
        // We want a fraction that is at least 1% of the image
        return swatch != null
                && (swatch.getPopulation() / (float) RESIZE_BITMAP_AREA > MINIMUM_IMAGE_FRACTION);
    }

    private void ensureColors() {
        double backLum = ColorUtils.calculateLuminance(backgroundColor);
        double textLum = ColorUtils.calculateLuminance(foregroundColor);
        double contrast = ColorUtils.calculateContrast(foregroundColor,
                backgroundColor);
        // We only respect the given colors if worst case Black or White still has
        // contrast
        boolean backgroundLight = backLum > textLum
                && ColorUtils.calculateContrast(backgroundColor, Color.BLACK) >= 4.5f
                || backLum <= textLum
                && ColorUtils.calculateContrast(backgroundColor, Color.WHITE) < 4.5f;
        if (contrast < 4.5f) {
            if (backgroundLight) {
                int secondaryTextColor = findContrastColor(
                        foregroundColor,
                        backgroundColor,
                        true ,
                        4.5f);
                primaryTextColor = changeColorLightness(
                        secondaryTextColor, -LIGHTNESS_TEXT_DIFFERENCE_LIGHT);
            } else {
                int secondaryTextColor =
                        findContrastColorAgainstDark(
                                foregroundColor,
                                backgroundColor,
                                true,
                                4.5f);
                primaryTextColor = changeColorLightness(
                        secondaryTextColor, -LIGHTNESS_TEXT_DIFFERENCE_DARK);
            }
        } else {
            primaryTextColor = foregroundColor;
            int secondaryTextColor = changeColorLightness(
                    primaryTextColor, backgroundLight ? LIGHTNESS_TEXT_DIFFERENCE_LIGHT
                            : LIGHTNESS_TEXT_DIFFERENCE_DARK);
            if (ColorUtils.calculateContrast(secondaryTextColor,
                    backgroundColor) < 4.5f) {
                // oh well the secondary is not good enough
                if (backgroundLight) {
                    secondaryTextColor = findContrastColor(
                            secondaryTextColor,
                            backgroundColor,
                            true,
                            4.5f);
                } else {
                    secondaryTextColor
                            = findContrastColorAgainstDark(
                            secondaryTextColor,
                            backgroundColor,
                            true,
                            4.5f);
                }
                primaryTextColor = changeColorLightness(
                        secondaryTextColor, backgroundLight
                                ? -LIGHTNESS_TEXT_DIFFERENCE_LIGHT
                                : -LIGHTNESS_TEXT_DIFFERENCE_DARK);
            }
        }

        //actionBarColor = NotificationColorUtil.resolveActionBarColor(context,
        //        backgroundColor);
    }

    public int findContrastColor(int color, int other, boolean findFg, double minRatio) {
        int fg = findFg ? color : other;
        int bg = findFg ? other : color;
        if (ColorUtils.calculateContrast(fg, bg) >= minRatio) {
            return color;
        }

        double[] lab = new double[3];
        ColorUtils.colorToLAB(findFg ? fg : bg, lab);

        double low = 0, high = lab[0];
        final double a = lab[1], b = lab[2];
        for (int i = 0; i < 15 && high - low > 0.00001; i++) {
            final double l = (low + high) / 2;
            if (findFg) {
                fg = ColorUtils.LABToColor(l, a, b);
            } else {
                bg = ColorUtils.LABToColor(l, a, b);
            }
            if (ColorUtils.calculateContrast(fg, bg) > minRatio) {
                low = l;
            } else {
                high = l;
            }
        }
        return ColorUtils.LABToColor(low, a, b);
    }

    public static int findContrastColorAgainstDark(int color, int other, boolean findFg,
                                                   double minRatio) {
        int fg = findFg ? color : other;
        int bg = findFg ? other : color;
        if (ColorUtils.calculateContrast(fg, bg) >= minRatio) {
            return color;
        }

        float[] hsl = new float[3];
        ColorUtils.colorToHSL(findFg ? fg : bg, hsl);

        float low = hsl[2], high = 1;
        for (int i = 0; i < 15 && high - low > 0.00001; i++) {
            final float l = (low + high) / 2;
            hsl[2] = l;
            if (findFg) {
                fg = ColorUtils.HSLToColor(hsl);
            } else {
                bg = ColorUtils.HSLToColor(hsl);
            }
            if (ColorUtils.calculateContrast(fg, bg) > minRatio) {
                high = l;
            } else {
                low = l;
            }
        }
        return findFg ? fg : bg;
    }

    public static int changeColorLightness(int baseColor, int amount) {
        final double[] result = new double[3];
        ColorUtils.colorToLAB(baseColor, result);
        result[0] = Math.max(Math.min(100, result[0] + amount), 0);
        return ColorUtils.LABToColor(result[0], result[1], result[2]);
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public int getPrimaryTextColor() {
        return primaryTextColor;
    }

    public int getForegroundColor() {
        return foregroundColor;
    }
}
