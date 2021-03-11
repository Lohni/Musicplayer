package com.example.musicplayer.ui.tagEditor;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.musicplayer.R;
import com.example.musicplayer.adapter.EqualizerViewPagerAdapter;
import com.example.musicplayer.entities.MusicResolver;
import com.example.musicplayer.utils.NavigationControlInterface;
import com.example.musicplayer.utils.Permissions;
import com.example.musicplayer.utils.tageditor.ID3Editor;
import com.example.musicplayer.utils.tageditor.ID3EditorInterface;
import com.example.musicplayer.utils.tageditor.ID3V4APICFrame;
import com.example.musicplayer.utils.tageditor.ID3V4Frame;
import com.example.musicplayer.utils.tageditor.TagResolver;
import com.example.musicplayer.utils.tageditor.TagWriter;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class TagEditorDetailFragment extends Fragment{
    private static final int PERMISSION_REQUEST_CODE = 0x03;
    private long trackID;
    private ID3Editor id3Editor;
    private TextInputEditText title, artist, album, genre, date, trackNr, composer;
    private ID3V4Frame titleFrame, artistFrame, albumFrame, genreFrame, yearFrame, tracknrFrame, composerFrame;
    private ID3V4APICFrame apicFrame;
    private ImageView tagImageView;

    private NavigationControlInterface navigationControlInterface;

    public TagEditorDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.tageditor_confirm, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_tagEditor_confirm){
            writeTag();
            requireActivity().onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            navigationControlInterface = (NavigationControlInterface) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + "must implement NavigationControlInterface");
        }
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
        tagImageView = view.findViewById(R.id.tagEditorDetail_cover);

        tagImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImageFromGallery(requireContext());
            }
        });

        navigationControlInterface.isDrawerEnabledListener(false);
        navigationControlInterface.setHomeAsUpEnabled(true);
        navigationControlInterface.setHomeAsUpIndicator(R.drawable.ic_clear_black_24dp);

        if (Permissions.permission(requireActivity(), this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            getValues();
        }

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && permissions[0].equals(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            getValues();
        }
    }

    public void setTrack(MusicResolver track) {
        trackID = track.getId();
    }

    private TagResolver getTagResolver(){
        TagResolver track = id3Editor.getTrackData();
        titleFrame.setFrameContent(title.getText().toString());
        track.setFrame(TagResolver.FRAME_TITLE, titleFrame);
        artistFrame.setFrameContent(artist.getText().toString());
        track.setFrame(TagResolver.FRAME_ARTIST, artistFrame);

        albumFrame.setFrameContent(album.getText().toString());
        track.setFrame(TagResolver.FRAME_ALBUM, albumFrame);

        genreFrame.setFrameContent(genre.getText().toString());
        track.setFrame(TagResolver.FRAME_GENRE, genreFrame);

        yearFrame.setFrameContent(date.getText().toString());
        track.setFrame(TagResolver.FRAME_YEAR, yearFrame);

        tracknrFrame.setFrameContent(trackNr.getText().toString());
        track.setFrame(TagResolver.FRAME_TRACKID, tracknrFrame);

        composerFrame.setFrameContent(composer.getText().toString());
        track.setFrame(TagResolver.FRAME_COMPOSER, composerFrame);

        if (apicFrame != null){
            track.setFrame(apicFrame);
        }

        return track;
    }

    private void setValues(TagResolver track) {
        titleFrame = track.getFrame(TagResolver.FRAME_TITLE);
        title.setText(titleFrame.getFrameContent());
        artistFrame = track.getFrame(TagResolver.FRAME_ARTIST);
        artist.setText(artistFrame.getFrameContent());
        albumFrame = track.getFrame(TagResolver.FRAME_ALBUM);
        album.setText(albumFrame.getFrameContent());
        genreFrame = track.getFrame(TagResolver.FRAME_GENRE);
        genre.setText(genreFrame.getFrameContent());
        yearFrame = track.getFrame(TagResolver.FRAME_YEAR);
        date.setText(yearFrame.getFrameContent());
        tracknrFrame = track.getFrame(TagResolver.FRAME_TRACKID);
        trackNr.setText(tracknrFrame.getFrameContent());
        composerFrame = track.getFrame(TagResolver.FRAME_COMPOSER);
        composer.setText(composerFrame.getFrameContent());

        if (track.getFrame() != null){
            apicFrame = track.getFrame();
            tagImageView.setImageBitmap(apicFrame.getPictureAsBitmap());
        } else {
            tagImageView.setImageDrawable(ResourcesCompat.getDrawable(requireContext().getResources(),R.drawable.ic_baseline_music_note_24,null));
        }

    }

    private void getValues() {
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, trackID);
        readBytes(trackUri);
    }

    private void readBytes(Uri uri) {
        id3Editor = new ID3Editor(uri, requireContext(), trackID, new ID3EditorInterface() {
            @Override
            public void onDataLoadedListener(TagResolver tagResolver) {
                setValues(tagResolver);
            }
        });
    }

    private void writeTag(){
        try {
            TagWriter tagWriter = new TagWriter(requireContext(),getTagResolver(), id3Editor.getTagHeader());
            tagWriter.writeToFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void selectImageFromGallery(Context context){
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose cover image");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (options[i].equals("Take Photo")){

                } else if (options[i].equals("Choose from Gallery")){
                    Intent pickFromGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickFromGallery, 1);
                } else if (options[i].equals("Cancel")){
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != Activity.RESULT_CANCELED){
            switch (requestCode){
                case 0:{
                    if (resultCode == Activity.RESULT_OK && data != null){
                        Bitmap photo = (Bitmap) data.getExtras().get("data");
                        tagImageView.setImageBitmap(photo);
                    }
                    break;
                }
                case 1:{
                    if (resultCode == Activity.RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        if (selectedImage!=null){
                            try {
                                InputStream is = requireContext().getContentResolver().openInputStream(selectedImage);
                                ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                                int bufferSize = 1024;
                                byte[] buffer = new byte[bufferSize];

                                int len = 0;
                                while ((len = is.read(buffer)) != -1){
                                    byteBuffer.write(buffer, 0, len);
                                }

                                byte[] rawPictureData = byteBuffer.toByteArray();
                                byteBuffer.close();

                                tagImageView.setImageBitmap(BitmapFactory.decodeByteArray(rawPictureData, 0 , rawPictureData.length));

                                String mimeType = requireContext().getContentResolver().getType(selectedImage);

                                //Init ApicFrame
                                if (apicFrame != null){
                                    apicFrame.setPicture(rawPictureData, mimeType);
                                } else {
                                    apicFrame = new ID3V4APICFrame();
                                    apicFrame.setPicture(rawPictureData, mimeType);
                                }

                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                    break;
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}