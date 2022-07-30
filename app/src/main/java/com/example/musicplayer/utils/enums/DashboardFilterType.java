package com.example.musicplayer.utils.enums;

public enum DashboardFilterType {
    FAVOURITE(0),
    LAST_PLAYED(1),
    TIMES_PLAYED(2);

    private final int filterType;

    DashboardFilterType(int filterType) {
        this.filterType = filterType;
    }

    public int getFilterType() {
        return filterType;
    }
}
