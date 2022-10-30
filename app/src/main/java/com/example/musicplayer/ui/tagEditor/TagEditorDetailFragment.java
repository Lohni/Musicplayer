package com.example.musicplayer.ui.tagEditor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.musicplayer.R;
import com.example.musicplayer.database.MusicplayerApplication;
import com.example.musicplayer.database.dao.MusicplayerDataAccess;
import com.example.musicplayer.database.viewmodel.MusicplayerViewModel;
import com.example.musicplayer.interfaces.NavigationControlInterface;
import com.example.musicplayer.utils.Permissions;
import com.example.musicplayer.utils.enums.ID3FrameId;
import com.example.musicplayer.utils.tageditor.ID3Editor;
import com.example.musicplayer.utils.tageditor.ID3V4APICFrame;
import com.example.musicplayer.utils.tageditor.ID3V4Frame;
import com.example.musicplayer.utils.tageditor.ID3V4Track;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class TagEditorDetailFragment extends Fragment {
    private ID3Editor id3Editor;
    private TextInputEditText title, artist, album, genre, date, trackNr, composer;
    private ImageView tagImageView;

    private NavigationControlInterface navigationControlInterface;
    private MusicplayerViewModel musicplayerViewModel;

    private Integer trackId;

    public TagEditorDetailFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        MusicplayerDataAccess mda = ((MusicplayerApplication) requireActivity().getApplication()).getDatabase().musicplayerDao();
        musicplayerViewModel = new ViewModelProvider(this, new MusicplayerViewModel.MusicplayerViewModelFactory(mda)).get(MusicplayerViewModel.class);

        trackId = getArguments().getInt("TRACK_ID");
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.tageditor_confirm, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_tagEditor_confirm) {
            writeTag();
            Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id3Editor.getTrackId());
            requireContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, trackUri));
            requireActivity().onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            navigationControlInterface = (NavigationControlInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + "must implement NavigationControlInterface");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tag_editor_detail, container, false);

        title = view.findViewById(R.id.tagEditorDetail_title);
        artist = view.findViewById(R.id.tagEditorDetail_artist);
        album = view.findViewById(R.id.tagEditorDetail_album);
        genre = view.findViewById(R.id.tagEditorDetail_genre);
        date = view.findViewById(R.id.tagEditorDetail_date);
        trackNr = view.findViewById(R.id.tagEditorDetail_tracknr);
        composer = view.findViewById(R.id.tagEditorDetail_composer);
        tagImageView = view.findViewById(R.id.tagEditorDetail_cover);

        tagImageView.setOnClickListener(view1 -> selectImageFromGallery(requireContext()));

        navigationControlInterface.isDrawerEnabledListener(false);
        navigationControlInterface.setHomeAsUpEnabled(true);
        navigationControlInterface.setHomeAsUpIndicator(R.drawable.ic_clear_black_24dp);

        musicplayerViewModel.getTrackById(trackId).observe(getViewLifecycleOwner(), track -> {
            if (Permissions.permission(requireActivity(), this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                readID3Tag();
            }
        });
        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && permissions[0].equals(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            readID3Tag();
        }
    }

    private void setValues(ID3V4Track track) {
        title.setText(getTextValueFromTrack(ID3FrameId.TIT2, track));
        artist.setText(getTextValueFromTrack(ID3FrameId.TPE2, track));
        album.setText(getTextValueFromTrack(ID3FrameId.TALB, track));
        genre.setText(getTextValueFromTrack(ID3FrameId.TCON, track));
        date.setText(getTextValueFromTrack(ID3FrameId.TDRC, track));
        trackNr.setText(getTextValueFromTrack(ID3FrameId.TRCK, track));
        composer.setText(getTextValueFromTrack(ID3FrameId.TCOM, track));

        if (track.getRelevantFrame(ID3FrameId.APIC) != null) {
            tagImageView.setImageBitmap(((ID3V4APICFrame) track.getRelevantFrame(ID3FrameId.APIC)).getPictureAsBitmap());
        } else {
            tagImageView.setImageDrawable(ResourcesCompat.getDrawable(requireContext().getResources(), R.drawable.ic_baseline_music_note_24, null));
        }
    }

    private String getTextValueFromTrack(ID3FrameId frameId, ID3V4Track track) {
        return (track.getRelevantFrame(frameId) != null)
                ? (String) track.getRelevantFrame(frameId).getFrameData() : "";
    }

    private void readID3Tag() {
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, trackId.longValue());
        id3Editor = new ID3Editor(trackUri, requireContext(), trackId.longValue(), this::setValues);
    }

    private void writeTag() {
        try {
            ID3V4Track track = id3Editor.getTrackData();
            updateEditorWithTextValues(track, ID3FrameId.TIT2, title.getText().toString());
            updateEditorWithTextValues(track, ID3FrameId.TPE2, artist.getText().toString());
            updateEditorWithTextValues(track, ID3FrameId.TALB, album.getText().toString());
            updateEditorWithTextValues(track, ID3FrameId.TCON, genre.getText().toString());
            updateEditorWithTextValues(track, ID3FrameId.TDRC, date.getText().toString());
            updateEditorWithTextValues(track, ID3FrameId.TRCK, trackNr.getText().toString());
            updateEditorWithTextValues(track, ID3FrameId.TCOM, composer.getText().toString());

            id3Editor.saveTrack();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateEditorWithTextValues(ID3V4Track track, ID3FrameId frameId, String newVal) {
        ID3V4Frame frame = track.getRelevantFrame(frameId);
        if (frame != null) {
            frame.setFrameData(newVal);
        } else if (!newVal.equals("")) {
            frame = track.createNewTextFrame(frameId, newVal);
            track.setFrame(frame);
        }
    }

    private void selectImageFromGallery(Context context) {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose cover image");

        builder.setItems(options, (dialogInterface, i) -> {
            if (options[i].equals("Take Photo")) {

            } else if (options[i].equals("Choose from Gallery")) {
                Intent pickFromGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickFromGallery, 1);
            } else if (options[i].equals("Cancel")) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != Activity.RESULT_CANCELED) {
            switch (requestCode) {
                case 0: {
                    if (resultCode == Activity.RESULT_OK && data != null) {
                        Bitmap photo = (Bitmap) data.getExtras().get("data");
                        tagImageView.setImageBitmap(photo);
                    }
                    break;
                }
                case 1: {
                    if (resultCode == Activity.RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        if (selectedImage != null) {
                            try {
                                InputStream is = requireContext().getContentResolver().openInputStream(selectedImage);
                                ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                                int bufferSize = 1024;
                                byte[] buffer = new byte[bufferSize];

                                int len;
                                while ((len = is.read(buffer)) != -1) {
                                    byteBuffer.write(buffer, 0, len);
                                }

                                byte[] rawPictureData = byteBuffer.toByteArray();
                                byteBuffer.close();

                                tagImageView.setImageBitmap(BitmapFactory.decodeByteArray(rawPictureData, 0, rawPictureData.length));

                                String mimeType = requireContext().getContentResolver().getType(selectedImage);
                                ID3V4Track track = id3Editor.getTrackData();
                                //Init ApicFrame
                                if (track.getRelevantFrame(ID3FrameId.APIC) != null) {
                                    ID3V4APICFrame apicFrame = (ID3V4APICFrame) track.getRelevantFrame(ID3FrameId.APIC);
                                    apicFrame.setFrameData(rawPictureData);
                                    apicFrame.setMimeType(mimeType);
                                } else {
                                    ID3V4APICFrame apicFrame = track.createNewApicframe(ID3FrameId.APIC, rawPictureData);
                                    apicFrame.setMimeType(mimeType);
                                    track.setFrame(apicFrame);
                                }

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