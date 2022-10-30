package com.lohni.musicplayer.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author Andreas Lohninger
 */
public class CustomDividerItemDecoration extends RecyclerView.ItemDecoration {
    private final Drawable divider;
    private final int paddingLeft;
    private final int paddingRight;

    public CustomDividerItemDecoration(Context context, int resId) {
        divider = ContextCompat.getDrawable(context, resId);
        paddingLeft = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, context.getResources().getDisplayMetrics());
        paddingRight = paddingLeft;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int childCount = parent.getChildCount() - 1;
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + divider.getIntrinsicHeight();

            divider.setBounds(paddingLeft, top, parent.getWidth() - paddingRight, bottom);
            divider.draw(c);
        }
    }
}
