package com.lohni.musicplayer.utils.enums;

public enum ListType {
    TRACK(0),
    PLAYLIST(1),
    ALBUM(2);

    private final int typeId;

    ListType(int typeId) {
        this.typeId = typeId;
    }

    public int getTypeId() {
        return typeId;
    }
}
