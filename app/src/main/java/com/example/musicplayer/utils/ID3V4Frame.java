package com.example.musicplayer.utils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ID3V4Frame {

    private final int FRAME_HEADER_LENGTH = 10, FRAME_ENCODING_LENGHT = 1;

    private final int ISO88591 = 0, UTF16 = 1, UTF16BE = 2, UTF8=3;

    private String FRAME_CONTENT;
    private ID3V4FrameHeader frameHeader;

    //Only Frame-IDs which are used for this application are defined
    private final String TIT2="TIT2", //Title
                         TALB="TALB", //Album
                         TCOM="TCOM", //Composer
                         TCON="TCON", //Genre
                         TRCK="TRCK", //Track-ID
                         TDRC="TDRC", //Jahr
                         TPE1="TPE1", //Interpret
                         POPM="POPM"; //Rating

    public ID3V4Frame(byte[] data, ID3V4FrameHeader frameHeader){
        this.frameHeader = frameHeader;
        decodeFrame(data);
    }

    private void decodeFrame(byte[] data){
        if (frameHeader.FRAME_ID != null){
            switch (frameHeader.FRAME_ID){
                case TPE1:
                case TDRC:
                case TRCK:
                case TCON:
                case TCOM:
                case TIT2:
                case TALB: {
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

    public byte[] getFrameContentAsBytes(int encoding){
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
        byte[] header = frameHeader.getHeaderAsBytes();
        byte encoding = ISO88591;
        byte[] content = getFrameContentAsBytes(ISO88591);

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

}
