package com.example.musicplayer.utils;

import android.content.ContentUris;
import android.content.Context;
import android.provider.MediaStore;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class TagWriter {

    private TagResolver track;
    private ID3V2TagHeader tagHeader;
    private Context context;

    public TagWriter(Context context, TagResolver track, ID3V2TagHeader tagHeader){
        this.context = context;
        this.track = track;
        this.tagHeader = tagHeader;
    }

    private void writeToFile() throws IOException {
        InputStream is = context.getContentResolver().openInputStream(
                ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.parseLong(track.getTrackid())));

        byte[] header = new byte[10];
        is.read(header, 0, header.length);
        tagHeader = new ID3V2TagHeader(header);

        byte[] data = new byte[tagHeader.getTAG_SIZE()];
        is.read(data, 0, data.length);
        is.close();
       if (tagHeader.getEXTENDED_HEADER() == 0) {

           ByteArrayInputStream bis = new ByteArrayInputStream(data);
           track = new TagResolver();
           int framePos = 0;

           while (bis.available() > 0){

               int bytesRead = bis.read(header,0,10);
               framePos += bytesRead;
               if (bytesRead == 10){
                   ID3V4FrameHeader frameHeader = new ID3V4FrameHeader(header);
                   if (isFrameRelevant(frameHeader.FRAME_ID)){

                   }
                   int frameSize = frameHeader.FRAME_SIZE;
                   byte[] frameData = new byte[frameSize];

                   bis.read(frameData,0,frameSize);
                   ID3V4Frame frame = new ID3V4Frame(frameData, frameHeader);
                   if (frame.getFrameContent() != null){
                       setTrackData(frame.getFrameContent(), frameHeader.FRAME_ID, framePos);
                   }
                   framePos += frameSize;
               }
           }
           bis.close();
       }
    }

    private boolean isFrameRelevant(String frameID){
        switch (frameID){
            case ID3V2FrameIDs.TPE1:
            case ID3V2FrameIDs.TDRC:
            case ID3V2FrameIDs.TRCK:
            case ID3V2FrameIDs.TCON:
            case ID3V2FrameIDs.TCOM:
            case ID3V2FrameIDs.TIT2:
            case ID3V2FrameIDs.TALB: {
                return true;
            }
            default:{
                return false;
            }
        }
    }

    private boolean hasFrameChanged(){
        return true;
    }

}
