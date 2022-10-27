package com.example.musicplayer.helper;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import com.example.musicplayer.R;
import com.example.musicplayer.utils.images.ImageTransformUtil;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class DragItemTouchHelper extends ItemTouchHelper.SimpleCallback {
    private List<List> targetLists = null;
    private List<Integer> idsToDelete = new ArrayList<>();
    private List<List> objToDelete;
    private OnItemMovedListener onItemMovedListener;
    private OnItemDeletedListener onItemDeletedListener;
    private OnItemRemovedListener onItemRemovedListener;
    private OnItemRestoredListener onItemRestoredListener;
    private RecyclerView.Adapter adapter;
    private boolean deleteOnSwipe = false, undo = false;

    private final Paint paint;
    private final Bitmap icon;
    private final float iconSize;
    private Snackbar snackbar;
    private final View anchor;
    private final ColorStateList cls, textColor, actionTextColor;

    public interface OnItemMovedListener {
        void onItemMoved(int fromPosition, int toPosition);
    }

    public interface OnItemDeletedListener {
        void onItemsDeleted(List toDelete);
    }

    public interface OnItemRemovedListener {
        void onItemRemoved(int id);
    }

    public interface OnItemRestoredListener {
        void onItemRestored(int id);
    }

    public DragItemTouchHelper(int dragDirs, int swipeDirs, Context context, RecyclerView.Adapter adapter, View anchor) {
        super(dragDirs, swipeDirs);
        this.anchor = anchor;
        this.adapter = adapter;
        paint = new Paint();
        paint.setColor(ContextCompat.getColor(context, R.color.colorSecondary));
        icon = ImageTransformUtil.getBitmapFromVectorDrawable(context, R.drawable.ic_round_delete_24);
        iconSize = ImageTransformUtil.convertDpToPixel(16f, context.getResources());
        cls = ContextCompat.getColorStateList(context, R.color.colorBackground);
        textColor = ContextCompat.getColorStateList(context, R.color.colorOnSurface);
        actionTextColor = ContextCompat.getColorStateList(context, R.color.colorPrimary);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        if (idsToDelete.size() == 0) {
            int fromPosition = viewHolder.getBindingAdapterPosition();
            int toPosition = target.getBindingAdapterPosition();

            if (targetLists != null) {
                for (List<?> list : targetLists) {
                    if (fromPosition < toPosition) {
                        for (int i = fromPosition; i < toPosition; i++) {
                            Collections.swap(list, i, i + 1);
                        }
                    } else {
                        for (int i = fromPosition; i > toPosition; i--) {
                            Collections.swap(list, i, i - 1);
                        }
                    }
                }

                adapter.notifyItemMoved(fromPosition, toPosition);
                if (onItemMovedListener != null) {
                    onItemMovedListener.onItemMoved(fromPosition, toPosition);
                }
                adapter.notifyItemRangeChanged(0, adapter.getItemCount(), "NO_COLOR");
                return true;
            }
        }
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        if (direction == ItemTouchHelper.LEFT) {
            int index = viewHolder.getBindingAdapterPosition();
            adapter.notifyItemRemoved(index);
            idsToDelete.add(index);

            for (int i = 0; i < targetLists.size(); i++) {
                objToDelete.get(i).add(targetLists.get(i).get(index));
                targetLists.get(i).remove(index);
            }

            if (snackbar == null || !snackbar.isShown()) {
                createSnackbar(anchor);
            } else {
                snackbar.setText(idsToDelete.size() + " items will be deleted!");
                snackbar.show();
            }

            if (onItemRemovedListener != null) {
                onItemRemovedListener.onItemRemoved(index);
            }

            adapter.notifyItemRangeChanged(index, adapter.getItemCount() - index, "REFRESH_INDEX");
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (deleteOnSwipe && actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            View itemView = viewHolder.itemView;

            if (dX < 0) {
                float right = itemView.getRight();
                float bottom = itemView.getBottom();
                float top = itemView.getTop();
                float height = itemView.getHeight();

                c.drawRect(right + dX, top, right, bottom, paint);
                c.drawBitmap(icon, right - iconSize - icon.getWidth(), top + (height - icon.getHeight()) / 2, paint);
            }
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && idsToDelete.size() == 0) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }

    public void setTargetList(List<?>... targets) {
        this.targetLists = List.of(targets);
        this.objToDelete = new ArrayList<>();
        for (List l : targetLists) {
            objToDelete.add(new ArrayList<>());
        }
    }

    public void setOnItemMovedListener(OnItemMovedListener onItemMovedListener) {
        this.onItemMovedListener = onItemMovedListener;
    }

    public void setOnItemDeletedListener(OnItemDeletedListener onItemDeletedListener) {
        this.onItemDeletedListener = onItemDeletedListener;
    }

    public void setOnItemRemovedListener(OnItemRemovedListener onItemRemovedListener) {
        this.onItemRemovedListener = onItemRemovedListener;
    }

    public void setOnItemRestoredListener(OnItemRestoredListener onItemRestoredListener) {
        this.onItemRestoredListener = onItemRestoredListener;
    }

    public void setDeleteOnSwipe(boolean deleteOnSwipe) {
        this.deleteOnSwipe = deleteOnSwipe;
    }

    private void createSnackbar(View anchor) {
        snackbar = Snackbar.make(anchor, idsToDelete.size() + " items will be deleted!", Snackbar.LENGTH_LONG)
                .setActionTextColor(actionTextColor)
                .setTextColor(textColor)
                .setAction("Undo", view -> undo = true)
                .addCallback(new BaseTransientBottomBar.BaseCallback<>() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);
                        if (undo) {
                            for (int i = idsToDelete.size() - 1; i >= 0; i--) {
                                for (int j = 0; j < targetLists.size(); j++) {
                                    targetLists.get(j).add(idsToDelete.get(i), objToDelete.get(j).get(i));
                                }
                                adapter.notifyItemInserted(idsToDelete.get(i));
                                if (onItemRestoredListener != null) {
                                    onItemRestoredListener.onItemRestored(idsToDelete.get(i));
                                }
                            }

                            for (List l : objToDelete) {
                                l.clear();
                            }

                            adapter.notifyItemRangeChanged(0, adapter.getItemCount(), "REFRESH_INDEX");
                        } else if (onItemDeletedListener != null) {
                            for (List l : objToDelete) {
                                onItemDeletedListener.onItemsDeleted(l);
                                l.clear();
                            }
                        }
                        idsToDelete.clear();
                        undo = false;
                    }
                }).setDuration(5000);

        snackbar.setBackgroundTintList(cls);
        snackbar.show();
    }
}
