package com.example.musicplayer.ui.tagEditor;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.musicplayer.R;
import com.example.musicplayer.adapter.TagEditorAdapter;
import com.example.musicplayer.entities.MusicResolver;
import com.example.musicplayer.ui.playlist.PlaylistInterface;
import com.example.musicplayer.utils.NavigationControlInterface;
import com.example.musicplayer.utils.Permissions;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TagEditorFragment extends Fragment {

    private RecyclerView tagList;
    private EditText search;

    private TagEditorAdapter adapter;
    private ArrayList<MusicResolver> trackList;

    private Future fetched;
    private TagEditorInterface tagEditorInterface;
    private NavigationControlInterface navigationControlInterface;

    public TagEditorFragment() {}

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            navigationControlInterface = (NavigationControlInterface) context;
            tagEditorInterface = (TagEditorInterface) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + "must implement TagEditorInterface");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tag_editor, container, false);
        tagList = view.findViewById(R.id.tagEditor_songlist);
        search = view.findViewById(R.id.tagEditor_search);

        navigationControlInterface.setToolbarTitle("Tag-Editor");
        navigationControlInterface.setHomeAsUpEnabled(false);
        navigationControlInterface.isDrawerEnabledListener(true);

        trackList = new ArrayList<>();
        adapter = new TagEditorAdapter(trackList, requireContext(), tagEditorInterface);
        tagList.setAdapter(adapter);
        tagList.setHasFixedSize(true);
        tagList.setLayoutManager(new LinearLayoutManager(requireContext()));

        if (Permissions.permission(requireActivity(), this, Manifest.permission.READ_EXTERNAL_STORAGE)){
            loadTrackList();
        }

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && permissions[0].equals(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            loadTrackList();
        }
    }

    private void loadTrackList(){

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Runnable task = () -> {
            ContentResolver contentResolver = requireContext().getContentResolver();
            Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Cursor musicCursor = contentResolver.query(musicUri, null, null, null, null);
            if(musicCursor!=null && musicCursor.moveToFirst()){
                int titleColumn = musicCursor.getColumnIndex
                        (MediaStore.Audio.Media.TITLE);
                int idColumn = musicCursor.getColumnIndex
                        (MediaStore.Audio.Media._ID);
                int artistColumn = musicCursor.getColumnIndex
                        (MediaStore.Audio.Media.ARTIST);
                int albumid = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);

                ArrayList<MusicResolver> chunk = new ArrayList<>();

                do {
                    long thisalbumid = musicCursor.getLong(albumid);
                    long thisId = musicCursor.getLong(idColumn);
                    String thisTitle = musicCursor.getString(titleColumn);
                    String thisArtist = musicCursor.getString(artistColumn);
                    chunk.add(new MusicResolver(thisId, thisalbumid, thisArtist, thisTitle));

                    if (chunk.size() == 20){

                        if (trackList.size() > 0){
                            int pos = trackList.size() - 1;
                            trackList.addAll(chunk);
                            adapter.notifyItemRangeInserted(pos,20);
                        } else {
                            trackList.addAll(chunk);
                            adapter.notifyItemRangeInserted(0,20);
                        }

                        chunk.clear();
                    }

                } while (musicCursor.moveToNext());

                if (chunk.size() > 0){
                    int pos = trackList.size() - 1;
                    trackList.addAll(chunk);
                    adapter.notifyItemRangeInserted(pos,chunk.size());
                    chunk.clear();
                }
            }
            if(musicCursor != null){
                musicCursor.close();
            }
            trackListLoaded();
        };
        fetched = executorService.submit(task);
    }

    private void trackListLoaded(){
        fetched.cancel(true);
    }

}