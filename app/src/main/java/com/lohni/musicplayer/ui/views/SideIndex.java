package com.lohni.musicplayer.ui.views;

import android.content.Context;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.textview.MaterialTextView;
import com.lohni.musicplayer.R;
import com.lohni.musicplayer.database.dto.AlbumTrackDTO;
import com.lohni.musicplayer.database.dto.TrackDTO;
import com.lohni.musicplayer.database.entity.Album;
import com.lohni.musicplayer.database.entity.Track;
import com.lohni.musicplayer.utils.images.ImageUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

public class SideIndex {

    private LinearLayout sideIndex;
    private Context context;
    private FrameLayout indexZoomHolder;
    private TextView indexZoom;
    private LinearLayoutManager listViewManager;
    private Map<String, Integer> mapIndex = new HashMap<>();

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
        float width = ImageUtil.convertPixelToSp(sideIndex.getWidth() - 9, context.getResources());

        float textsize = ImageUtil.convertPixelToSp(sideIndex.getHeight() / (div), context.getResources());
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

    public SideIndex setIndexList(ArrayList<?> dtos) {
        if (!dtos.isEmpty()) {
            Object type = dtos.get(0);
            if (type instanceof Track) {
                fillHashMap(dtos.stream().map(dto -> (Track) dto).map(Track::getTTitle).collect(Collectors.toList()));
            } else if (type instanceof Album) {
                fillHashMap(dtos.stream().map(dto -> (Album) dto).map(Album::getAName).collect(Collectors.toList()));
            } else if (type instanceof TrackDTO) {
                fillHashMap(dtos.stream().map(dto -> (TrackDTO) dto).map(TrackDTO::getTrack).map(Track::getTTitle).collect(Collectors.toList()));
            } else if (type instanceof AlbumTrackDTO) {
                fillHashMap(dtos.stream().map(dto -> (AlbumTrackDTO) dto).map(alb -> alb.album.getAName()).collect(Collectors.toList()));
            }
        }
        return this;
    }

    private void fillHashMap(List<String> values) {
        mapIndex = new LinkedHashMap<>();
        for (int i = 0; i < values.size(); i++) {
            String index = values.get(i).substring(0, 1);
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
    }

    public void setVisibilityGone() {
        sideIndex.setVisibility(View.GONE);
    }

    public void setVisible() {
        sideIndex.setVisibility(View.VISIBLE);
    }
}
