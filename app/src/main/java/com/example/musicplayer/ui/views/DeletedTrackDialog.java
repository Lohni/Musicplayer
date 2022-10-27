package com.example.musicplayer.ui.views;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.musicplayer.R;
import com.example.musicplayer.adapter.TrackSelectionAdapter;
import com.example.musicplayer.database.entity.Track;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DeletedTrackDialog extends DialogFragment {
    private final ArrayList<Track> deletedTracks = new ArrayList<>();
    private OnRestoreListener onRestore;
    private boolean scrolling;

    public interface OnRestoreListener {
        void onRestore(List<Track> tracksToRestore);
    }

    public DeletedTrackDialog(List<Track> deletedTracks) {
        this.deletedTracks.addAll(deletedTracks);
    }

    @Override
    public int getTheme() {
        return R.style.DialogAnim;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_deleted_tracks_dialog, container, true);

        View close = view.findViewById(R.id.deleted_track_dialog_close);
        ConstraintLayout header = view.findViewById(R.id.deleted_track_dialog);
        MaterialButton restore = view.findViewById(R.id.deleted_track_dialog_restore);
        RecyclerView deletedList = view.findViewById(R.id.deleted_track_dialog_list);

        close.setOnClickListener((v) -> dismiss());

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        TrackSelectionAdapter mAdapter = new TrackSelectionAdapter(getContext(), deletedTracks);
        mAdapter.setOnTrackSelectedListener((position) -> {
            if (mAdapter.getSelectedCount() > 0) {
                restore.setText("Restore " + mAdapter.getSelectedCount());
                restore.setTextColor(getContext().getColor(R.color.colorPrimary));
                restore.setBackground(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.shuffle_ripple, null));
            } else {
                restore.setText("Restore");
                restore.setTextColor(getContext().getColor(R.color.colorSurfaceVariant));
                restore.setBackground(null);
            }
        });

        deletedList.setHasFixedSize(true);
        deletedList.setLayoutManager(llm);
        deletedList.setAdapter(mAdapter);

        deletedList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                int surface = getContext().getResources().getColor(R.color.colorSurface, null);
                int level2 = getContext().getResources().getColor(R.color.colorSurfaceLevel2, null);

                if (!scrolling && llm.findFirstCompletelyVisibleItemPosition() > 0) {
                    scrolling = true;
                    ValueAnimator valueAnimatorToolbar = ObjectAnimator.ofArgb(header, "backgroundColor", surface, level2);
                    valueAnimatorToolbar.setDuration(500);
                    valueAnimatorToolbar.setEvaluator(new ArgbEvaluator());

                    ValueAnimator valueAnimatorStatusBar = ObjectAnimator.ofArgb(getDialog().getWindow(), "statusBarColor", surface, level2);
                    valueAnimatorStatusBar.setDuration(500);
                    valueAnimatorStatusBar.setEvaluator(new ArgbEvaluator());

                    valueAnimatorToolbar.start();
                    valueAnimatorStatusBar.start();
                } else if (scrolling && llm.findFirstCompletelyVisibleItemPosition() == 0) {
                    scrolling = false;
                    ValueAnimator valueAnimatorToolbar = ObjectAnimator.ofArgb(header, "backgroundColor", level2, surface);
                    valueAnimatorToolbar.setDuration(300);
                    valueAnimatorToolbar.setEvaluator(new ArgbEvaluator());

                    ValueAnimator valueAnimatorStatusBar = ObjectAnimator.ofArgb(getDialog().getWindow(), "statusBarColor", level2, surface);
                    valueAnimatorStatusBar.setDuration(300);
                    valueAnimatorStatusBar.setEvaluator(new ArgbEvaluator());

                    valueAnimatorToolbar.start();
                    valueAnimatorStatusBar.start();
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        restore.setOnClickListener((v) -> {
            if (onRestore != null && mAdapter.getSelectedCount() > 0) {
                onRestore.onRestore(mAdapter.getSelected());
                dismiss();
            }
        });

        return view;
    }

    public void setOnRestoreClickListener(OnRestoreListener onRestore) {
        this.onRestore = onRestore;
    }
}
