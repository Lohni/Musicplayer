package com.example.musicplayer.ui.tagEditor;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.musicplayer.R;
import com.example.musicplayer.entities.MusicResolver;
import com.example.musicplayer.utils.ID3Editor;
import com.example.musicplayer.utils.ID3EditorInterface;
import com.example.musicplayer.utils.TagResolver;
import com.google.android.material.textfield.TextInputEditText;

public class TagEditorDetailFragment extends Fragment{

    private long trackID;
    private TagResolver track;
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

        getValues();

        return view;
    }

    public void setTrack(MusicResolver track) {
        trackID = track.getId();

    }

    private void setValues(TagResolver track) {
        title.setText(track.getTitle());
        artist.setText(track.getArtist());
        album.setText(track.getAlbum());
        genre.setText(track.getGenre());
        date.setText(track.getYear());
        trackNr.setText(track.getTrackid());
        composer.setText(track.getComposer());
    }

    private void getValues() {
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, trackID);
        readBytes(trackUri);
    }

    private void init(Uri trackUri) {
    }

    private void readBytes(Uri uri) {
        ID3Editor tag = new ID3Editor(uri, requireContext(), new ID3EditorInterface() {
            @Override
            public void onDataLoadedListener(TagResolver tagResolver) {
                setValues(tagResolver);
            }
        });
    }
}