package com.example.musicplayer.utils.enums;

public class DashboardEnumDeserializer {
    public static DashboardListType getDashboardListType(int type) {
        switch (type) {
            case 1: return DashboardListType.PLAYLIST;
            case 2: return DashboardListType.ALBUM;
            default: return DashboardListType.TRACK;
        }
    }

    public static ListFilterType getListFilterTypeByInt(int type) {
        switch (type) {
            case 0: return ListFilterType.FAVOURITE;
            case 1: return ListFilterType.LAST_PLAYED;
            case 3: return ListFilterType.ALPHABETICAL;
            case 4: return ListFilterType.TIME_PLAYED;
            case 5: return ListFilterType.LAST_CREATED;
            default: return ListFilterType.TIMES_PLAYED;
        }
    }

    public static String getTitleForFilterType(ListFilterType filterType) {
        switch (filterType) {
            case FAVOURITE: return "Favourite";
            case LAST_PLAYED: return "Last played";
            case ALPHABETICAL: return "Alphabetical";
            case TIME_PLAYED: return "Time played";
            case LAST_CREATED: return "Last created";
            default: return "Times played";
        }
    }

    public static String getTitleForListType(DashboardListType type) {
        switch (type) {
            case ALBUM: return "Album";
            case PLAYLIST: return "Playlist";
            default: return "Track";
        }
    }
}
