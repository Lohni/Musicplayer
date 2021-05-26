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
import com.example.musicplayer.utils.enums.PlaybackBehaviour;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import androidx.annotation.Nullable;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private final ArrayList<MusicResolver> songlist = new ArrayList<>();
    private int currSongIndex;
    private PlaybackBehaviour.PlaybackBehaviourState playbackBehaviour = PlaybackBehaviour.PlaybackBehaviourState.REPEAT_LIST;

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
        super.onCreate();
    }

    // MediaPlayer Functions
    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        sendBroadcast(new Intent().setAction(getString(R.string.intent_mediaplayer_play)));
        player.start();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        player.reset();
        return true;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        skip();
    }

    //Playback functions
    public void setSonglist(ArrayList<MusicResolver> list){
        this.songlist.clear();
        this.songlist.addAll(list);
        sendBroadcast(new Intent().setAction("MUSICPLAYER_QUEUE_SIZE").putExtra("MUSICPLAYER_QUEUE_SIZE", songlist.size()));
    }

    public void playNext(ArrayList<MusicResolver> list){
        if (songlist.size() > 0){
            this.songlist.addAll(0, list);
        } else {
            this.songlist.addAll(list);
        }
        currSongIndex = 0;
        playbackBehaviour = PlaybackBehaviour.PlaybackBehaviourState.PLAY_ORDER;
        play();
    }

    public void playNext(MusicResolver song){
        if (songlist.size() > 0){
            this.songlist.add(0, song);
        } else {
            this.songlist.add(song);
        }
        currSongIndex = 0;
        play();
    }

    public void skip(){
        if(songlist.size() > 0){
            switch (playbackBehaviour){
                case SHUFFLE:
                    Random random = new Random();
                    currSongIndex = random.nextInt(songlist.size());
                    break;
                case REPEAT_LIST:
                    currSongIndex++;
                    if (currSongIndex == songlist.size()){
                        currSongIndex = 0;
                    }
                    break;
                case REPEAT_SONG:
                    break;
                case PLAY_ORDER:
                    currSongIndex++;
                    break;
            }
            play();
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
        play();
    }

    public void setProgress(int progress){
        player.seekTo(progress);
    }

    public void play(){
        if(songlist.size() > 0){
            player.reset();
            Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,songlist.get(currSongIndex).getId());
            try{
                player.setDataSource(getApplicationContext(),trackUri);
                player.attachAuxEffect(environmentalReverb.getId());
                player.setAuxEffectSendLevel(1f);
            } catch (IOException e){
                Log.e("MUSIC-SERVICE","Failed to set MediaPlayer-DataSource:",e);
            }
            player.prepareAsync();
            sendBroadcast(new Intent().setAction("MUSICPLAYER_CURRENT_INDEX").putExtra("MUSICPLAYER_CURRENT_INDEX", currSongIndex));
        }
    }

    public MusicResolver getCurrSong(){
        if(!songlist.isEmpty())return songlist.get(currSongIndex);
        else return null;
    }

    public int getCurrentSongIndex(){
        return currSongIndex;
    }

    public int getQueueSize(){
        return songlist.size();
    }

    public int getDuration(){
        if(player.isPlaying()) return player.getDuration();
        else return 0;
    }

    public int getCurrentPosition(){
        return player.getCurrentPosition();
    }

    public void setPlaybackBehaviour(PlaybackBehaviour.PlaybackBehaviourState newState){
        playbackBehaviour = newState;
    }

    public PlaybackBehaviour.PlaybackBehaviourState getPlaybackBehaviour(){
        return playbackBehaviour;
    }

    public int getSessionId(){return player.getAudioSessionId();}

    public void shuffle(){
        skip();
    }

    /*
    Audio Effects
     */

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
