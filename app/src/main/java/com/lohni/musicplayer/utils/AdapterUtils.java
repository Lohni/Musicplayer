package com.lohni.musicplayer.utils;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.recyclerview.widget.RecyclerView;

import com.lohni.musicplayer.database.entity.Track;
import com.lohni.musicplayer.utils.images.ImageUtil;

import java.util.HashMap;
import java.util.List;

public class AdapterUtils {
    public synchronized static void loadCoverImagesAsync(Context context, List<Track> newList, RecyclerView recyclerView, HashMap<Integer, Drawable> targetMap) {
        new Thread(() -> {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            int count = 0;
            for (Track track : newList) {
                Integer trackId = track.getTId();
                Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, trackId);
                try {
                    if (!targetMap.containsKey(trackId)) {
                        mmr.setDataSource(context, trackUri);
                        byte[] thumbnail = mmr.getEmbeddedPicture();
                        if (thumbnail != null) {
                            Bitmap cover = BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length);
                            Drawable drawable = ImageUtil.roundCorners(cover, context.getResources());
                            targetMap.put(trackId, drawable);
                        }
                    }
                } catch (IllegalArgumentException eF) {
                    System.out.println("MediaMetadataRetriever IllegalArgument");
                }

                int finalCount = count;
                recyclerView.post(() -> recyclerView.getAdapter().notifyItemChanged(finalCount, "RELOAD_IMAGES"));
                count++;
            }
        }).start();
    }
}
