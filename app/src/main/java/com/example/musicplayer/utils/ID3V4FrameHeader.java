package com.example.musicplayer.utils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ID3V4FrameHeader {

    public int FRAME_SIZE, TAG_ALTER_PRESERVATION, FILE_ALTER_PRESERVATION,
            READONLY, GROUPING, COMPRESSION, ENCRYPTION, UNSYNCHRONISATION, DATA_LENGTH_INDICATOR;

    public String FRAME_ID;
    public byte[] header;

    public ID3V4FrameHeader(byte[] header){
        this.header = header;
        decodeHeader(header);
    }

    private void decodeHeader(byte[] header){
        FRAME_ID = new String(Arrays.copyOfRange(header,0,4), StandardCharsets.ISO_8859_1);
        getSize(header[4], header[5],header[6],header[7]);
        getFlags(header[8], header[9]);
    }

    private void getSize(byte size4, byte size3, byte size2, byte size1){
        size4 = (byte) (size4 << 1);
        size3 = (byte) (size3 << 1);
        size2 = (byte) (size2 << 1);
        size1 = (byte) (size1 << 1);

        FRAME_SIZE = (int) (((size4) << 24) + ((size3) << 17) + ((size2) << 10) + ((size1) << 3) >> 4);
    }

    private void getFlags(byte statusFlag, byte formatFlag){
        TAG_ALTER_PRESERVATION = (statusFlag >> 6) & 1;
        FILE_ALTER_PRESERVATION = (statusFlag >> 5) & 1;
        READONLY = (statusFlag >> 4) & 1;
        GROUPING = (formatFlag >> 7) & 1;
        COMPRESSION = (formatFlag >> 3) & 1;
        ENCRYPTION = (formatFlag >> 2) & 1;
        UNSYNCHRONISATION = (formatFlag >> 1) & 1;
        DATA_LENGTH_INDICATOR = formatFlag & 1;
    }

    public byte[] getHeaderAsBytes(){return header;}

}
