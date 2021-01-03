package com.example.musicplayer.utils;

import java.util.Arrays;

public class ID3V2TagHeader {

    private int TAG_SIZE;
    private int TAG_VERSION_MAJOR;
    private int TAG_VERSION_REVISION;
    private int UNSYNCHRONISATION, EXTENDED_HEADER, EXPERIMENTAL_INDICATOR, FOOTER;

    public ID3V2TagHeader(byte[] header){
        decodeHeader(header);
    }

    private void decodeHeader(byte[] header){
        TAG_VERSION_MAJOR = getMajorVersion(header[3]);
        TAG_VERSION_REVISION = getRevision(header[4]);
        getFlags(header[5]);
        decodeSize(Arrays.copyOfRange(header, 6, 9));
    }

    private int getMajorVersion(byte versionMajor){
        return (int) versionMajor;
    }

    private int getRevision(byte versionRevision){
        return (int) versionRevision;
    }

    private void getFlags(byte flag){
        UNSYNCHRONISATION = flag >> 7;
        EXTENDED_HEADER = flag >> 6;
        EXPERIMENTAL_INDICATOR = flag >> 5;
        FOOTER = flag >> 4;
    }

    private boolean decodeSize(byte[] size){
        if (size.length == 4){
            byte byte1 = (byte) (size[0] << 1);
            byte byte2 = (byte) (size[1] << 1);
            byte byte3 = (byte) (size[2] << 1);
            byte byte4 = (byte) (size[3] << 1);

            TAG_SIZE = (int) (((byte1) << 24) + ((byte2) << 17) + ((byte3) << 10) + ((byte4) << 3) >> 4);

            return true;
        } else return false;
    }

    public int getTAG_SIZE() {
        return TAG_SIZE;
    }

    public int getTAG_VERSION_MAJOR() {
        return TAG_VERSION_MAJOR;
    }

    public int getTAG_VERSION_REVISION() {
        return TAG_VERSION_REVISION;
    }

    public int getUNSYNCHRONISATION() {
        return UNSYNCHRONISATION;
    }

    public int getEXTENDED_HEADER() {
        return EXTENDED_HEADER;
    }

    public int getEXPERIMENTAL_INDICATOR() {
        return EXPERIMENTAL_INDICATOR;
    }

    public int getFOOTER() {
        return FOOTER;
    }
}
