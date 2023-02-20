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
import android.media.audiofx.BassBoost;
import android.media.audiofx.EnvironmentalReverb;
import android.media.audiofx.Equalizer;
import android.media.audiofx.LoudnessEnhancer;
import android.media.audiofx.Virtualizer;
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
import android.view.KeyEvent;

import com.lohni.musicplayer.R;
import com.lohni.musicplayer.database.entity.AdvancedReverbPreset;
import com.lohni.musicplayer.database.entity.Album;
import com.lohni.musicplayer.database.entity.EqualizerPreset;
import com.lohni.musicplayer.database.entity.Playlist;
import com.lohni.musicplayer.database.entity.Track;
import com.lohni.musicplayer.dto.EqualizerProperties;
import com.lohni.musicplayer.utils.ServiceUtil;
import com.lohni.musicplayer.utils.converter.AudioEffectSettingsHelper;
import com.lohni.musicplayer.utils.enums.ListType;
import com.lohni.musicplayer.utils.enums.PlaybackAction;
import com.lohni.musicplayer.utils.enums.PlaybackBehaviour;
import com.lohni.musicplayer.utils.enums.PlaybackBehaviourState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private final ArrayList<Track> songlist = new ArrayList<>();
    private final TmpState tmpState = new TmpState();
    private PlaybackBehaviourState playbackBehaviour = PlaybackBehaviourState.REPEAT_LIST;
    private ListType currentListType = ListType.TRACK;
    private int listTypeId = -1;
    private final IBinder mBinder = new MusicBinder();
    private final Handler handler = new Handler();
    private final Runnable mediaButtonCounterRunnable = () -> MEDIA_BUTTON_DOWN_COUNT = 0;

    private MediaPlayer player;
    private EnvironmentalReverb environmentalReverb;
    private Equalizer equalizer;
    private BassBoost bassBoost;
    private Virtualizer virtualizer;
    private LoudnessEnhancer loudnessEnhancer;

    private MediaSessionCompat mediaSession;
    private NotificationControl notificationControl;

    private int MEDIA_BUTTON_DOWN_COUNT = 0;
    private int currSongIndex = -1;

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
        player.setLooping(false);
        environmentalReverb = new EnvironmentalReverb(1, 0);
        equalizer = new Equalizer(0, player.getAudioSessionId());
        bassBoost = new BassBoost(1, player.getAudioSessionId());
        virtualizer = new Virtualizer(1, player.getAudioSessionId());
        virtualizer.forceVirtualizationMode(Virtualizer.VIRTUALIZATION_MODE_AUTO);
        loudnessEnhancer = new LoudnessEnhancer(player.getAudioSessionId());

        IntentFilter filter = new IntentFilter();
        filter.addAction(getString(R.string.notification_action_next));
        filter.addAction(getString(R.string.notification_action_pause));
        filter.addAction(getString(R.string.notification_action_play));
        filter.addAction(getString(R.string.notification_action_previous));
        filter.addAction(AudioManager.ACTION_HEADSET_PLUG);
        filter.addAction(getString(R.string.musicservice_bassboost_enabled));
        filter.addAction(getString(R.string.musicservice_virtualizer_enabled));
        filter.addAction(getString(R.string.musicservice_loudness_enhancer_enabled));
        filter.addAction(getString(R.string.musicservice_equalizer_enabled));
        filter.addAction(getString(R.string.musicservice_reverb_enabled));
        filter.addAction(getString(R.string.musicservice_bassboost_strength));
        filter.addAction(getString(R.string.musicservice_virtualizer_strength));
        filter.addAction(getString(R.string.musicservice_loudness_enhancer_strength));
        filter.addAction(getString(R.string.musicservice_reverb_values));
        filter.addAction(getString(R.string.musicservice_equalizer_values));
        filter.addAction(getString(R.string.musicservice_play_list));
        this.registerReceiver(this.broadcastReceiver, filter);

        mediaSession = new MediaSessionCompat(getBaseContext(), "MUSICPLAYER_MEDIASESSION");
        mediaSession.setActive(true);
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                resume();
            }

            @Override
            public void onPause() {
                pause();
            }

            @Override
            public void onSkipToNext() {
                skip(PlaybackAction.SKIP_NEXT);
            }

            @Override
            public void onSkipToPrevious() {
                skip(PlaybackAction.SKIP_PREVIOUS);
            }

            @Override
            public boolean onMediaButtonEvent(@NonNull Intent mediaButtonIntent) {
                String intentAction = mediaButtonIntent.getAction();
                if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
                    KeyEvent event = mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                    if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
                        if (MEDIA_BUTTON_DOWN_COUNT == 0) {
                            handler.postDelayed(mediaButtonCounterRunnable, 600);
                        }
                        MEDIA_BUTTON_DOWN_COUNT++;
                        if (MEDIA_BUTTON_DOWN_COUNT < 2) {
                            if (!player.isPlaying()) {
                                resume();
                            } else {
                                pause();
                            }
                        } else {
                            skip(PlaybackAction.SKIP_NEXT);
                        }
                    }
                }
                return true;
            }
        });
        super.onCreate();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        tmpState.currentPlayingTmp = songlist.get(currSongIndex);
        tmpState.currentListTypeIDTmp = listTypeId;
        tmpState.currentListTypeTmp = currentListType;
        sendCurrentStateToPlaybackControl();
        sendPreparedSong();
        handler.post(this::createNotification);
        resume();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        player.reset();
        tmpState.currentPlayingTmp = null;
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
        if (player != null) player.release();
        if (environmentalReverb != null) environmentalReverb.release();
        if (equalizer != null) equalizer.release();
        if (bassBoost != null) bassBoost.release();
        if (virtualizer != null) virtualizer.release();
        if (loudnessEnhancer != null) loudnessEnhancer.release();

        stopForeground(true);
        this.unregisterReceiver(this.broadcastReceiver);
    }

    private void createNotification() {
        mediaSession.setMetadata(notificationControl.createMediaMetadataFromTrack(tmpState.currentPlayingTmp));
        mediaSession.setPlaybackState(createPlaybackState());
        startForeground(notificationControl.getNOTIFICATION_ID(), notificationControl.createNotification(this, isPlaying(), mediaSession.getSessionToken(), tmpState.currentPlayingTmp));
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
                    if (mode == 0) {
                        pause();
                    }
                } else if (intent.getAction().equals(getString(R.string.musicservice_bassboost_enabled))) {
                    bassBoost.setEnabled(intent.getBooleanExtra("ENABLED", false));
                } else if (intent.getAction().equals(getString(R.string.musicservice_virtualizer_enabled))) {
                    virtualizer.setEnabled(intent.getBooleanExtra("ENABLED", false));
                } else if (intent.getAction().equals(getString(R.string.musicservice_loudness_enhancer_enabled))) {
                    loudnessEnhancer.setEnabled(intent.getBooleanExtra("ENABLED", false));
                } else if (intent.getAction().equals(getString(R.string.musicservice_equalizer_enabled))) {
                    equalizer.setEnabled(intent.getBooleanExtra("ENABLED", false));
                } else if (intent.getAction().equals(getString(R.string.musicservice_reverb_enabled))) {
                    environmentalReverb.setEnabled(intent.getBooleanExtra("ENABLED", false));
                } else if (intent.getAction().equals(getString(R.string.musicservice_bassboost_strength))) {
                    bassBoost.setStrength(intent.getShortExtra("STRENGTH", (short) 0));
                } else if (intent.getAction().equals(getString(R.string.musicservice_virtualizer_strength))) {
                    virtualizer.setStrength(intent.getShortExtra("STRENGTH", (short) 0));
                } else if (intent.getAction().equals(getString(R.string.musicservice_loudness_enhancer_strength))) {
                    loudnessEnhancer.setTargetGain(intent.getIntExtra("STRENGTH", 0));
                } else if (intent.getAction().equals(getString(R.string.musicservice_reverb_values))) {
                    AdvancedReverbPreset arp = intent.getParcelableExtra("VALUES");
                    if (arp != null) {
                        environmentalReverb.setProperties(AudioEffectSettingsHelper.extractReverbValues(arp));
                    }
                } else if (intent.getAction().equals(getString(R.string.musicservice_equalizer_values))) {
                    EqualizerPreset eq = intent.getParcelableExtra("VALUES");
                    if (eq != null) {
                        ServiceUtil.setEqualizerBandLevelsByPreset(equalizer, eq);
                    }
                } else if (intent.getAction().equals(getString(R.string.musicservice_play_list))) {
                    Parcelable[] tracks = intent.getParcelableArrayExtra("LIST");
                    removeAllTracks();
                    addSongList(Arrays.stream(tracks).map(p -> (Track) p).collect(Collectors.toList()), true);
                }
            }
        }
    };

    public Track getCurrSong() {
        return tmpState.currentPlayingTmp;
    }

    public int getDuration() {
        if (player.isPlaying()) return player.getDuration();
        else return 0;
    }

    public int getCurrentPosition() {
        return player.getCurrentPosition();
    }

    public int getSessionId() {
        return player.getAudioSessionId();
    }

    public void shuffle() {
        skip(PlaybackAction.SKIP_NEXT);
    }

    public boolean isPlaying() {
        return player != null && player.isPlaying();
    }

    /*
     * Queue Control
     */
    public void addSongList(List<Track> newTracks, boolean playNext) {
        int sizePreAdd = songlist.size();

        for (int i = 0; i < newTracks.size(); i++) {
            Track track = newTracks.get(i);
            if (playNext) {
                int startIndex = (currSongIndex >= 0) ? currSongIndex + 1 : 0;
                if (!songlist.contains(track)) {
                    songlist.add(startIndex + i, track);
                } else {
                    int oldIndexTrack = songlist.indexOf(track);
                    int toPos = (oldIndexTrack < startIndex) ? currSongIndex : startIndex + i;
                    changeOrder(oldIndexTrack, toPos, false);
                }
            } else if (!songlist.contains(track)) {
                songlist.add(track);
            }
        }

        if (sizePreAdd == 0 && playNext
                && (currentListType.equals(ListType.ALBUM) || currentListType.equals(ListType.PLAYLIST))) {
            sendOnListPlay();
        }

        if (currSongIndex < 0 && playbackBehaviour.equals(PlaybackBehaviourState.SHUFFLE)) {
            currSongIndex = 0;
            skip(PlaybackAction.SKIP_NEXT);
        } else if ((tmpState.currentPlayingTmp == null || sizePreAdd == 0) && playNext && !songlist.isEmpty()) {
            playSong(newTracks.get(0));
        }

        sendCurrentStateToPlaybackControl();
    }

    public void removeAllTracks() {
        if (!songlist.isEmpty() && currSongIndex >= 0) {
            Track currPlaying = songlist.get(currSongIndex);
            currSongIndex = -1;
            sendCurrentStateToPlaybackControl(currPlaying);
        }
        songlist.clear();
    }

    public void removeTracks(List<Track> tracksToRemove) {
        if (!songlist.isEmpty()) {
            Track currPlayed = songlist.get(currSongIndex);

            for (Track track : tracksToRemove) {
                if (track.equals(currPlayed)) {
                    sendOnSongCompleted();
                    player.reset();
                }
                songlist.remove(track);
            }

            currSongIndex = songlist.contains(currPlayed) ? songlist.indexOf(currPlayed) : -1;
            sendCurrentStateToPlaybackControl();
        }
    }

    public void changeOrder(int fromPosition, int toPosition, boolean sendState) {
        Track currPlayed = songlist.get(currSongIndex);
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(songlist, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(songlist, i, i - 1);
            }
        }
        currSongIndex = songlist.indexOf(currPlayed);
        if (sendState) sendCurrentStateToPlaybackControl();
    }

    public void setListTypePayload(Object listTypePayload) {
        if (listTypePayload == null || (listTypePayload == ListType.TRACK)) {
            currentListType = ListType.TRACK;
            listTypeId = -1;
        } else if (listTypePayload instanceof Album) {
            currentListType = ListType.ALBUM;
            listTypeId = ((Album) listTypePayload).getAId();
        } else if (listTypePayload instanceof Playlist) {
            currentListType = ListType.PLAYLIST;
            listTypeId = ((Playlist) listTypePayload).getPId();
        }
    }

    /*
     * Playback actions
     */

    private void play() {
        if (songlist.size() > 0 && currSongIndex >= 0) {
            player.reset();
            Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songlist.get(currSongIndex).getTId());
            try {
                player.setDataSource(getApplicationContext(), trackUri);
                player.attachAuxEffect(environmentalReverb.getId());
                player.setAuxEffectSendLevel(1f);
            } catch (IOException e) {
                Log.e("MUSIC-SERVICE", "Failed to set MediaPlayer-DataSource:", e);
            }
            player.prepareAsync();
        }
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
        player.setOnCompletionListener(this);
        sendCurrentStateToPlaybackControl();
        createNotification();
    }

    public void skip(PlaybackAction action) {
        sendOnSongCompleted();
        if (songlist.size() > 0 && currSongIndex >= 0) {
            int oldSongIndex = currSongIndex;
            if (action == PlaybackAction.SKIP_NEXT
                    || (action == PlaybackAction.SKIP_PREVIOUS && player.getCurrentPosition() < 2000)) {
                currSongIndex = ServiceUtil.getNextSongIndex(playbackBehaviour, action, songlist.size(), currSongIndex);
            }

            if ((oldSongIndex != currSongIndex || songlist.size() == 1)
                    && playbackBehaviour != PlaybackBehaviourState.SHUFFLE
                    && currSongIndex == 0
                    && (currentListType.equals(ListType.ALBUM) || currentListType.equals(ListType.PLAYLIST))) {
                sendOnListPlay();
            }

            play();
        }
    }

    public void playSong(Track track) {
        sendOnSongCompleted();
        currSongIndex = songlist.indexOf(track);
        play();
    }

    public void setProgress(int progress) {
        player.seekTo(progress);
    }

    public void setPlaybackBehaviour(PlaybackBehaviourState newState) {
        playbackBehaviour = newState;
        if (tmpState.currentPlayingTmp == null && !songlist.isEmpty() && newState == PlaybackBehaviourState.SHUFFLE) {
            currSongIndex = 0;
            skip(PlaybackAction.SKIP_NEXT);
        }
    }

    /*
    Audio Effects
     */

    public short[] getEqualizerBandLevels() {
        int numberBands = equalizer.getNumberOfBands();
        short[] bandLevels = new short[numberBands];
        for (short i = 0; i < numberBands; i++) {
            bandLevels[i] = equalizer.getBandLevel(i);
        }
        return bandLevels;
    }

    public EqualizerProperties getEqualizerProperties() {
        int[] centerFreq = new int[equalizer.getNumberOfBands()];
        for (short i = 0; i < centerFreq.length; i++) {
            centerFreq[i] = equalizer.getCenterFreq(i);
        }
        return new EqualizerProperties(equalizer.getNumberOfBands(), equalizer.getBandLevelRange(), centerFreq);
    }

    public void sendCurrentStateToPlaybackControl() {
        Track currSong = getCurrSong();
        sendCurrentStateToPlaybackControl(currSong);
    }

    public void sendCurrentStateToPlaybackControl(Track currSong) {
        Bundle bundle = new Bundle();

        bundle.putString("TITLE", (currSong != null) ? currSong.getTTitle() : "");
        bundle.putString("ARTIST", (currSong != null) ? currSong.getTArtist() : "");
        bundle.putInt("DURATION", (currSong != null) ? currSong.getTDuration() : 0);
        bundle.putLong("ID", (currSong != null) ? currSong.getTId() : -1);
        bundle.putInt("QUEUE_SIZE", songlist.size());
        bundle.putInt("QUEUE_INDEX", currSongIndex);
        bundle.putBoolean("ISONPAUSE", !player.isPlaying());
        bundle.putInt("BEHAVIOUR_STATE", PlaybackBehaviour.getStateAsInteger(playbackBehaviour));
        bundle.putInt("SESSION_ID", getSessionId());
        bundle.putInt("CURRENT_POSITION", (currSong != null) ? getCurrentPosition() : 0);
        bundle.putParcelableArrayList(getString(R.string.parcelable_track_list), songlist);

        sendBroadcast(new Intent().setAction(getString(R.string.playback_control_values)).putExtras(bundle));
    }

    private void sendPreparedSong() {
        Track currSong = getCurrSong();
        if (currSong != null) {
            Bundle bundle = new Bundle();
            bundle.putInt("ID", currSong.getTId());
            bundle.putInt("LIST_INDEX", currSongIndex);

            // Todo: Simplify
            if (tmpState.currentListTypeTmp == ListType.ALBUM) {
                bundle.putInt("ALBUM_ID", tmpState.currentListTypeIDTmp);
            } else if (tmpState.currentListTypeTmp == ListType.PLAYLIST) {
                bundle.putInt("PLAYLIST_ID", tmpState.currentListTypeIDTmp);
            }

            sendBroadcast(new Intent(this, SystemBroadcastReceiver.class).setAction(getString(R.string.musicservice_song_prepared)).putExtras(bundle));
        }
    }

    public void sendOnSongCompleted() {
        Track completedSong = getCurrSong();
        if (completedSong != null) {
            Bundle bundle = new Bundle();
            bundle.putInt("ID", completedSong.getTId());
            bundle.putInt("TYPE", currentListType.getTypeId());
            bundle.putLong("TIME_PLAYED", getCurrentPosition());

            sendBroadcast(new Intent(this, SystemBroadcastReceiver.class).setAction(getString(R.string.musicservice_song_ended)).putExtras(bundle));
        }
    }

    private void sendOnListPlay() {
        if (currentListType.equals(ListType.ALBUM)) {
            Bundle bundle = new Bundle();
            bundle.putInt("ALBUM_ID", listTypeId);
            sendBroadcast(new Intent(this, SystemBroadcastReceiver.class).setAction(getString(R.string.musicservice_album_play)).putExtras(bundle));
        } else if (currentListType.equals(ListType.PLAYLIST)) {
            Bundle bundle = new Bundle();
            bundle.putInt("PLAYLIST_ID", listTypeId);
            sendBroadcast(new Intent(this, SystemBroadcastReceiver.class).setAction(getString(R.string.musicservice_playlist_play)).putExtras(bundle));
        }
    }

    private static final class TmpState {
        private Track currentPlayingTmp = null;
        private ListType currentListTypeTmp = ListType.TRACK;
        private int currentListTypeIDTmp = -1;
    }
}
