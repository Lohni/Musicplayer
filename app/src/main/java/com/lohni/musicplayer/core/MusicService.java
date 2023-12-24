package com.lohni.musicplayer.core;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.lohni.musicplayer.R;
import com.lohni.musicplayer.database.entity.Album;
import com.lohni.musicplayer.database.entity.Playlist;
import com.lohni.musicplayer.database.entity.Track;
import com.lohni.musicplayer.dto.EqualizerProperties;
import com.lohni.musicplayer.utils.enums.ListType;
import com.lohni.musicplayer.utils.enums.PlaybackAction;
import com.lohni.musicplayer.utils.enums.PlaybackBehaviour;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, PlaybackSession.OnListPlay {
    private final IBinder mBinder = new MusicBinder();
    private final Handler handler = new Handler();

    private final PlaybackSession playbackSession = new PlaybackSession();
    private AudioEffectSession audioEffectSession;
    private MediaPlayer player;
    private MediaSessionCompat mediaSession;
    private NotificationControl notificationControl;

    public class MusicBinder extends Binder {
        public MusicService getServiceInstance() {
            return MusicService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        notificationControl = new NotificationControl(this, getSystemService(NotificationManager.class));
        createNotification();
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        player = new MediaPlayer();
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setOnPreparedListener(this);
        player.setOnErrorListener(this);
        player.setOnCompletionListener(this);
        player.setLooping(false);
        playbackSession.setOnListPlayListener(this);
        audioEffectSession = new AudioEffectSession(player.getAudioSessionId());

        IntentFilter filter = new IntentFilter();
        filter.addAction(getString(R.string.notification_action_next));
        filter.addAction(getString(R.string.notification_action_pause));
        filter.addAction(getString(R.string.notification_action_play));
        filter.addAction(getString(R.string.notification_action_previous));
        filter.addAction(AudioManager.ACTION_HEADSET_PLUG);
        filter.addAction(getString(R.string.musicservice_audioeffect));
        filter.addAction(getString(R.string.musicservice_play_list));
        this.registerReceiver(this.broadcastReceiver, filter);

        MediaSessionCallback sessionCallback = new MediaSessionCallback();
        sessionCallback.setOnSkipListener(this::skip);
        sessionCallback.setOnPauseListener(this::pause);
        sessionCallback.setOnPlayListener(this::resume);
        sessionCallback.setOnMediaButtonListener(() -> {
            if (!player.isPlaying()) resume();
            else pause();
        });

        mediaSession = new MediaSessionCompat(getBaseContext(), "MUSICPLAYER_MEDIASESSION");
        mediaSession.setActive(true);
        mediaSession.setCallback(sessionCallback);
        super.onCreate();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        sendCurrentStateToPlaybackControl();
        sendPreparedSong();
        handler.post(this::createNotification);
        resume();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        player.reset();
        playbackSession.clearSnapshot();
        return true;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        skip(PlaybackAction.SKIP_NEXT);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        audioEffectSession.release();
        playbackSession.clearSnapshot();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        playbackSession.clearSnapshot();
        if (player != null) player.release();
        audioEffectSession.release();
        stopForeground(true);
        this.unregisterReceiver(this.broadcastReceiver);
    }

    private void createNotification() {
        mediaSession.setMetadata(notificationControl.createMediaMetadataFromTrack(playbackSession.getCurrentTrack().orElse(null)));
        mediaSession.setPlaybackState(createPlaybackState());
        startForeground(notificationControl.getNOTIFICATION_ID(), notificationControl.createNotification(this, isPlaying(), mediaSession.getSessionToken(), playbackSession.getCurrentTrack().orElse(null)));
    }

    private PlaybackStateCompat createPlaybackState() {
        int state = (isPlaying()) ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
        float speed = (isPlaying()) ? 1f : 0f;
        return new PlaybackStateCompat.Builder()
                .setState(state, player.getCurrentPosition(), speed)
                .setActions(PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS | PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
                .build();
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                if (intent.getAction().equals(getString(R.string.notification_action_play))) {
                    resume();
                } else if (intent.getAction().equals(getString(R.string.notification_action_pause))) {
                    pause();
                } else if (intent.getAction().equals(getString(R.string.notification_action_next))) {
                    skip(PlaybackAction.SKIP_NEXT);
                } else if (intent.getAction().equals(getString(R.string.notification_action_previous))) {
                    skip(PlaybackAction.SKIP_PREVIOUS);
                } else if (intent.getAction().equals(AudioManager.ACTION_HEADSET_PLUG)) {
                    int mode = intent.getIntExtra("state", 0);
                    if (mode == 0) pause();
                } else if (intent.getAction().equals(getString(R.string.musicservice_audioeffect))) {
                    audioEffectSession.setValueFromBundle(intent);
                } else if (intent.getAction().equals(getString(R.string.musicservice_play_list))) {
                    Parcelable[] tracks = intent.getParcelableArrayExtra("LIST");
                    int index = intent.getIntExtra("INDEX", 0);
                    removeAllTracks();
                    addSongList(Arrays.stream(tracks).map(p -> (Track) p).collect(Collectors.toList()), true);
                    playSong((Track) tracks[index]);
                }
            }
        }
    };

    public int getCurrentPosition() {
        return Math.max(player.getCurrentPosition(), 0);
    }

    public boolean isPlaying() {
        return player != null && player.isPlaying();
    }

    /*
     * Queue Control
     */
    public void addSongList(List<Track> newTracks, boolean playNext) {
        playbackSession.addToQueue(newTracks, playNext);

        if (playNext && playbackSession.getPlaybackBehaviour().equals(PlaybackBehaviour.SHUFFLE)) {
            skip(PlaybackAction.SKIP_NEXT);
        }

        sendCurrentStateToPlaybackControl();
    }

    public void removeAllTracks() {
        playbackSession.clearQueue();
        sendCurrentStateToPlaybackControl();
    }

    public void changeOrder(int fromPosition, int toPosition) {
        playbackSession.changeOrder(fromPosition, toPosition);
    }

    public void removeTracks(List<Track> tracksToRemove) {
        if (playbackSession.removeTracksFromQueue(tracksToRemove)) {
            sendOnSongCompleted();
            player.reset();
        }
        sendCurrentStateToPlaybackControl();
    }

    public void setListTypePayload(Object listTypePayload) {
        ListType listType = ListType.TRACK;
        int listTypeObjectId = -1;

        if (listTypePayload instanceof Album) {
            listType = ListType.ALBUM;
            listTypeObjectId = ((Album) listTypePayload).getAId();
        } else if (listTypePayload instanceof Playlist) {
            listType = ListType.PLAYLIST;
            listTypeObjectId = ((Playlist) listTypePayload).getPId();
        }

        playbackSession.setListType(listType);
        playbackSession.setListTypeObject(listTypeObjectId);
    }

    /*
     * Playback actions
     */

    private void play() {
        playbackSession.getCurrentTrack().ifPresent((track) -> {
            player.reset();
            Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, track.getTId());
            try {
                player.setDataSource(getApplicationContext(), trackUri);
                player.attachAuxEffect(audioEffectSession.getEnvReverbId());
                player.setAuxEffectSendLevel(1f);
            } catch (IOException e) {
                Log.e("MUSIC-SERVICE", "Failed to set MediaPlayer-DataSource:", e);
            }
            player.prepareAsync();
        });
    }

    public void pause() {
        if (player.isPlaying()) {
            player.pause();
            sendCurrentStateToPlaybackControl();
            createNotification();
        }
    }

    public void resume() {
        player.start();
        sendCurrentStateToPlaybackControl();
        createNotification();
    }

    public void skip(PlaybackAction action) {
        sendOnSongCompleted();

        if (action == PlaybackAction.SKIP_NEXT || (action == PlaybackAction.SKIP_PREVIOUS && player.getCurrentPosition() < 2000)) {
            playbackSession.nextTrack(action);
        }

        play();
    }

    public void playSong(Track track) {
        sendOnSongCompleted();
        playbackSession.setTrack(track);
        play();
    }

    public void setProgress(int progress) {
        player.seekTo(progress);
    }

    public void setPlaybackBehaviour(PlaybackBehaviour newState) {
        playbackSession.setPlaybackBehaviour(newState);
    }

    /*
    Audio Effects
     */

    public short[] getEqualizerBandLevels() {
        return audioEffectSession.getEqualizerBandLevels();
    }

    public EqualizerProperties getEqualizerProperties() {
        return audioEffectSession.getEqualizerProperties();
    }

    public void sendCurrentStateToPlaybackControl() {
        Bundle bundle = playbackSession.getStateAsBundle();
        bundle.putBoolean("ISONPAUSE", !player.isPlaying());
        bundle.putInt("SESSION_ID", player.getAudioSessionId());
        bundle.putInt("CURRENT_POSITION", getCurrentPosition());

        sendBroadcast(new Intent().setAction(getString(R.string.playback_control_values)).putExtras(bundle));
    }

    private void sendPreparedSong() {
        playbackSession.getSnapshotAsBundle().ifPresent(bundle -> {
            sendBroadcast(new Intent(this, SystemBroadcastReceiver.class).setAction(getString(R.string.musicservice_song_prepared)).putExtras(bundle));
        });
    }

    public void sendOnSongCompleted() {
        playbackSession.getSnapshotAsBundle().ifPresent((bundle) -> {
            bundle.putLong("TIME_PLAYED", getCurrentPosition());
            sendBroadcast(new Intent(this, SystemBroadcastReceiver.class).setAction(getString(R.string.musicservice_song_ended)).putExtras(bundle));
        });
    }

    @Override
    public void onListPlay(@NonNull Bundle bundle) {
        sendBroadcast(new Intent(this, SystemBroadcastReceiver.class).setAction(getString(R.string.musicservice_list_play)).putExtras(bundle));
    }
}
