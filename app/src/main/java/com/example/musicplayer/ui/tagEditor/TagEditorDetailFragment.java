package com.example.musicplayer.ui.tagEditor;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.musicplayer.R;
import com.example.musicplayer.entities.MusicResolver;
import com.example.musicplayer.utils.ID3Editor;
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

        getValues();

        return view;
    }

    public void setTrack(MusicResolver track) {
        this.track = track;

    }

    private void setValues() {

    }

    private void getValues() {
        ContentResolver contentResolver = requireContext().getContentResolver();
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, track.getId());
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(requireContext(), trackUri);
        readBytes(trackUri);


/*
        title.setText(track.getTitle());
        artist.setText(track.getArtist());
        album.setText(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
        genre.setText(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE));
        date.setText(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE));
        trackNr.setText(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER));
        composer.setText(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER));

 */
    }

    private void init(Uri trackUri) {

    }

    private void readBytes(Uri uri) {
        ID3Editor tag = new ID3Editor(uri, requireContext());
    }
}