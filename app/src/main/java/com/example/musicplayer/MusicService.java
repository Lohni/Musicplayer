package com.example.musicplayer;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.EnvironmentalReverb;
import android.media.audiofx.Equalizer;
import android.media.audiofx.LoudnessEnhancer;
import android.media.audiofx.Virtualizer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;

import com.example.musicplayer.entities.MusicResolver;
import com.example.musicplayer.ui.audioeffects.EqualizerProperties;

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

    private EnvironmentalReverb environmentalReverb;
    private Equalizer equalizer;
    private BassBoost bassBoost;
    private Virtualizer virtualizer;
    private LoudnessEnhancer loudnessEnhancer;

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
        environmentalReverb.release();
        equalizer.release();
        bassBoost.release();
        virtualizer.release();
        loudnessEnhancer.release();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(player!=null)player.release();
        if (environmentalReverb!=null)environmentalReverb.release();
        if (equalizer != null)equalizer.release();
        if (bassBoost != null)bassBoost.release();
        if (virtualizer != null)virtualizer.release();
        if (loudnessEnhancer != null) loudnessEnhancer.release();
    }

    @Override
    public void onCreate() {
        player = new MediaPlayer();
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        //player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnErrorListener(this);
        player.setOnCompletionListener(this);
        environmentalReverb = new EnvironmentalReverb(1,0);
        equalizer = new Equalizer(0, player.getAudioSessionId());
        bassBoost = new BassBoost(1, player.getAudioSessionId());
        virtualizer = new Virtualizer(1, player.getAudioSessionId());
        virtualizer.forceVirtualizationMode(Virtualizer.VIRTUALIZATION_MODE_AUTO);
        loudnessEnhancer = new LoudnessEnhancer(player.getAudioSessionId());

        //environmentalReverb.setEnabled(true);
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
        return true;
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
                player.attachAuxEffect(environmentalReverb.getId());
                player.setAuxEffectSendLevel(1f);
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

    public void shuffle(){
        skip();
    }

    public EnvironmentalReverb.Settings getReverbSettings(){
        return environmentalReverb.getProperties();
    }

    public void setEnvironmentalReverbSettings(EnvironmentalReverb.Settings settings){
        environmentalReverb.setProperties(settings);
    }

    public boolean isReverbEnabled(){return environmentalReverb.getEnabled();}

    public void setReverbEnabled(boolean status){
        environmentalReverb.setEnabled(status);
    }

    public short[] getEqualizerBandLevels(){
        int numberBands = equalizer.getNumberOfBands();
        short[] bandLevels = new short[numberBands];
        for (short i = 0; i< numberBands;i++){
            bandLevels[i] = equalizer.getBandLevel(i);
        }
        return bandLevels;
    }

    public void setEqualizerBandLevels(short[] bandLevels){
        if (equalizer.getNumberOfBands() == bandLevels.length){
            for (short i = 0; i < bandLevels.length; i++){
                equalizer.setBandLevel(i, bandLevels[i]);
            }
        }
    }

    public boolean isEqualizerEnabled(){return equalizer.getEnabled();}

    public void setEqualizerEnabled(boolean status){equalizer.setEnabled(status);}

    public EqualizerProperties getEqualizerProperties(){
        int[] centerFreq = new int[equalizer.getNumberOfBands()];
        for (short i = 0; i < centerFreq.length; i++){
            centerFreq[i] = equalizer.getCenterFreq(i);
        }
        return new EqualizerProperties(equalizer.getNumberOfBands(), equalizer.getBandLevelRange(), centerFreq);
    }

    public boolean isBassBoostEnabled(){return bassBoost.getEnabled(); }

    public void setBassBoostEnabled(boolean state){ bassBoost.setEnabled(state);}

    public void setBassBoostStrength(short strength){ bassBoost.setStrength(strength);}

    public short getBassBoostStrength(){return bassBoost.getRoundedStrength();}


    public boolean isVirtualizerEnabled(){return virtualizer.getEnabled();}

    public void setVirtualizerEnabled(boolean state){virtualizer.setEnabled(state);}

    public void setVirtualizerStrength(short strength){virtualizer.setStrength(strength);}

    public short getVirtualizerStrength(){return virtualizer.getRoundedStrength();}


    public boolean isLoudnessEnhancerEnabled(){return loudnessEnhancer.getEnabled();}

    public void setLoudnessEnhancerEnabled(boolean state){loudnessEnhancer.setEnabled(state);}

    public void setLoudnessEnhancerGain(int gain){
        loudnessEnhancer.setTargetGain(gain);}

    public int getLoudnessEnhancerStrength(){return (int) loudnessEnhancer.getTargetGain();}

}   
