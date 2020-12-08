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

    private int TAG_SIZE;
    private int TAG_VERSION_MAJOR;
    private int TAG_VERSION_REVISION;
    private int UNSYNCHRONISATION, EXTENDED_HEADER, EXPERIMENTAL_INDICATOR, FOOTER;

    private Context context;

    private TagResolver track;

    private ID3EditorInterface id3EditorInterface;

    public ID3Editor(Uri uri, Context context, ID3EditorInterface id3EditorInterface){
        this.context = context;
        this.id3EditorInterface = id3EditorInterface;
        decode(uri);
    }

    private boolean decode(Uri uri){
        InputStream is;
        try {
            is = context.getContentResolver().openInputStream(uri);

            byte[] header = new byte[HEADER_SIZE];
            is.read(header, 0, header.length);

            if (decodeHeader(header)){

                byte[] data = new byte[TAG_SIZE];
                is.read(data, 0, data.length);

                is.close();
                switch (TAG_VERSION_MAJOR){
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
                is.close();
            }

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
            track = new TagResolver();
            byte[] header = new byte[10];
            while (bis.available() > 0){

                int bytesRead = bis.read(header,0,HEADER_SIZE);
                if (bytesRead == HEADER_SIZE){
                    ID3V4FrameHeader frameHeader = new ID3V4FrameHeader(header);
                    int frameSize = frameHeader.FRAME_SIZE;
                    byte[] frameData = new byte[frameSize];

                    bis.read(frameData,0,frameSize);
                    ID3V4Frame frame = new ID3V4Frame(frameData, frameHeader);
                    if (frame.getFrameContent() != null){
                        setTrackData(frame.getFrameContent(), frameHeader.FRAME_ID);
                    }
                }
            }
            bis.close();
            if (id3EditorInterface != null){
                id3EditorInterface.onDataLoadedListener(track);
            }
        } catch (IOException e){
            System.out.println(e);
        }
    }

    private void setTrackData(String trackData, String frameID){
        switch (frameID){
            case ID3V2FrameIDs.TPE1:{
                track.setArtist(trackData);
                break;
            }
            case ID3V2FrameIDs.TDRC:{
                track.setYear(trackData);
                break;
            }
            case ID3V2FrameIDs.TRCK:{
                track.setTrackid(trackData);
                break;
            }
            case ID3V2FrameIDs.TCON:{
                track.setGenre(trackData);
                break;
            }
            case ID3V2FrameIDs.TCOM:{
                track.setComposer(trackData);
            }
            case ID3V2FrameIDs.TIT2:{
                track.setTitle(trackData);
            }
            case ID3V2FrameIDs.TALB: {
                track.setAlbum(trackData);
                break;
            }
            default:{
                break;
            }
        }
    }

    private void checkFooter(InputStream is){

    }

    private void getFlags(byte flag){
        UNSYNCHRONISATION = flag >> 7;
        EXTENDED_HEADER = flag >> 6;
        EXPERIMENTAL_INDICATOR = flag >> 5;
        FOOTER = flag >> 4;
    }

    private void getSize(byte size4, byte size3, byte size2, byte size1){
        size4 = (byte) (size4 << 1);
        size3 = (byte) (size3 << 1);
        size2 = (byte) (size2 << 1);
        size1 = (byte) (size1 << 1);

        TAG_SIZE = (int) (((size4) << 24) + ((size3) << 17) + ((size2) << 10) + ((size1) << 3) >> 4);
    }

    private boolean decodeHeader(byte[] header){
        if (header[0] == 0x49 && header[1] == 0x44 && header[2] == 0x33){
            TAG_VERSION_MAJOR = (int) header[3];
            TAG_VERSION_REVISION = (int) header[4];
            getFlags(header[5]);
            getSize(header[6], header[7], header[8], header[9]);
            return true;
        } else {
            return false;
        }
    }

    private void getV4Frames(byte[] data){
        if (EXTENDED_HEADER == 0){
            processFrames(data);
        }
    }

    public TagResolver getTrackData(){
        return track;
    }
}
