package com.lohni.musicplayer.core;

import android.view.View;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class DeleteSnackbar<T> {
    private final ArrayList<T> deleteList = new ArrayList<>();
    private Snackbar snackbar;
    private final int duration;

    private OnUndoListener<T> onUndoListener;
    private OnDissmissedListener<T> onDissmissedListener;

    public interface OnUndoListener<T> {
        void OnUndo(ArrayList<T> toUndo);
    }

    public interface OnDissmissedListener<T> {
        void OnDissmissed(ArrayList<T> toDelete);
    }

    public DeleteSnackbar(int duration) {
        this.duration = duration;
    }

    private void initSnackbar(View anchor) {
        if (snackbar == null) {
            snackbar = Snackbar.make(anchor, "", Snackbar.LENGTH_LONG)
                    .setAction("Undo", view -> {
                        if (onUndoListener != null) onUndoListener.OnUndo(deleteList);
                        deleteList.clear();
                    })
                    .addCallback(new BaseTransientBottomBar.BaseCallback<>() {
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            super.onDismissed(transientBottomBar, event);
                            if (onDissmissedListener != null)
                                onDissmissedListener.OnDissmissed(deleteList);
                            deleteList.clear();
                        }
                    })
                    .setDuration(duration);
        }
    }

    public void addToDelete(T toDelete, View anchor) {
        initSnackbar(anchor);
        deleteList.add(toDelete);
        snackbar.setText(deleteList.size() + " songs will be deleted!");
        snackbar.setDuration(duration);
        snackbar.show();
    }

    public void setOnUndoListener(OnUndoListener<T> onUndoListener) {
        this.onUndoListener = onUndoListener;
    }

    public void setOnDissmissedListener(OnDissmissedListener<T> onDissmissedListener) {
        this.onDissmissedListener = onDissmissedListener;
    }
}
