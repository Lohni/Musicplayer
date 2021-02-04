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
    private byte[] audioTag;

    public TagWriter(Context context, TagResolver track, ID3V2TagHeader tagHeader){
        this.context = context;
        this.track = track;
        this.tagHeader = tagHeader;
    }

    private void writeToFile() throws IOException {
        InputStream is = context.getContentResolver().openInputStream(
                ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, track.getTrackId()));

        int oldTagSize = tagHeader.getTAG_SIZE();
        tagHeader.setNewTagSize(getNewTagSize());
        byte[] newTag = new byte[tagHeader.getTAG_SIZE() + tagHeader.getTagHeaderLength()];

        //Skip Header from File
        int offset = tagHeader.getTagHeaderLength();

        byte[] tagHeaderBytes = tagHeader.toBytes();
        for (int i = 0; i<tagHeaderBytes.length;i++){
            newTag[i] = tagHeaderBytes[i];
        }

        byte[] data = new byte[oldTagSize];
        is.read(data, offset, data.length);
        is.close();

       ByteArrayInputStream bis = new ByteArrayInputStream(data);

       while (bis.available() > 0){

           byte[] rawFrameHeader = new byte[10];
           bis.read(rawFrameHeader, 0, rawFrameHeader.length);
           ID3V4FrameHeader frameHeader = new ID3V4FrameHeader(rawFrameHeader);
           int frameSize = frameHeader.FRAME_SIZE;
           byte[] frameData = new byte[frameSize];
           bis.read(frameData,0,frameSize);
           if (!isFrameRelevant(frameHeader.FRAME_ID)){
               for (int i = 0; i<rawFrameHeader.length;i++){
                   newTag[offset + i] = rawFrameHeader[i];
                   offset++;
               }

               for (int i = 0; i<frameData.length;i++){
                   newTag[offset + i] = frameData[i];
                   offset++;
               }

           } else {
               byte[] frame = getFrameData(frameHeader.FRAME_ID);
               for (int i = 0; i<frame.length;i++){
                   newTag[offset + i] = frame[i];
                   offset++;
               }
           }
       }
       bis.close();
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

    private byte[] getFrameData(String frameID){
        switch (frameID){
            case ID3V2FrameIDs.TPE1:{
                return track.getArtistFrame().getFrameAsBytes();
            }
            case ID3V2FrameIDs.TDRC:{
                return track.getYearFrame().getFrameAsBytes();
            }
            case ID3V2FrameIDs.TRCK:{
                return track.getTrackIdFrame().getFrameAsBytes();
            }
            case ID3V2FrameIDs.TCON:{
                return track.getGenreFrame().getFrameAsBytes();
            }
            case ID3V2FrameIDs.TCOM:{
                return track.getComposerFrame().getFrameAsBytes();
            }
            case ID3V2FrameIDs.TIT2:{
                return track.getTitleFrame().getFrameAsBytes();
            }
            case ID3V2FrameIDs.TALB: {
                return track.getAlbumFrame().getFrameAsBytes();
            }
            default:{
                return null;
            }
        }
    }

    private int getNewTagSize(){
        int tagSizeDiff = track.getChangedContentSize();
        return tagHeader.getTAG_SIZE() + tagSizeDiff;
    }

}
