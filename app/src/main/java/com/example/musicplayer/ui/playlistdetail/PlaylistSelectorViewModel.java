package com.example.musicplayer.ui.playlistdetail;

import android.app.Application;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.example.musicplayer.entities.MusicResolver;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class PlaylistSelectorViewModel extends AndroidViewModel {
    private MutableLiveData<ArrayList<MusicResolver>> trackList;

    public PlaylistSelectorViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<ArrayList<MusicResolver>> getTracklist(){
        if(trackList==null){
            trackList = new MutableLiveData<ArrayList<MusicResolver>>();
            fetchTracks();
        }
        return trackList;
    }
    private void fetchTracks(){
        ArrayList<MusicResolver> tracks = new ArrayList<>();
        ContentResolver contentResolver = getApplication().getContentResolver();
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
                tracks.add(new MusicResolver(thisId, thisalbumid, thisArtist, thisTitle));
            }
            while (musicCursor.moveToNext());
        }
        if(musicCursor != null)musicCursor.close();
        trackList.setValue(tracks);
    }
}
