package com.example.musicplayer.utils.enums;

public enum DashboardListType {
    TRACK(0),
    PLAYLIST(1),
    ALBUM(2);

    private final int typeId;

    DashboardListType(int typeId) {
        this.typeId = typeId;
    }

    public int getTypeId() {
        return typeId;
    }
}
