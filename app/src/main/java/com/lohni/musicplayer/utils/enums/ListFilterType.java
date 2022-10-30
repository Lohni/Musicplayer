package com.lohni.musicplayer.utils.enums;

public enum ListFilterType {
    FAVOURITE(0, "FAV"),
    LAST_PLAYED(1, "LAST"),
    TIMES_PLAYED(2, "TIMES"),
    ALPHABETICAL(3, "A-Z"),
    TIME_PLAYED(4, "TIME"),
    LAST_CREATED(5, "CREATE");

    private final int filterType;
    private final String id;

    ListFilterType(int filterType, String id) {
        this.filterType = filterType;
        this.id = id;
    }

    public int getFilterType() {
        return filterType;
    }

    public String getId(){return id;}
}
