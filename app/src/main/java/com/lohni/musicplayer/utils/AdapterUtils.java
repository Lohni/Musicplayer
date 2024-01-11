package com.lohni.musicplayer.utils;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;

import com.lohni.musicplayer.core.ApplicationDataViewModel;
import com.lohni.musicplayer.database.dto.AlbumDTO;
import com.lohni.musicplayer.database.dto.AlbumTrackDTO;
import com.lohni.musicplayer.database.dto.TrackDTO;
import com.lohni.musicplayer.database.entity.Track;
import com.lohni.musicplayer.utils.enums.ListFilterType;
import com.lohni.musicplayer.utils.images.ImageUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import androidx.recyclerview.widget.RecyclerView;

public class AdapterUtils {
    public static void loadCoverImagesAsync(Context context, List<Track> newList, ApplicationDataViewModel vm) {
        HashMap<Integer, Drawable> drawableHashMap = new HashMap<>(newList.size());
        int numThreads = (newList.size() < 50) ? 1 : Math.min(10, newList.size() / 50);
        int batch = (int) Math.ceil((float) newList.size() / numThreads);

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads + 1);
        for (int i = 0; i < numThreads; i++) {
            List<Track> subList = newList.subList(i * batch, Math.min((i + 1) * batch, newList.size() - 1));
            executor.execute(() -> {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                HashMap<Integer, Drawable> threadHashMap = new HashMap<>(newList.size());

                for (Track track : subList) {
                    Integer trackId = track.getTId();
                    Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, trackId);
                    try {
                        mmr.setDataSource(context, trackUri);
                        byte[] thumbnail = mmr.getEmbeddedPicture();
                        if (thumbnail != null) {
                            Bitmap cover = BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length);
                            Drawable drawable = ImageUtil.roundCorners(cover, context.getResources());
                            threadHashMap.put(trackId, drawable);
                        }
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                try {
                    addDrawables(drawableHashMap, threadHashMap);
                    mmr.release();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        executor.execute(() -> {
            while (executor.getActiveCount() > 1) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            vm.addImageDrawables(drawableHashMap);
        });
    }

    private synchronized static void addDrawables(HashMap<Integer, Drawable> drawableHashMap, HashMap<Integer, Drawable> threadHashMap) {
        drawableHashMap.putAll(threadHashMap);
    }

    public static void loadAlbumCoverImagesAsync(Context context, List<AlbumTrackDTO> newList, ApplicationDataViewModel vm) {
        new Thread(() -> {
            HashMap<Integer, Drawable> drawableHashMap = new HashMap<>();
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            for (AlbumTrackDTO albumTrackDTO : newList) {
                List<Bitmap> coverList = new ArrayList<>();
                for (Track track : albumTrackDTO.trackList) {
                    Integer trackId = track.getTId();
                    Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, trackId);
                    try {
                        mmr.setDataSource(context, trackUri);
                        byte[] thumbnail = mmr.getEmbeddedPicture();
                        if (thumbnail != null) {
                            Bitmap cover = BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length);
                            if (coverList.stream().noneMatch(bmp -> ImageUtil.calSimilarity(bmp, cover) > 0.90)) {
                                coverList.add(cover);
                            }
                        }
                    } catch (IllegalArgumentException ignored) {
                    }

                    if (coverList.size() >= 4) break;
                }
                ImageUtil.createBitmapCollection(coverList, context).ifPresent(coll -> drawableHashMap.put(albumTrackDTO.album.getAId(), coll));
            }
            vm.addAlbumDrawable(drawableHashMap);
            try {
                mmr.release();
            } catch (IOException ignored) {
            }
        }).start();
    }

    public static String getDescription(TrackDTO trackDTO, ListFilterType type) {
        if (trackDTO.getSize() != null || type.equals(ListFilterType.LAST_CREATED)) {
            if (type.equals(ListFilterType.LAST_PLAYED)) {
                LocalDateTime ldt = LocalDateTime.parse(trackDTO.getSize(), GeneralUtils.DB_TIMESTAMP);
                return GeneralUtils.getTimeDiffAsText(ldt);
            } else if (type.equals(ListFilterType.LAST_CREATED) && trackDTO.getTrack().getTCreated() != null) {
                LocalDateTime dbTime = LocalDateTime.parse(trackDTO.getTrack().getTCreated(), GeneralUtils.DB_TIMESTAMP);
                return GeneralUtils.getTimeDiffAsText(dbTime);
            } else if (type.equals(ListFilterType.TIMES_PLAYED)) {
                return trackDTO.getSize();
            } else if (type.equals(ListFilterType.TIME_PLAYED)) {
                try {
                    return GeneralUtils.convertTimeWithUnit(Integer.parseInt(trackDTO.getSize()));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return "";
    }

    public static String getDescription(AlbumDTO albumDTO, ListFilterType type) {
        if (albumDTO.getSize() != null || type.equals(ListFilterType.LAST_CREATED)) {
            if (type.equals(ListFilterType.LAST_PLAYED)) {
                LocalDateTime ldt = LocalDateTime.parse(albumDTO.getSize(), GeneralUtils.DB_TIMESTAMP);
                return GeneralUtils.getTimeDiffAsText(ldt);
            } else if (type.equals(ListFilterType.LAST_CREATED)) {
                LocalDateTime dbTime = LocalDateTime.parse(albumDTO.getAlbum().album.getACreated(), GeneralUtils.DB_TIMESTAMP);
                return GeneralUtils.getTimeDiffAsText(dbTime);
            } else if (type.equals(ListFilterType.TIMES_PLAYED)) {
                return albumDTO.getSize();
            } else if (type.equals(ListFilterType.TIME_PLAYED)) {
                try {
                    return GeneralUtils.convertTimeWithUnit(Integer.parseInt(albumDTO.getSize()));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return "";
    }

    public static int moveItemToNewPositionList(List<TrackDTO> oldList, List<TrackDTO> newList, RecyclerView.Adapter adapter) {
        int toPos = 0, targetId = -1;
        if (oldList.size() == newList.size()) {
            for (int i = 0; i < oldList.size(); i++) {
                if (!newList.get(i).getTrack().getTId().equals(oldList.get(i).getTrack().getTId())) {
                    toPos = i;
                    targetId = newList.get(i).getTrack().getTId();
                    break;
                }
            }

            if (targetId >= 0) {
                int fromPos = 0;
                for (int i = 0; i < oldList.size(); i++) {
                    if (oldList.get(i).getTrack().getTId().equals(targetId)) {
                        fromPos = i;
                        break;
                    }
                }

                adapter.notifyItemMoved(fromPos, toPos);
            }
        }
        return toPos;
    }

    public static <T> boolean compareLists(List<T> first, List<T> second) {
        if (first.size() != second.size()) return false;

        for (int i = 0; i < first.size(); i++) {
            if (!first.get(i).equals(second.get(i))) return false;
        }

        return true;
    }
}
