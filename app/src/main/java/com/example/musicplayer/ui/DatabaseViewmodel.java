package com.example.musicplayer.ui;

import android.app.Application;
import android.database.Cursor;
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

    public DatabaseViewmodel(@NonNull Application application) {
        super(application);
        mDatabase=new MusicplayerDatabase(application);
    }

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

    public LiveData<ArrayList<MusicResolver>> fetchTableContent(String table){
        getTableContent(table);
        return tableContent;
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
        if(mDatabase.deleteItem(table,id)) Toast.makeText(getApplication(),"Track removed",Toast.LENGTH_SHORT).show();
    }

    //Database Methods
    private boolean setNewTable(String table){
        boolean state = mDatabase.newTable(table);
        ArrayList<String> size =new ArrayList<>();
        ArrayList<String> tables = new ArrayList<>();
        if (state){
            tables.add(table);
            size.add("0");
            allTables.setValue(tables);
            allTableSizes.setValue(size);
        }
        return state;
    }

    private void getTableContent(String table){
        Cursor cursor = mDatabase.getListContents(table);
        tableContent = new MutableLiveData<>();
        ArrayList<MusicResolver> contents = new ArrayList<>();
        while(cursor.moveToNext()){
            contents.add(new MusicResolver(cursor.getLong(0),cursor.getLong(3),cursor.getString(2),cursor.getString(1)));
        }
        tableContent.setValue(contents);
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
}
