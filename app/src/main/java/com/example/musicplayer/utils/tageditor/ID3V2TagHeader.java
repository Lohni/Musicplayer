package com.example.musicplayer.utils.tageditor;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ID3V2TagHeader {

    private int TAG_SIZE = 0;
    private int TAG_VERSION_MAJOR;
    private int TAG_VERSION_REVISION = 0;
    private int UNSYNCHRONISATION = 0, EXTENDED_HEADER = 0, EXPERIMENTAL_INDICATOR = 0, FOOTER = 0;
    private int TAG_HEADER_LENGTH = 10;

    public ID3V2TagHeader(byte[] header){
        decodeHeader(header);
    }

    public ID3V2TagHeader(){
        TAG_VERSION_MAJOR = 4;
    }

    private void decodeHeader(byte[] header){
        TAG_VERSION_MAJOR = getMajorVersion(header[3]);
        TAG_VERSION_REVISION = getRevision(header[4]);
        getFlags(header[5]);
        decodeSize(Arrays.copyOfRange(header, 6, 10));
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
        if (EXTENDED_HEADER == 0)TAG_HEADER_LENGTH = 10;
    }

    private boolean decodeSize(byte[] size){
        if (size.length != 4)return false;

        int b1 = size[0] << 21;
        int b2 = size[1] << 14;
        int b3 = size[2] << 7;
        int b4 = size[3];

        TAG_SIZE = b1 + b2 + b3 + b4;
        return true;
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

    public int getTagHeaderLength(){return TAG_HEADER_LENGTH;}

    public void setNewTagSize(int newLength){
        TAG_SIZE = newLength;
    }

    public byte[] toBytes(){
        byte[] header = new byte[TAG_HEADER_LENGTH];
        byte[] tagIdentifier = "ID3".getBytes(StandardCharsets.ISO_8859_1);
        header[0] = tagIdentifier[0];
        header[1] = tagIdentifier[1];
        header[2] = tagIdentifier[2];
        header[3] = (byte) 4; //Allways write tag as version 4
        header[4] = (byte) TAG_VERSION_REVISION;
        header[5] = getFlagsAsByte();
        byte[] encodedSizeBytes = getEncodedSize();
        header[6] = encodedSizeBytes[0];
        header[7] = encodedSizeBytes[1];
        header[8] = encodedSizeBytes[2];
        header[9] = encodedSizeBytes[3];

        return header;
    }

    private byte getFlagsAsByte(){
        return (byte) ((((byte) UNSYNCHRONISATION) << 7) + (((byte) EXTENDED_HEADER) << 6) + (((byte) EXPERIMENTAL_INDICATOR) << 5) + (((byte) FOOTER) << 4));
    }

    private byte[] getEncodedSize(){
        //Split to Byte-Chunks
        byte[] size = new byte[4];
        size[0] = (byte) (TAG_SIZE >> 24);
        size[1] = (byte) (TAG_SIZE >> 16);
        size[2] = (byte) (TAG_SIZE >> 8);
        size[3] = (byte) (TAG_SIZE);

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
}
