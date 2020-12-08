package com.example.musicplayer.utils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ID3V4Frame {

    private int FRAME_HEADER_LENGTH = 10;

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

}
