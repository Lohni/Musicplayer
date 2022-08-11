package com.example.musicplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.EnvironmentalReverb;
import android.media.audiofx.Equalizer;
import android.media.audiofx.LoudnessEnhancer;
import android.media.audiofx.Virtualizer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;

import com.example.musicplayer.core.SystemBroadcastReceiver;
import com.example.musicplayer.database.entity.EqualizerPreset;
import com.example.musicplayer.database.entity.Track;
import com.example.musicplayer.ui.audioeffects.EqualizerProperties;
import com.example.musicplayer.utils.enums.DashboardListType;
import com.example.musicplayer.utils.enums.PlaybackBehaviour;
import com.example.musicplayer.utils.images.BitmapColorExtractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.AudioManagerCompat;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private final ArrayList<Track> songlist = new ArrayList<>();
    private int currSongIndex;
    private PlaybackBehaviour.PlaybackBehaviourState playbackBehaviour = PlaybackBehaviour.PlaybackBehaviourState.REPEAT_LIST;
    private DashboardListType currentListType;

    private MediaPlayer player;
    private final IBinder mBinder = new MusicBinder();

    private EnvironmentalReverb environmentalReverb;
    private Equalizer equalizer;
    private BassBoost bassBoost;
    private Virtualizer virtualizer;
    private LoudnessEnhancer loudnessEnhancer;

    private MediaSessionCompat mediaSession;
    private MediaMetadataCompat metadataCompat;

    private int MEDIA_BUTTON_DOWN_COUNT = 0;
    Handler handler;
    Runnable mediaButtonCounterRunnable, createMetadataRunnable;

    private Bitmap customCoverImage, currentBitmap;
    private BitmapColorExtractor bitmapColorExtractor;
    private final int NOTIFICATION_ID = 123456;
    private int bitmapWidth, bitmapHeight;

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
        createNotificationChannel();

        customCoverImage = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_baseline_music_note_24);
        bitmapColorExtractor = new BitmapColorExtractor(this, customCoverImage, Color.DKGRAY);

        updateMediaMetatdata();
        createNotification();

        return START_STICKY;
    }

    private void updateMediaMetatdata() {
        if (songlist.isEmpty()) {
            metadataCompat = new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "Select a song to play")
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "")
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, currentBitmap)
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0)
                    .build();

        } else {
            metadataCompat = new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, songlist.get(currSongIndex).getTTitle())
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, songlist.get(currSongIndex).getTArtist())
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, currentBitmap)
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, player.getDuration())
                    .build();
        }
        mediaSession.setMetadata(metadataCompat);
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

    @Override
    public void onCreate() {
        bitmapWidth = getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_width);
        bitmapHeight = getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_height);

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
        this.registerReceiver(this.broadcastReceiver, filter);

        handler = new Handler();
        mediaButtonCounterRunnable = () -> MEDIA_BUTTON_DOWN_COUNT = 0;
        createMetadataRunnable = () -> {
            Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songlist.get(currSongIndex).getTId());
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(this, trackUri);
            byte[] thumbnail = mmr.getEmbeddedPicture();
            mmr.release();

            if (thumbnail != null) {
                currentBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length), bitmapWidth, bitmapHeight, false);
                bitmapColorExtractor = new BitmapColorExtractor(this, currentBitmap);
            } else {
                currentBitmap = customCoverImage;
                bitmapColorExtractor = new BitmapColorExtractor(this, currentBitmap, Color.DKGRAY);
            }

            updateMediaMetatdata();
            updateMediaSessionPlaybackState();
            createNotification();
        };

        mediaSession = new MediaSessionCompat(getBaseContext(), "MUSICPLAYER_MEDIASESSION");
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
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
                            skip();
                        }
                    }
                }
                return true;
            }
        });

        mediaSession.setActive(true);
        super.onCreate();
    }

    private void updateMediaSessionPlaybackState() {
        int state = (isPlaying()) ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(state, player.getCurrentPosition(), 1f)
                .build());
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
                    skip();
                } else if (intent.getAction().equals(getString(R.string.notification_action_previous))) {
                    skipPrevious();
                } else if (intent.getAction().equals(AudioManager.ACTION_HEADSET_PLUG)) {
                    int mode = intent.getIntExtra("state", 0);
                    if (mode == 0) {
                        pause();
                    }
                }

                int state = (isPlaying()) ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
                float speed = (isPlaying()) ? 1f : 0f;
                mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                        .setState(state, player.getCurrentPosition(), speed)
                        .build());

                createNotification();
            }
        }
    };

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        sendCurrentStateToPlaybackControl();
        sendPreparedSong();
        handler.post(createMetadataRunnable);
        resume();
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

    public void setSonglist(ArrayList<Track> list, DashboardListType dashboardListType) {
        sendOnSongCompleted();
        this.songlist.clear();
        this.songlist.addAll(list);
        currentListType = dashboardListType;
        sendCurrentStateToPlaybackControl();
    }

    public void addToSonglist(ArrayList<Track> toAdd) {
        this.songlist.addAll(toAdd);
        sendCurrentStateToPlaybackControl();
    }

    public void skip() {
        if (songlist.size() > 0) {
            sendOnSongCompleted();
            switch (playbackBehaviour) {
                case SHUFFLE:
                    Random random = new Random();
                    currSongIndex = random.nextInt(songlist.size());
                    break;
                case REPEAT_LIST:
                    currSongIndex++;
                    if (currSongIndex == songlist.size()) {
                        currSongIndex = 0;
                    }
                    break;
                case REPEAT_SONG:
                    break;
            }
            play();
        }
    }

    public void skipPrevious() {
        if (songlist.size() > 0) {
            sendOnSongCompleted();
            if (player.getCurrentPosition() < 2000) {
                switch (playbackBehaviour) {
                    case SHUFFLE:
                        Random random = new Random();
                        currSongIndex = random.nextInt(songlist.size());
                        break;
                    case REPEAT_LIST:
                        currSongIndex--;
                        if (currSongIndex < 0) {
                            currSongIndex = songlist.size() - 1;
                        }
                        break;
                    case REPEAT_SONG:
                        break;
                }
            }
            play();
        }
    }

    public void pause() {
        if (player.isPlaying()) {
            player.pause();
            sendCurrentStateToPlaybackControl();
            updateMediaSessionPlaybackState();
        }
    }

    public void resume() {
        player.start();
        player.setOnCompletionListener(this);
        sendCurrentStateToPlaybackControl();
        updateMediaSessionPlaybackState();
    }

    public void setSong(Track track) {
        if (player.isPlaying()) {
            sendOnSongCompleted();
        }

        for (int i = 0; i < songlist.size(); i++) {
            if (songlist.get(i).getTId().equals(track.getTId())) {
                currSongIndex = i;
            }
        }
        play();
    }

    public void setProgress(int progress) {
        player.seekTo(progress);
        PlaybackStateCompat state = new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, player.getCurrentPosition(), 1f)
                .build();
        mediaSession.setPlaybackState(state);
    }

    public void play() {
        if (songlist.size() > 0) {
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
            sendCurrentStateToPlaybackControl();
        }
    }

    public Track getCurrSong() {
        if (!songlist.isEmpty()) return songlist.get(currSongIndex);
        else return null;
    }

    public int getDuration() {
        if (player.isPlaying()) return player.getDuration();
        else return 0;
    }

    public int getCurrentPosition() {
        return player.getCurrentPosition();
    }

    public void setPlaybackBehaviour(PlaybackBehaviour.PlaybackBehaviourState newState) {
        playbackBehaviour = newState;
    }

    public int getSessionId() {
        return player.getAudioSessionId();
    }

    public void shuffle() {
        skip();
    }

    public boolean isPlaying() {
        if (player != null) {
            return player.isPlaying();
        }
        return false;
    }

    /*
    Audio Effects
     */

    public EnvironmentalReverb.Settings getReverbSettings() {
        return environmentalReverb.getProperties();
    }

    public void setEnvironmentalReverbSettings(EnvironmentalReverb.Settings settings) {
        environmentalReverb.setProperties(settings);
    }

    public boolean isReverbEnabled() {
        return environmentalReverb.getEnabled();
    }

    public void setReverbEnabled(boolean status) {
        if (environmentalReverb.getEnabled() != status) {
            environmentalReverb.setEnabled(status);
        }
    }

    public short[] getEqualizerBandLevels() {
        int numberBands = equalizer.getNumberOfBands();
        short[] bandLevels = new short[numberBands];
        for (short i = 0; i < numberBands; i++) {
            bandLevels[i] = equalizer.getBandLevel(i);
        }
        return bandLevels;
    }

    public void setEqualizerBandLevels(EqualizerPreset equalizerPreset) {
        short[] bandLevel = new short[5];

        bandLevel[0] = equalizerPreset.getEqLevel1().shortValue();
        bandLevel[1] = equalizerPreset.getEqLevel2().shortValue();
        bandLevel[2] = equalizerPreset.getEqLevel3().shortValue();
        bandLevel[3] = equalizerPreset.getEqLevel4().shortValue();
        bandLevel[4] = equalizerPreset.getEqLevel5().shortValue();

        if (equalizer.getNumberOfBands() == bandLevel.length) {
            for (short i = 0; i < bandLevel.length; i++) {
                equalizer.setBandLevel(i, bandLevel[i]);
            }
        }
    }

    public boolean isEqualizerEnabled() {
        return equalizer.getEnabled();
    }

    public void setEqualizerEnabled(boolean status) {
        if (equalizer.getEnabled() != status) {
            equalizer.setEnabled(status);
        }
    }

    public EqualizerProperties getEqualizerProperties() {
        int[] centerFreq = new int[equalizer.getNumberOfBands()];
        for (short i = 0; i < centerFreq.length; i++) {
            centerFreq[i] = equalizer.getCenterFreq(i);
        }
        return new EqualizerProperties(equalizer.getNumberOfBands(), equalizer.getBandLevelRange(), centerFreq);
    }

    public boolean isBassBoostEnabled() {
        return bassBoost.getEnabled();
    }

    public void setBassBoostEnabled(boolean state) {
        bassBoost.setEnabled(state);
    }

    public void setBassBoostStrength(short strength) {
        bassBoost.setStrength(strength);
    }

    public short getBassBoostStrength() {
        return bassBoost.getRoundedStrength();
    }

    public boolean isVirtualizerEnabled() {
        return virtualizer.getEnabled();
    }

    public void setVirtualizerEnabled(boolean state) {
        virtualizer.setEnabled(state);
    }

    public void setVirtualizerStrength(short strength) {
        virtualizer.setStrength(strength);
    }

    public short getVirtualizerStrength() {
        return virtualizer.getRoundedStrength();
    }

    public boolean isLoudnessEnhancerEnabled() {
        return loudnessEnhancer.getEnabled();
    }

    public void setLoudnessEnhancerEnabled(boolean state) {
        loudnessEnhancer.setEnabled(state);
    }

    public void setLoudnessEnhancerGain(int gain) {
        loudnessEnhancer.setTargetGain(gain);
    }

    public int getLoudnessEnhancerStrength() {
        return (int) loudnessEnhancer.getTargetGain();
    }


    private void createNotification() {
        Intent nextIntent = new Intent();
        nextIntent.setAction(getString(R.string.notification_action_next));

        Intent pauseIntent = new Intent();
        pauseIntent.setAction(getString(R.string.notification_action_pause));

        Intent playIntent = new Intent();
        playIntent.setAction(getString(R.string.notification_action_play));

        Intent skipPreviousIntent = new Intent();
        skipPreviousIntent.setAction(getString(R.string.notification_action_previous));

        int playPause = (isPlaying()) ? R.drawable.ic_pause_black_24dp : R.drawable.ic_play_arrow_black_24dp;
        Intent playPauseIntent = isPlaying() ? pauseIntent : playIntent;

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "MUSICSERVICE_CHANNEL")
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.getSessionToken()).setShowActionsInCompactView(0, 1, 2))
                .setSmallIcon(R.drawable.ic_baseline_music_note_24)
                .setOnlyAlertOnce(true)
                .setAutoCancel(false)
                .setOngoing(true)
                .setColorized(true)
                .setColor(bitmapColorExtractor.getBackgroundColor())
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setLargeIcon(metadataCompat.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART))
                .addAction(R.drawable.ic_skip_previous_black_24dp, "Previous", PendingIntent.getBroadcast(this, 2, skipPreviousIntent, PendingIntent.FLAG_IMMUTABLE))
                .addAction(playPause, "Play/Pause", PendingIntent.getBroadcast(this, 1, playPauseIntent, PendingIntent.FLAG_IMMUTABLE))
                .addAction(R.drawable.ic_skip_next_black_24dp, "Skip", PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_IMMUTABLE))
                .setContentTitle(metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE))
                .setContentText(metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ARTIST))
                .setVibrate(new long[]{0});

        Notification n = notificationBuilder.build();
        startForeground(NOTIFICATION_ID, n);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "MUSICSERVICE_CHANNEL";
            String description = "PLAYBACK_CONTROL";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("MUSICSERVICE_CHANNEL", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void sendCurrentStateToPlaybackControl() {
        Track currSong = getCurrSong();
        if (currSong != null) {
            Bundle bundle = new Bundle();

            bundle.putString("TITLE", currSong.getTTitle());
            bundle.putString("ARTIST", currSong.getTArtist());
            bundle.putInt("DURATION", currSong.getTDuration());
            bundle.putLong("ID", currSong.getTId());

            bundle.putInt("QUEUE_SIZE", songlist.size());
            bundle.putInt("QUEUE_INDEX", currSongIndex);
            bundle.putBoolean("ISONPAUSE", !player.isPlaying());
            bundle.putInt("BEHAVIOUR_STATE", PlaybackBehaviour.getStateAsInteger(playbackBehaviour));
            bundle.putInt("SESSION_ID", getSessionId());
            bundle.putInt("CURRENT_POSITION", getCurrentPosition());

            sendBroadcast(new Intent().setAction(getString(R.string.playback_control_values)).putExtras(bundle));
        }
    }

    private void sendPreparedSong() {
        Track currSong = getCurrSong();
        if (currSong != null) {
            Bundle bundle = new Bundle();
            bundle.putInt("ID", currSong.getTId());
            sendBroadcast(new Intent(this, SystemBroadcastReceiver.class).setAction(getString(R.string.musicservice_song_prepared)).putExtras(bundle));
        }
    }

    public void sendOnSongCompleted() {
        Bundle bundle = getOnSongCompletedBundle();
        if (bundle != null) {
            sendBroadcast(new Intent(this, SystemBroadcastReceiver.class).setAction(getString(R.string.musicservice_song_ended)).putExtras(bundle));
        }
    }

    private Bundle getOnSongCompletedBundle() {
        Track completedSong = getCurrSong();
        if (completedSong != null) {
            Bundle bundle = new Bundle();
            bundle.putInt("ID", completedSong.getTId());
            bundle.putInt("TYPE", currentListType.getTypeId());
            bundle.putLong("TIME_PLAYED", getCurrentPosition());
            return bundle;
        }
        return null;
    }
}
