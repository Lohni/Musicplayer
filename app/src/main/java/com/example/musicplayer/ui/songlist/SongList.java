package com.example.musicplayer.ui.songlist;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.musicplayer.MainActivity;
import com.example.musicplayer.R;
import com.example.musicplayer.adapter.SongListAdapter;
import com.example.musicplayer.entities.MusicResolver;
import com.example.musicplayer.utils.NavigationControlInterface;
import com.example.musicplayer.utils.Permissions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SongList extends Fragment{
    private static final int PERMISSION_REQUEST_CODE = 0x03;

    private RecyclerView listView;
    private LinearLayoutManager listViewManager;
    private View view;
    private Map<String,Integer> mapIndex;
    private SongListAdapter songListAdapter;
    private MaterialButton shuffle;

    private final ArrayList<MusicResolver> songList = new ArrayList<>();
    private SongListInterface songListInterface;
    private NavigationControlInterface navigationControlInterface;
    private TextView shuffle_size, indexZoom;
    private FrameLayout indexZoomHolder;

    public SongList() {}

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            navigationControlInterface = (NavigationControlInterface) context;
            songListInterface = (SongListInterface) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + "must implement SongListInterface");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_song_list, container, false);
        navigationControlInterface.isDrawerEnabledListener(true);
        navigationControlInterface.setHomeAsUpEnabled(false);
        navigationControlInterface.setToolbarTitle("Tracklist");
        listView = view.findViewById(R.id.songList);
        shuffle = view.findViewById(R.id.songlist_shuffle);
        shuffle_size = view.findViewById(R.id.songlist_size);
        indexZoomHolder = view.findViewById(R.id.songlist_indexzoom_holder);
        indexZoom = view.findViewById(R.id.songlist_indexzoom);

        indexZoomHolder.setVisibility(View.GONE);
        listView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(requireContext(),R.anim.layout_animation_fall_down));
        if (Permissions.permission(requireActivity(), this, Manifest.permission.READ_EXTERNAL_STORAGE)){
            fetchSongList();
        }

        songListAdapter = new SongListAdapter(requireContext(),songList, songListInterface);
        listView.setAdapter(songListAdapter);
        listView.setHasFixedSize(true);
        listViewManager = new LinearLayoutManager(requireContext());
        listView.setLayoutManager(listViewManager);

        shuffle.setOnClickListener(view -> {
            songListInterface.OnSongListCreatedListener(songList);
            songListInterface.OnSonglistShuffleClickListener();
        });

        getIndexList();
        displayIndex(getResources().getDisplayMetrics().heightPixels);
        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && permissions[0].equals(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            fetchSongList();
            songListAdapter.notifyDataSetChanged();
            getIndexList();
            displayIndex(getResources().getDisplayMetrics().heightPixels);
        }
    }

    private void fetchSongList(){
        ContentResolver contentResolver = requireContext().getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = contentResolver.query(musicUri, null, null, null, null);
        if(musicCursor!=null && musicCursor.moveToFirst()){
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int albumid = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);

            do {
                long thisalbumid = musicCursor.getLong(albumid);
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songList.add(new MusicResolver(thisId, thisalbumid, thisArtist, thisTitle));
            }
            while (musicCursor.moveToNext());
        }

        if(musicCursor != null)musicCursor.close();
        Collections.sort(songList, (a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()));

        shuffle_size.setText(songList.size() + " Songs");
    }

    private void displayIndex(int abs_heigt) {
        LinearLayout linearLayout = view.findViewById(R.id.side_index);
        LinearLayout indexLayout = new LinearLayout(requireActivity()){
            @Override
            public boolean dispatchTouchEvent(MotionEvent ev) {
                int x = Math.round(ev.getX());
                int y = Math.round(ev.getY());
                for (int i=0; i<getChildCount(); i++){
                    TextView child = (TextView) getChildAt(i);
                    if(x > child.getLeft() && x < child.getRight() && y > child.getTop() && y < child.getBottom()){
                        child.callOnClick();

                        if(ev.getAction() == MotionEvent.ACTION_UP){
                            indexZoomHolder.setVisibility(View.GONE);
                        }
                    }
                }
                if(!(x > getLeft() && x < getRight() && y > getTop() && y < getBottom())){
                    indexZoomHolder.setVisibility(View.GONE);
                }
                return true;
            }
        };
        indexLayout.setOrientation(LinearLayout.VERTICAL);

        float dip = 61f;
        Resources r = getResources();
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.getDisplayMetrics()
        );
        int div = (int) ((int) 27*1.8);

        int textsize= ((abs_heigt-px)/(div))/((int) getContext().getResources().getDisplayMetrics().scaledDensity);

        MaterialTextView textView;
        List<String> indexList = new ArrayList<String>(mapIndex.keySet());
        for (String index : indexList) {
            textView = new MaterialTextView(requireActivity());
            textView.setTextSize(textsize);
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorDarkOnBackground));
            textView.setText(index);
            textView.setFocusable(false);
            textView.setGravity(Gravity.CENTER);
            textView.setOnClickListener(view -> {
                indexZoomHolder.setVisibility(View.VISIBLE);
                TextView selectedIndex = (TextView) view;
                indexZoom.setText(selectedIndex.getText().subSequence(0,1));
                listViewManager.scrollToPositionWithOffset(mapIndex.get(selectedIndex.getText()), 0);
            });
            indexLayout.addView(textView);
        }
        linearLayout.addView(indexLayout);
    }

    private void getIndexList() {
        mapIndex = new LinkedHashMap<>();
        for (int i = 0; i < songList.size(); i++) {
            MusicResolver item = songList.get(i);
            String index = item.getTitle().substring(0,1);
            Character character = index.charAt(0);

            if(character <=64 || character >=123){
                index = "#";
            } else if(character >= 91 && character <= 96)index = "#";
            else if(character >96){
                character = Character.toUpperCase(character);
                index = character.toString();
            }

            if (mapIndex.get(index) == null)
                mapIndex.put(index, i);
        }
    }

    private class DividerItemDecoration extends RecyclerView.ItemDecoration{

        private final Drawable divider;
        private final int paddingLeft, paddingRight;

        public DividerItemDecoration(Context context, int resId) {
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

}
