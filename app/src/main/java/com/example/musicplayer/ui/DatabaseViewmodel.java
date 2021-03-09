package com.example.musicplayer.ui;

import android.app.Application;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.example.musicplayer.MusicplayerDatabase;
import com.example.musicplayer.entities.MusicResolver;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class DatabaseViewmodel extends AndroidViewModel {

    private MutableLiveData<ArrayList<String>> allTables;
    private MutableLiveData<ArrayList<String>> allTableSizes;
    private MutableLiveData<ArrayList<MusicResolver>> tableContent;
    private MusicplayerDatabase mDatabase;
    private MutableLiveData<String> mtable;

    public DatabaseViewmodel(@NonNull Application application) {
        super(application);
        mDatabase=new MusicplayerDatabase(application);
    }

    public LiveData<String> getTableName(){return mtable;}

    public LiveData<ArrayList<String>> fetchTables(){
        if(allTables==null){
            allTables=new MutableLiveData<>();
            getAllTables();
        }
        return allTables;
    }

    public LiveData<ArrayList<String>> fetchTableSizes(){
        if(allTableSizes==null){
            allTableSizes=new MutableLiveData<>();
            getAllTableSizes();
        }
        return allTableSizes;
    }

    public LiveData<ArrayList<MusicResolver>> fetchTableContent(String table, Context context){
        getTableContent(table, context);
        return tableContent;
    }

    public void setTableName(String tableName){
        mtable = new MutableLiveData<>();
        mtable.setValue(tableName);
    }

    public boolean createNewTable(String table){
        //return mDatabase.newTable(table);
        return setNewTable(table);
    }

    public void addTableEntries(String table, ArrayList<MusicResolver> entries){
        for(int i=0;i<entries.size();i++){
            mDatabase.addNew(entries.get(i).getTitle(),entries.get(i).getArtist(),entries.get(i).getId(),entries.get(i).getAlbum_id(),table);
        }
        tableContent.setValue(entries);
    }

    public void deleteTable(String table){
        if(mDatabase.deleteTable(table)) Toast.makeText(getApplication(),"Playlist deleted",Toast.LENGTH_SHORT).show();
    }

    public void deleteTableEntry(String table, long id){
        if(mDatabase.deleteItem(table,id)){
            Toast.makeText(getApplication(),"Track removed",Toast.LENGTH_SHORT).show();
        }
    }

    //Database Methods
    private boolean setNewTable(String table){
        boolean state = mDatabase.newTable(table);
        notifyDatabaseChanged();
        return state;
    }

    private void getTableContent(String table, Context context){
        Cursor cursor = mDatabase.getListContents(table);
        tableContent = new MutableLiveData<>();
        ArrayList<MusicResolver> contents = new ArrayList<>();
        while(cursor.moveToNext()){
            Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,cursor.getLong(0));
            Cursor songCursor = context.getContentResolver().query(trackUri, null, null, null, null);
            if (songCursor != null && songCursor.moveToFirst()){
                //Get columns
                int titleColumn = songCursor.getColumnIndex
                        (android.provider.MediaStore.Audio.Media.TITLE);
                int idColumn = songCursor.getColumnIndex
                        (MediaStore.Audio.Media._ID);
                int artistColumn = songCursor.getColumnIndex
                        (android.provider.MediaStore.Audio.Media.ARTIST);
                int albumid = songCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);

                contents.add(new MusicResolver(songCursor.getLong(idColumn), songCursor.getLong(albumid), songCursor.getString(artistColumn), songCursor.getString(titleColumn)));
                tableContent.setValue(contents);
            }
        }
    }

    private void getAllTableSizes(){
        if (allTables==null){
           getAllTables();
        }
        ArrayList<String> size = new ArrayList<>();
        for(int i=0;i<allTables.getValue().size();i++){
            if(mDatabase.getTableSize(allTables.getValue().get(i)) == null)size.add("0");
            else size.add(mDatabase.getTableSize(allTables.getValue().get(i)));
        }
        allTableSizes.setValue(size);
    }

    private void getAllTables(){
        Cursor data = mDatabase.getTables();
        ArrayList<String> tables = new ArrayList<>();
        while(data.moveToNext()){
            if(!data.getString(1).equals("android_metadata")){
                tables.add(data.getString(1));
            }
        }
        allTables.setValue(tables);
    }

    public void notifyDatabaseChanged(){
        getAllTables();
        getAllTableSizes();
    }
}
