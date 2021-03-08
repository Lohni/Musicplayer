package com.example.musicplayer.utils;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ID3V4Frame {

    private final int FRAME_HEADER_LENGTH = 10, FRAME_ENCODING_LENGHT = 1;

    private final int ISO88591 = 0, UTF16 = 1, UTF16BE = 2, UTF8=3;

    private String FRAME_CONTENT = "";
    private ID3V4FrameHeader frameHeader;

    private boolean isRead = false;

    public ID3V4Frame(byte[] data, ID3V4FrameHeader frameHeader){
        this.frameHeader = frameHeader;
        if (data != null)decodeFrame(data);
    }

    private void decodeFrame(byte[] data){
        if (frameHeader.FRAME_ID != null){
            switch (frameHeader.FRAME_ID){
                case ID3V2FrameIDs.TPE2:
                case ID3V2FrameIDs.TDRC:
                case ID3V2FrameIDs.TRCK:
                case ID3V2FrameIDs.TCON:
                case ID3V2FrameIDs.TCOM:
                case ID3V2FrameIDs.TIT2:
                case ID3V2FrameIDs.TALB: {
                    FRAME_CONTENT = getText(Arrays.copyOfRange(data, 1, frameHeader.FRAME_SIZE), (int) data[0]);
                    break;
                }
                default:{
                    FRAME_CONTENT = null;
                    break;
                }
            }
        }
    }

    public void setFrameContent(String content){
        FRAME_CONTENT = content;
        //+1 for encoding byte
        frameHeader.setNewFrameSize(content.length() + 1);
    }

    private String getText(byte[] text, int encoding){
        switch (encoding){
            case ISO88591:{
                return new String(text, StandardCharsets.ISO_8859_1);
            }
            case UTF16:{
                return new String(text, StandardCharsets.UTF_16);
            }
            case  UTF16BE:{
                return new String(text,StandardCharsets.UTF_16BE);
            }
            case UTF8:{
                return new String(text,StandardCharsets.UTF_8);
            }
            default:{
                return null;
            }
        }
    }

    public String getFrameContent(){
        return FRAME_CONTENT;
    }

    private byte[] getFrameContentAsBytes(int encoding){
        switch (encoding) {
            case ISO88591: {
                return FRAME_CONTENT.getBytes(StandardCharsets.ISO_8859_1);
            }
            case UTF16: {
                return FRAME_CONTENT.getBytes(StandardCharsets.UTF_16);
            }
            case UTF16BE: {
                return FRAME_CONTENT.getBytes(StandardCharsets.UTF_16BE);
            }
            case UTF8: {
                return FRAME_CONTENT.getBytes(StandardCharsets.UTF_8);
            }
            default: {
                return null;
            }
        }
    }

    //ISO88591 used as standard encoding
    public byte[] getFrameAsBytes(){
        byte[] header = frameHeader.toBytes();
        byte encoding = ISO88591;
        byte[] content = getFrameContentAsBytes(ISO88591);

        //+1 for encoding byte
        byte[] frame = new byte[header.length + 1 + content.length];
        System.arraycopy(header,0,frame,0,header.length);
        frame[10] = encoding;
        System.arraycopy(content,0,frame,header.length+1,content.length);

        return frame;
    }

    public int getFrameSize(){
        return FRAME_HEADER_LENGTH + FRAME_ENCODING_LENGHT + FRAME_CONTENT.length();
    }

    public String getFrameId(){
        return frameHeader.FRAME_ID;
    }

    public void setRead(boolean status){isRead=status;}

    public boolean getRead(){return isRead;}
}
