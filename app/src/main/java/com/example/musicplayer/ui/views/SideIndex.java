package com.example.musicplayer.ui.views;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.musicplayer.R;
import com.example.musicplayer.database.dto.TrackDTO;
import com.example.musicplayer.database.entity.Track;
import com.example.musicplayer.utils.images.ImageTransformUtil;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

public class SideIndex {

    private LinearLayout sideIndex;
    private Context context;
    private FrameLayout indexZoomHolder;
    private TextView indexZoom;
    private LinearLayoutManager listViewManager;
    private Map<String, Integer> mapIndex;

    public SideIndex(Context context, LinearLayout sideIndex, FrameLayout indexZoomHolder, TextView indexZoom, LinearLayoutManager listViewManager) {
        this.sideIndex = sideIndex;
        this.context = context;
        this.indexZoomHolder = indexZoomHolder;
        this.indexZoomHolder.bringToFront();
        this.indexZoomHolder.setVisibility(View.GONE);
        this.indexZoom = indexZoom;
        this.listViewManager = listViewManager;
    }

    public void displayIndex() {
        sideIndex.removeAllViews();

        LinearLayout indexLayout = new LinearLayout(context) {
            @Override
            public boolean dispatchTouchEvent(MotionEvent ev) {
                int y = Math.round(ev.getY());
                for (int i = 0; i < getChildCount(); i++) {
                    TextView child = (TextView) getChildAt(i);
                    if (y > child.getTop() && y < child.getBottom()) {
                        child.callOnClick();

                        if (ev.getAction() == MotionEvent.ACTION_UP) {
                            indexZoomHolder.setVisibility(View.GONE);
                        }
                    }
                }
                if (!(y > getTop() && y < getBottom())) {
                    indexZoomHolder.setVisibility(View.GONE);
                }
                return true;
            }
        };
        indexLayout.setOrientation(LinearLayout.VERTICAL);

        int div = (int) (27);
        float width = ImageTransformUtil.convertPixelToSp(sideIndex.getWidth() - 9, context.getResources());

        float textsize = ImageTransformUtil.convertPixelToSp(sideIndex.getHeight() / (div), context.getResources());
        textsize = Math.min(textsize, width);

        MaterialTextView textView;
        List<String> indexList = new ArrayList<>(mapIndex.keySet());
        for (String index : indexList) {
            textView = new MaterialTextView(context);
            textView.setTextSize(textsize);
            textView.setTextColor(ContextCompat.getColor(context, R.color.colorOnBackground));
            textView.setText(index);
            textView.setFocusable(false);
            textView.setGravity(Gravity.CENTER);
            textView.setPadding(0, 0, 8, 0);
            textView.setOnClickListener(view -> {
                indexZoomHolder.setVisibility(View.VISIBLE);
                TextView selectedIndex = (TextView) view;
                indexZoom.setText(selectedIndex.getText().subSequence(0, 1));
                listViewManager.scrollToPositionWithOffset(mapIndex.get(selectedIndex.getText()), 0);
            });
            indexLayout.addView(textView);
        }
        sideIndex.addView(indexLayout);
    }

    public SideIndex setIndexList(ArrayList<Track> songList) {
        mapIndex = new LinkedHashMap<>();
        for (int i = 0; i < songList.size(); i++) {
            Track item = songList.get(i);
            String index = item.getTTitle().substring(0, 1);
            Character character = index.charAt(0);

            if (character <= 64 || character >= 123) {
                index = "#";
            } else if (character >= 91 && character <= 96) index = "#";
            else if (character > 96) {
                character = Character.toUpperCase(character);
                index = character.toString();
            }

            mapIndex.putIfAbsent(index, i);
        }
        return this;
    }

    public SideIndex setIndexListDTO(ArrayList<TrackDTO> songList) {
        mapIndex = new LinkedHashMap<>();
        for (int i = 0; i < songList.size(); i++) {
            Track item = songList.get(i).getTrack();
            String index = item.getTTitle().substring(0, 1);
            Character character = index.charAt(0);

            if (character <= 64 || character >= 123) {
                index = "#";
            } else if (character >= 91 && character <= 96) index = "#";
            else if (character > 96) {
                character = Character.toUpperCase(character);
                index = character.toString();
            }

            mapIndex.putIfAbsent(index, i);
        }
        return this;
    }

    public void setVisibilityGone() {
        sideIndex.setVisibility(View.GONE);
    }

    public void setVisible() {
        sideIndex.setVisibility(View.VISIBLE);
    }
}
