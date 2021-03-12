package com.example.musicplayer.ui.songlist;

import android.Manifest;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.musicplayer.MainActivity;
import com.example.musicplayer.R;
import com.example.musicplayer.adapter.SongListAdapter;
import com.example.musicplayer.entities.MusicResolver;
import com.example.musicplayer.utils.NavigationControlInterface;
import com.example.musicplayer.utils.Permissions;
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
    private View view;
    private Map<String,Integer> mapIndex;
    private SongListAdapter songListAdapter;
    private ConstraintLayout shuffle;

    private ArrayList<MusicResolver> songList = new ArrayList<>();
    private SongListInterface songListInterface;
    private NavigationControlInterface navigationControlInterface;
    private TextView shuffle_size;

    public SongList() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

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
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_song_list, container, false);
        navigationControlInterface.isDrawerEnabledListener(true);
        navigationControlInterface.setHomeAsUpEnabled(false);
        navigationControlInterface.setToolbarTitle("Tracklist");
        listView = view.findViewById(R.id.songList);
        shuffle = view.findViewById(R.id.songlist_shuffle);
        shuffle_size = view.findViewById(R.id.songlist_size);

        listView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(requireContext(),R.anim.layout_animation_fall_down));
        if (Permissions.permission(requireActivity(), this, Manifest.permission.READ_EXTERNAL_STORAGE)){
            fetchSongList();
        }

        songListAdapter = new SongListAdapter(getContext(),songList, songListInterface);
        listView.setAdapter(songListAdapter);
        listView.setHasFixedSize(true);
        listView.setLayoutManager(new LinearLayoutManager(requireContext()));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(requireContext(), R.drawable.recyclerview_divider);
        listView.addItemDecoration(dividerItemDecoration);

        shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                songListInterface.OnSonglistShuffleClickListener();
            }
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
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int albumid = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);

            //add songs to list
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
        Collections.sort(songList, new Comparator<MusicResolver>(){
            public int compare(MusicResolver a, MusicResolver b){
                return a.getTitle().compareToIgnoreCase(b.getTitle());
            }
        });

        songListInterface.OnSongListCreatedListener(songList);
        shuffle_size.setText(songList.size() + " Songs");
    }

    private void displayIndex(int abs_heigt) {
        LinearLayout indexLayout = view.findViewById(R.id.side_index);

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
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextLight));
            textView.setText(index);
            textView.setGravity(Gravity.CENTER);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TextView selectedIndex = (TextView) view;
                    listView.scrollToPosition(mapIndex.get(selectedIndex.getText()));
                }
            });
            indexLayout.addView(textView);
        }
    }

    private void getIndexList() {
        mapIndex = new LinkedHashMap<String, Integer>();
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

        private Drawable divider;
        private int paddingLeft, paddingRight;

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
