package com.example.musicplayer.utils.enums;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.example.musicplayer.R;

import androidx.core.content.res.ResourcesCompat;

public class DashboardEnumDeserializer {
    public static DashboardListType getDashboardListType(int type) {
        switch (type) {
            case 1: return DashboardListType.PLAYLIST;
            case 2: return DashboardListType.ALBUM;
            default: return DashboardListType.TRACK;
        }
    }

    public static DashboardFilterType getDashboardListFilter(int type) {
        switch (type) {
            case 0: return DashboardFilterType.FAVOURITE;
            case 1: return DashboardFilterType.LAST_PLAYED;
            default: return DashboardFilterType.TIMES_PLAYED;
        }
    }

    public static String getTitleForFilterType(DashboardFilterType filterType) {
        if (filterType == DashboardFilterType.FAVOURITE) {
            return "Favourite";
        } else if (filterType == DashboardFilterType.TIMES_PLAYED) {
            return "Most played";
        } else {
            return "Last played";
        }
    }

    public static String getTitleForListType(DashboardListType type) {
        switch (type) {
            case ALBUM: return "Album";
            case PLAYLIST: return "Playlist";
            default: return "Track";
        }
    }

    public static Drawable getTypeDrawable(Context context, DashboardListType type) {
        switch (type) {
            case ALBUM: return ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_album_black_24dp, null);
            case PLAYLIST: return ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_playlist_play_black_24dp, null);
            default: return ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_baseline_music_note_24, null);
        }
    }
}
