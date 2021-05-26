package com.example.musicplayer.ui.album;

import android.app.Application;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import com.example.musicplayer.entities.AlbumResolver;
import com.example.musicplayer.entities.MusicResolver;
import com.example.musicplayer.transition.AlbumDetailTransition;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class AlbumViewModel extends AndroidViewModel {

    private MutableLiveData<ArrayList<AlbumResolver>> albumList;
    private MutableLiveData<ArrayList<MusicResolver>> albumSongList;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    public AlbumViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<ArrayList<AlbumResolver>> getAllAlbums(){
        if (albumList == null){
            albumList = new MutableLiveData<>();
            fetchAlbumList();
        }
        return albumList;
    }

    public LiveData<ArrayList<MusicResolver>> getAllAlbumSongs(long albumID){
        if (albumSongList == null){
            albumSongList = new MutableLiveData<>();
        }
        if (albumSongList.getValue() != null){
            albumSongList.getValue().clear();
        }
        fetchAlbumSongs(albumID);
        return albumSongList;
    }

    private void fetchAlbumList(){
        executor.submit(new Runnable() {
            @Override
            public void run() {
                ArrayList<AlbumResolver> albums = new ArrayList<>();
                ContentResolver contentResolver = getApplication().getContentResolver();
                Uri musicUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;


                final String _id = MediaStore.Audio.Albums._ID;
                final String album_name = MediaStore.Audio.Albums.ALBUM;
                final String totSongs = MediaStore.Audio.Albums.NUMBER_OF_SONGS;
                final String artist_Name = MediaStore.Audio.Albums.ARTIST;
                final String artist_Id = MediaStore.Audio.Albums.ARTIST_ID;
                final String albumArt = MediaStore.Audio.Albums.ALBUM_ART;

                final String[] columns = {_id, album_name, artist_Name, artist_Id, totSongs, albumArt};
                Cursor cursor = contentResolver.query(musicUri, columns, null, null, album_name + " ASC");
                if(cursor!=null && cursor.moveToFirst()){
                    do {
                        long albumId = cursor.getLong(cursor.getColumnIndex(_id));
                        String albumName = cursor.getString(cursor.getColumnIndex(album_name));
                        String albumArtist = cursor.getString(cursor.getColumnIndex(artist_Name));
                        int totalSongs = cursor.getInt(cursor.getColumnIndex(totSongs));
                        long artistId = cursor.getLong(cursor.getColumnIndex(artist_Id));
                        String artUri = cursor.getString(cursor.getColumnIndex(albumArt));

                        albums.add(new AlbumResolver(albumId, artistId, totalSongs, albumName, albumArtist, artUri));
                    } while (cursor.moveToNext());
                }
                if(cursor != null)cursor.close();
                albumList.postValue(albums);
            }
        });


    }

    private void fetchAlbumSongs(long targetAlbumID){
        executor.submit(new Runnable() {
            @Override
            public void run() {
                ArrayList<MusicResolver> albumTracks = new ArrayList<>();
                ContentResolver contentResolver = getApplication().getContentResolver();
                Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

                final String _id = MediaStore.Audio.Media._ID;
                final String title = MediaStore.Audio.Media.TITLE;
                final String trackID = MediaStore.Audio.Media.TRACK;
                final String artistIndex = MediaStore.Audio.Media.ARTIST;
                final String albumID = MediaStore.Audio.Media.ALBUM_ID;

                final String[] colums = {_id, title, trackID, artistIndex};
                Cursor cursor = contentResolver.query(musicUri, colums, albumID + " = ?", new String[]{String.valueOf(targetAlbumID)}, trackID + " ASC");
                if(cursor!=null && cursor.moveToFirst()){
                    do{
                        long songID = cursor.getLong(cursor.getColumnIndex(_id));
                        String trackTitle = cursor.getString(cursor.getColumnIndex(title));
                        int trackNr = cursor.getInt(cursor.getColumnIndex(trackID));
                        String artist = cursor.getString(cursor.getColumnIndex(artistIndex));

                        MusicResolver track = new MusicResolver(songID, targetAlbumID, artist, trackTitle);
                        track.setTrackNr(trackNr);
                        albumTracks.add(track);
                    }while (cursor.moveToNext());
                }
                if (cursor!=null)cursor.close();
                albumSongList.postValue(albumTracks);
            }
        });
    }
}
