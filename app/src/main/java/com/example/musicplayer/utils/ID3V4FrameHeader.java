package com.example.musicplayer.utils;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ID3V4FrameHeader {

    public int FRAME_SIZE, TAG_ALTER_PRESERVATION, FILE_ALTER_PRESERVATION,
            READONLY, GROUPING, COMPRESSION, ENCRYPTION, UNSYNCHRONISATION, DATA_LENGTH_INDICATOR;

    public String FRAME_ID;

    private byte statusFlag, formatFlag;

    public ID3V4FrameHeader(byte[] header){
        decodeHeader(header);
    }

    public ID3V4FrameHeader(){
        FRAME_SIZE = 0;
        byte def = 0b0;
        getFlags(def, def);
    }

    private void decodeHeader(byte[] header){
        FRAME_ID = new String(Arrays.copyOfRange(header,0,4), StandardCharsets.ISO_8859_1);
        getSize(header[4], header[5],header[6],header[7]);
        getFlags(header[8], header[9]);
    }

    private void getSize(byte size4, byte size3, byte size2, byte size1){
        int b1 = size4 << 21;
        int b2 = size3 << 14;
        int b3 = size2 << 7;
        int b4 = size1;

        FRAME_SIZE = b1 + b2 + b3 + b4;
    }

    private void getFlags(byte statusFlag, byte formatFlag){
        this.statusFlag = statusFlag;
        this.formatFlag = formatFlag;
        TAG_ALTER_PRESERVATION = (statusFlag >> 6) & 1;
        FILE_ALTER_PRESERVATION = (statusFlag >> 5) & 1;
        READONLY = (statusFlag >> 4) & 1;
        GROUPING = (formatFlag >> 7) & 1;
        COMPRESSION = (formatFlag >> 3) & 1;
        ENCRYPTION = (formatFlag >> 2) & 1;
        UNSYNCHRONISATION = (formatFlag >> 1) & 1;
        DATA_LENGTH_INDICATOR = formatFlag & 1;
    }

    //Todo: return real values
    public byte[] toBytes(){
        byte[] header = new byte[10];
        byte[] id = FRAME_ID.getBytes(StandardCharsets.UTF_8);
        byte[] size = getEncodedSize();
        header[0] = id[0];
        header[1] = id[1];
        header[2] = id[2];
        header[3] = id[3];
        header[4] = size[0];
        header[5] = size[1];
        header[6] = size[2];
        header[7] = size[3];
        header[8] = statusFlag;
        header[9] = formatFlag;

        return header;
    }

    private byte[] getEncodedSize(){
        //Split to Byte-Chunks
        byte[] size = new byte[4];
        size[0] = (byte) (FRAME_SIZE >> 24);
        size[1] = (byte) (FRAME_SIZE >> 16);
        size[2] = (byte) (FRAME_SIZE >> 8);
        size[3] = (byte) (FRAME_SIZE);
        // 0x7F -> Clear MSB
        int overFlow3 = (size[3] & 0xFF) >> 7;
        byte syncByte3 = (byte) (size[3] & 0x7F);

        int overFlow2 = (size[2] & 0xFF) >> 6;
        byte syncByte2 = (byte) (((size[2] << 1) + overFlow3) & 0x7F);

        int overFlow1 = (size[1] & 0xFF) >> 5;
        byte syncByte1 = (byte) (((size[1] << 2) + overFlow2) & 0x7F);

        byte syncByte0 = (byte) (((size[0] << 3) + overFlow1) & 0x7F);
        return new byte[] {syncByte0, syncByte1, syncByte2, syncByte3};
    }

    public void setNewFrameSize(int size){FRAME_SIZE = size;}

    public boolean setFrameID(String id){
        if (id.length() != 4)return false;
        this.FRAME_ID = id;
        return true;
    }
}
