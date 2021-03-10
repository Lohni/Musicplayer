package com.example.musicplayer.ui.songlist;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
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

    private ListView listView;
    private View view;
    private Map<String,Integer> mapIndex;
    private SongListAdapter songListAdapter;

    private ArrayList<MusicResolver> songList = new ArrayList<>();
    private SongListInterface songListInterface;
    private NavigationControlInterface navigationControlInterface;

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
        listView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(requireContext(),R.anim.layout_animation_fall_down));
        permission();
        fetchSongList();

        Collections.sort(songList, new Comparator<MusicResolver>(){
            public int compare(MusicResolver a, MusicResolver b){
                return a.getTitle().compareToIgnoreCase(b.getTitle());
            }
        });

        songListAdapter = new SongListAdapter(getContext(),songList);
        listView.setAdapter(songListAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                songListInterface.OnSongSelectedListener(i);
            }
        });

        songListInterface.OnSongListCreatedListener(songList);
        getIndexList();
        displayIndex(container, getResources().getDisplayMetrics().heightPixels);
        return view;
    }

    private void permission(){
        //if (Build.VERSION.SDK_INT >= 23) {
        //Check whether your app has access to the READ permission//
        if (checkPermission()) {
            //If your app has access to the device’s storage, then print the following message to Android Studio’s Logcat//
            Log.e("permission", "Permission already granted.");
        } else {
            //If your app doesn’t have permission to access external storage, then call requestPermission//
            requestPermission();
        }
        //}
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
    }

    private boolean checkPermission() {
        //Check for READ_EXTERNAL_STORAGE access, using ContextCompat.checkSelfPermission()//
        int result = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
        //If the app does have this permission, then return true//
        //If the app doesn’t have this permission, then return false//
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    private void displayIndex(ViewGroup container,int abs_heigt) {
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
            textView.setText(index);
            textView.setGravity(Gravity.CENTER);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TextView selectedIndex = (TextView) view;
                    listView.setSelection(mapIndex.get(selectedIndex.getText()));
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

}
