package com.example.musicplayer;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;

import com.example.musicplayer.entities.MusicResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import androidx.annotation.Nullable;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private ArrayList<MusicResolver> songlist;
    private int currSongIndex;
    private boolean repeatList = true, repeatSong=false, shuffle=false;

    private MediaPlayer player;
    private boolean isStopped=false;
    private final IBinder mBinder = new MusicBinder();

    //return service instance
    public class MusicBinder extends Binder{
        public MusicService getServiceInstance(){
            return MusicService.this;
        }
    }

    //Service Functions
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(player!=null)player.release();
    }

    @Override
    public void onCreate() {
        player = new MediaPlayer();
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnErrorListener(this);
        player.setOnCompletionListener(this);
        super.onCreate();
    }


    // MediaPlayer Functions
    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        sendBroadcast(new Intent().setAction("START"));
        player.start();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        player.reset();
        //datasource
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if(repeatSong){
            play((int) songlist.get(currSongIndex).getId());
        }else {
            skip();
        }
    }

    //Playback functions
    public void setSonglist(ArrayList<MusicResolver> list){
        this.songlist=list;
    }

    public void skip(){
        if(songlist!=null){
            if (shuffle){
                Random random = new Random();
                currSongIndex = random.nextInt((songlist.size()-1) + 1);
                play(songlist.get(currSongIndex).getId());
            }else if(currSongIndex < songlist.size()-1){
                currSongIndex+=1;
                play(songlist.get(currSongIndex).getId());
            } else if(repeatList){
                currSongIndex=0;
                play(songlist.get(currSongIndex).getId());
            } else {
                player.stop();
            }
        }
    }

    public void skipPrevious(){

    }

    public void pause(){
        player.pause();
    }

    public void resume(){player.start();}

    public void setSong(int index){
        currSongIndex = index;
        play(songlist.get(index).getId());
    }

    public void setProgress(int progress){
        player.seekTo(progress);
    }

    public void play(long songID){
        player.reset();
        if(songlist!=null){
            Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,songID);
            try{
                player.setDataSource(getApplicationContext(),trackUri);
            } catch (IOException e){
                Log.e("MUSIC-SERVICE","Failed to set MediaPlayer-DataSource:",e);
            }
            player.prepareAsync();
        }
    }

    public MusicResolver getCurrSong(){
        if(songlist!=null)return songlist.get(currSongIndex);
        else return null;
    }

    public int getDuration(){
        if(player.isPlaying()) return player.getDuration();
        else return 0;
    }

    public int getCurrentPosition(){
        return player.getCurrentPosition();
    }

    public void setRepeat(boolean state){
        this.repeatList=state;
    }

    public boolean getRepeat(){
        return repeatList;
    }

    public void setShuffle(boolean state){
        this.shuffle=state;
    }

    public boolean getShuffle(){return shuffle;}

    public void setRepeatSong(boolean state){
        this.repeatSong=state;
    }

    public boolean getRepeatSong(){return repeatSong;}

    public int getSessionId(){return player.getAudioSessionId();}
}
