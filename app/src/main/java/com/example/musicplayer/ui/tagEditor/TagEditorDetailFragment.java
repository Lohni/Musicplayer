package com.example.musicplayer.ui.tagEditor;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.musicplayer.R;
import com.example.musicplayer.entities.MusicResolver;
import com.google.android.material.textfield.TextInputEditText;

public class TagEditorDetailFragment extends Fragment {

    private MusicResolver track;
    private TextInputEditText title, artist, album, genre, date, trackNr, composer;

    public TagEditorDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tag_editor_detail, container, false);

        title = view.findViewById(R.id.tagEditorDetail_title);
        artist = view.findViewById(R.id.tagEditorDetail_artist);
        album = view.findViewById(R.id.tagEditorDetail_album);
        genre = view.findViewById(R.id.tagEditorDetail_genre);
        date = view.findViewById(R.id.tagEditorDetail_date);
        trackNr = view.findViewById(R.id.tagEditorDetail_tracknr);
        composer = view.findViewById(R.id.tagEditorDetail_composer);

        return view;
    }

    public void setTrack(MusicResolver track){
        this.track = track;
    }

}