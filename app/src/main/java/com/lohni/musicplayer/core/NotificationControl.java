package com.lohni.musicplayer.core;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.lohni.musicplayer.MainActivity;
import com.lohni.musicplayer.R;
import com.lohni.musicplayer.database.entity.Track;
import com.lohni.musicplayer.utils.images.BitmapColorExtractor;

import androidx.core.app.NotificationCompat;

public class NotificationControl {
    private MediaMetadataCompat metadataCompat;
    private Bitmap customCoverImage, currentBitmap;
    private BitmapColorExtractor bitmapColorExtractor;

    private final int NOTIFICATION_ID = 123456, bitmapWidth, bitmapHeight;

    public NotificationControl(Context context, NotificationManager notificationmanager) {
        createNotificationChannel(notificationmanager);
        customCoverImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_baseline_music_note_24);
        bitmapColorExtractor = new BitmapColorExtractor(context, customCoverImage, Color.DKGRAY);
        bitmapWidth = context.getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_width);
        bitmapHeight = context.getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_height);
    }

    private void createNotificationChannel(NotificationManager notificationManager) {
        CharSequence name = "MUSICSERVICE_CHANNEL";
        NotificationChannel channel = new NotificationChannel(name.toString(), name, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("PLAYBACK_CONTROL");
        notificationManager.createNotificationChannel(channel);
    }

    public MediaMetadataCompat createMediaMetadataFromTrack(Track track) {
        String title = (track == null) ? "Select a song to play" : track.getTTitle();
        String artist = (track == null) ? "" : track.getTArtist();
        int duration = (track == null) ? 0 : track.getTDuration();

        metadataCompat = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, currentBitmap)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .build();
        return metadataCompat;
    }

    private void updateNotification(Context context, Track track) {
        byte[] thumbnail = null;
        if (track != null) {
            Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, track.getTId());
            try (MediaMetadataRetriever mmr = new MediaMetadataRetriever()) {
                mmr.setDataSource(context, trackUri);
                thumbnail = mmr.getEmbeddedPicture();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (thumbnail != null) {
            currentBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length), bitmapWidth, bitmapHeight, false);
            bitmapColorExtractor = new BitmapColorExtractor(context, currentBitmap);
        } else {
            currentBitmap = customCoverImage;
            bitmapColorExtractor = new BitmapColorExtractor(context, currentBitmap, Color.DKGRAY);
        }
    }

    public Notification createNotification(Context context, boolean isPlaying, MediaSessionCompat.Token sessionToken, Track track) {
        updateNotification(context, track);

        Intent nextIntent = new Intent(context.getString(R.string.playback_action_next));
        Intent pauseIntent = new Intent(context.getString(R.string.playback_action_pause));
        Intent playIntent = new Intent(context.getString(R.string.playback_action_play));
        Intent skipPreviousIntent = new Intent(context.getString(R.string.playback_action_previous));
        Intent controlIntent = isPlaying ? pauseIntent : playIntent;

        int controlIcon = (isPlaying) ? R.drawable.ic_pause_black_24dp : R.drawable.ic_play_arrow_black_24dp;

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "MUSICSERVICE_CHANNEL")
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(sessionToken).setShowActionsInCompactView(0, 1, 2))
                .setSmallIcon(R.drawable.ic_baseline_music_note_24)
                .setOnlyAlertOnce(true)
                .setAutoCancel(false)
                .setOngoing(true)
                .setColorized(true)
                .setColor(bitmapColorExtractor.getBackgroundColor())
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_IMMUTABLE))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setLargeIcon(metadataCompat.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART))
                .addAction(R.drawable.ic_skip_previous_black_24dp, "Previous", PendingIntent.getBroadcast(context, 2, skipPreviousIntent, PendingIntent.FLAG_IMMUTABLE))
                .addAction(controlIcon, "Play/Pause", PendingIntent.getBroadcast(context, 1, controlIntent, PendingIntent.FLAG_IMMUTABLE))
                .addAction(R.drawable.ic_skip_next_black_24dp, "Skip", PendingIntent.getBroadcast(context, 0, nextIntent, PendingIntent.FLAG_IMMUTABLE))
                .setContentTitle(metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE))
                .setContentText(metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ARTIST))
                .setVibrate(new long[]{0});

        return notificationBuilder.build();
    }

    public int getNOTIFICATION_ID() {
        return NOTIFICATION_ID;
    }
}
