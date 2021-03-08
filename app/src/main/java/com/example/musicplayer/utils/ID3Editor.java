package com.example.musicplayer.utils;

import android.content.Context;
import android.net.Uri;
import android.nfc.Tag;

import com.example.musicplayer.entities.MusicResolver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class ID3Editor {

    //Size in bytes
    private static final int HEADER_SIZE = 10;
    private long trackID;
    private ID3V2TagHeader tagHeader;
    private TagResolver track;

    private Context context;
    private ID3EditorInterface id3EditorInterface;

    public ID3Editor(Uri uri, Context context, long trackID, ID3EditorInterface id3EditorInterface){
        this.context = context;
        this.trackID = trackID;
        this.id3EditorInterface = id3EditorInterface;
        decode(uri);
    }

    private boolean decode(Uri uri){
        InputStream is;
        try {
            is = context.getContentResolver().openInputStream(uri);

            byte[] header = new byte[HEADER_SIZE];
            is.read(header, 0, header.length);

            if (checkIfV2TagIsPresent(Arrays.copyOfRange(header,0, 3))){
                tagHeader = new ID3V2TagHeader(header);

                byte[] data = new byte[tagHeader.getTAG_SIZE()];
                is.read(data, 0, data.length);


                switch (tagHeader.getTAG_VERSION_MAJOR()){
                    case 2:{

                    }
                    case 3:{

                    }
                    case 4:{
                        getV4Frames(data);
                    }
                    default:{

                    }
                }
            } else {
                tagHeader = new ID3V2TagHeader();
            }
            is.close();

        } catch (FileNotFoundException e){
            System.out.println(e);
        } catch (IOException e){
            System.out.println(e);
        }
        return true;
    }

    private void processFrames(byte[] data){
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            track = new TagResolver(trackID);

            byte[] header = new byte[10];
            while (bis.available() > 0){

                int bytesRead = bis.read(header,0,HEADER_SIZE);
                if (bytesRead == HEADER_SIZE){
                    ID3V4FrameHeader frameHeader = new ID3V4FrameHeader(header);
                    int frameSize = frameHeader.FRAME_SIZE;
                    byte[] frameData = new byte[frameSize];
                    bis.read(frameData,0,frameSize);

                    if (frameHeader.FRAME_ID.equals("APIC")){
                        track.setFrame(new ID3V4APICFrame(frameData, frameHeader));
                    }

                    ID3V4Frame frame = new ID3V4Frame(frameData, frameHeader);
                    if (frame.getFrameContent() != null){
                        setTrackData(frame);
                    }
                }
            }
            bis.close();
            track.calculateCombinedSize();
            id3EditorInterface.onDataLoadedListener(track);
        } catch (IOException e){
            System.out.println(e);
        }
    }

    private void setTrackData(ID3V4Frame frame){
        switch (frame.getFrameId()){
            case ID3V2FrameIDs.TPE2:{
                track.setFrame(TagResolver.FRAME_ARTIST,frame);
                break;
            }
            case ID3V2FrameIDs.TDRC:{
                track.setFrame(TagResolver.FRAME_YEAR,frame);
                break;
            }
            case ID3V2FrameIDs.TRCK:{
                track.setFrame(TagResolver.FRAME_TRACKID,frame);
                break;
            }
            case ID3V2FrameIDs.TCON:{
                track.setFrame(TagResolver.FRAME_GENRE,frame);
                break;
            }
            case ID3V2FrameIDs.TCOM:{
                track.setFrame(TagResolver.FRAME_COMPOSER,frame);
                break;
            }
            case ID3V2FrameIDs.TIT2:{
                track.setFrame(TagResolver.FRAME_TITLE,frame);
                break;
            }
            case ID3V2FrameIDs.TALB: {
                track.setFrame(TagResolver.FRAME_ALBUM,frame);
                break;
            }
            default:{
                break;
            }
        }
    }

    private void checkFooter(InputStream is){

    }

    private boolean checkIfV2TagIsPresent(byte[] tagIdent){
        if (tagIdent[0] == 0x49 && tagIdent[1] == 0x44 && tagIdent[2] == 0x33)return true;
        else return false;
    }

    private void getV4Frames(byte[] data){
            processFrames(data);
    }

    public TagResolver getTrackData(){
        return track;
    }

    public ID3V2TagHeader getTagHeader(){return tagHeader;}

}
