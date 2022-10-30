package com.lohni.musicplayer.utils.enums;

public enum ID3FrameId {
    APIC("APIC"), //Picture
    TPE2("TPE1"), //Artist
    TIT2("TIT2"), //Title
    TALB("TALB"), //Album
    TCOM("TCOM"), //Composer
    TCON("TCON"), //Genre
    TRCK("TRCK"), //TrackId
    TDRC("TDRC"), //Jahr
    POPM("POPM"), //Rating
    DEFAULT(""); //not-needed frames

    private final String frameId;

    ID3FrameId(String frameId){
        this.frameId = frameId;
    }

    public String getFrameId(){return frameId;}
}
