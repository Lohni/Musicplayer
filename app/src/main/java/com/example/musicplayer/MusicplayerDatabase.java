package com.example.musicplayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MusicplayerDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "playlists.db";
    private static  String TABLE_NAME;
    private static final String COL1 = "ID";
    private static final String COL2 = "TITLE";
    private static final String COL3 = "ARTIST";
    private static final String COL4 = "ALBUM_ID";

    public MusicplayerDatabase(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean newTable(String tableName){
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + tableName + " (ID INTEGER PRIMARY KEY, TITLE TEXT, ARTIST TEXT, ALBUM_ID INTEGER)");
            return true;
        } catch (SQLiteException e){
            Log.e("DATABASE", "Table doesnt exist", e);
            return false;
        }
    }

    public Boolean deleteTable(String tablename){
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.execSQL("DROP TABLE IF EXISTS " + tablename);
            return true;
        } catch (SQLiteException e){
            Log.e("DATABASE", "Table doesnt exist", e);
            return false;
        }
    }

    public void setTableName(String currtable) {
        TABLE_NAME = currtable;
    }

    public boolean addNew(String title, String artist, long id, long album_id, String table){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, id);
        contentValues.put(COL2, title);
        contentValues.put(COL3, artist);
        contentValues.put(COL4, album_id);
        long result = db.insert(table, null, contentValues);
        //if date as inserted incorrectly it will return -1
        return result != -1;
    }

    public Cursor getListContents(String tablename){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data;
        try{
            data = db.rawQuery("SELECT  *  FROM " + tablename, null);
            return data;
        }
        catch (SQLiteException e){
            Log.e("DATABASE", "Table doesn't exist", e);
            return null;
        }
    }

    public Cursor getTables(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM sqlite_master WHERE type='table'", null);
        return data;
    }

    public String getTableSize(String tablename){
        SQLiteDatabase db = this.getWritableDatabase();
        try{
            Cursor data = db.rawQuery("SELECT " + COL1 + " FROM " + tablename, null);
            Integer count = data.getCount();
            String size =  count.toString();
            return size;
        }
        catch (SQLiteException e){
            Log.e("DATABASE", "Table doesnt exist", e);
            return null;
        }
    }

    public boolean renameTable(String newname, String oldname){
        SQLiteDatabase db = this.getWritableDatabase();
        try{
            db.execSQL("ALTER TABLE " + oldname + " RENAME TO " + newname);
            return true;
        }
        catch (SQLiteException e){
            Log.e("ALTER TABLE", "RENAME ERROR", e);
            return false;
        }
    }

    public boolean deleteItem(String table, long id){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(table, COL1 + "=" + id, null) > 0;
    }
}
