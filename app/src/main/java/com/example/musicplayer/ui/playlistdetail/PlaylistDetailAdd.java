package com.example.musicplayer.ui.playlistdetail;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.musicplayer.R;
import com.example.musicplayer.adapter.TrackSelectionAdapter;
import com.example.musicplayer.entities.MusicResolver;
import com.example.musicplayer.ui.playlist.PlaylistInterface;
import com.google.android.material.datepicker.MaterialTextInputPicker;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class PlaylistDetailAdd extends Fragment {
    private static final int PERMISSION_REQUEST_CODE = 0x03 ;

    private ListView selection;
    private EditText search;
    private ArrayList<MusicResolver> trackList;
    private TrackSelectionAdapter mAdapter;
    private ExtendedFloatingActionButton confirm;
    private PlaylistInterface playlistInterface;
    private boolean isFiltered=false;
    private int selectedCount = 0;

    public PlaylistDetailAdd() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            playlistInterface = (PlaylistInterface) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + "must implement SongListInterface");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_playlist_detail_add, container, false);
        selection = view.findViewById(R.id.selection_list);
        search = view.findViewById(R.id.playlist_add_search);
        confirm = view.findViewById(R.id.playlist_detail_add_confirm);
        confirm.setVisibility(View.INVISIBLE);
        permission();
        trackList = new ArrayList<>();

        selection.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(requireContext(),R.anim.layout_animation_fall_down));
        PlaylistSelectorViewModel model = new ViewModelProvider(this).get(PlaylistSelectorViewModel.class);
        model.getTracklist().observe(getViewLifecycleOwner(),trackList ->{
            this.trackList=trackList;
            Collections.sort(this.trackList, new Comparator<MusicResolver>(){
                public int compare(MusicResolver a, MusicResolver b){
                    return a.getTitle().compareToIgnoreCase(b.getTitle());
                }
            });

            if(mAdapter==null){
                mAdapter=new TrackSelectionAdapter(requireContext(),trackList);
                selection.setAdapter(mAdapter);
            } else {
                mAdapter.notifyDataSetChanged();
            }
            playlistInterface.OnTracklistLoadedListener();
        });

        selection.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int position;
                if (isFiltered)position = mAdapter.getOriginalPosition(i);
                else position = i;
                MusicResolver track = mAdapter.getItem(position);
                if(track.isSelected()){
                    track.setSelected(false);
                    selectedCount-=1;
                }
                else {
                    track.setSelected(true);
                    selectedCount += 1;
                }
                trackList.set(position,track);
                mAdapter.notifyDataSetChanged();
                if (selectedCount > 0) {
                    if (selectedCount == 1){
                        confirm.setVisibility(View.VISIBLE);
                        confirm.setText("ADD " + selectedCount + " SONG");
                    } else confirm.setText("ADD " + selectedCount + " SONGS");
                } else confirm.setVisibility(View.INVISIBLE);
            }
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() != 0) isFiltered=true;
                else isFiltered=false;
                mAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playlistInterface.OnAddSongsListener();
            }
        });

        return view;
    }

    private void permission(){
        //Check whether your app has access to the READ permission//
        if (checkPermission()) {
            //If your app has access to the device’s storage, then print the following message to Android Studio’s Logcat//
            Log.e("permission", "Permission already granted.");
        } else {
            //If your app doesn’t have permission to access external storage, then call requestPermission//
            requestPermission();
        }
    }

    private boolean checkPermission() {
        //Check for READ_EXTERNAL_STORAGE access, using ContextCompat.checkSelfPermission()
        int result = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
        //If the app does have this permission, then return true//
        //If the app doesn’t have this permission, then return false//
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    public ArrayList getSelected(){
        ArrayList<MusicResolver> selected_tracks = new ArrayList<>();
        for(int i=0;i<trackList.size();i++){
            if (trackList.get(i).isSelected())selected_tracks.add(trackList.get(i));
        }
        return selected_tracks;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
