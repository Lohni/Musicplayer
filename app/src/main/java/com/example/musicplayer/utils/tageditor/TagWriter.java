package com.example.musicplayer.utils.tageditor;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

    public void writeToFile() throws IOException {
        Uri trackUri =  ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, track.getTrackId());
        InputStream is = context.getContentResolver().openInputStream(trackUri);

        int oldTagSize = tagHeader.getTAG_SIZE();
        tagHeader.setNewTagSize(getNewTagSize());
        byte[] newTag = new byte[tagHeader.getTAG_SIZE() + tagHeader.getTagHeaderLength()];
        byte[] paddingBytes = null;

        int offset = tagHeader.getTagHeaderLength();
        if (oldTagSize > 0){
            //Skip Header from File
            byte[] tagHeaderBytes = tagHeader.toBytes();
            for (int i = 0; i<tagHeaderBytes.length;i++){
                newTag[i] = tagHeaderBytes[i];
            }

            byte[] data = new byte[oldTagSize];
            is.read(data, 0, data.length);
            is.close();

            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            bis.read(new byte[10], 0, tagHeader.getTagHeaderLength());

            while (bis.available() > 0){
                byte[] rawFrameHeader = new byte[10];
                bis.read(rawFrameHeader, 0, rawFrameHeader.length);
                if (rawFrameHeader[0] == 0x00){
                    //All Frames are read, only padding is left
                    paddingBytes = new byte[bis.available() + rawFrameHeader.length];
                    bis.read(paddingBytes, 0, paddingBytes.length);
                } else {
                    ID3V4FrameHeader frameHeader = new ID3V4FrameHeader(rawFrameHeader);
                    int frameSize = frameHeader.FRAME_SIZE;
                    byte[] frameData = new byte[frameSize];
                    bis.read(frameData,0,frameSize);
                    if (!isFrameRelevant(frameHeader.FRAME_ID)){
                        for (int i = 0; i<rawFrameHeader.length;i++){
                            newTag[offset] = rawFrameHeader[i];
                            offset++;
                        }

                        for (int i = 0; i<frameData.length;i++){
                            newTag[offset] = frameData[i];
                            offset++;
                        }
                    } else {
                        byte[] frame = getFrameData(frameHeader.FRAME_ID);
                        System.out.println(frameHeader.FRAME_ID);
                        for (int i = 0; i<frame.length;i++){
                            newTag[offset] = frame[i];
                            offset++;
                        }
                    }
                }
            }
            bis.close();
        }

        byte[] unusedFrames = getUnusedFrames();

        //Todo: Combine to 1 IF
        if (unusedFrames != null){
            if ((unusedFrames.length + offset) <= newTag.length){
                for (int i = 0; i<unusedFrames.length; i++){
                    newTag[offset + i] = unusedFrames[i];
                }
            }
            offset+=unusedFrames.length;
        }
        if (paddingBytes != null){
            for (int i = 0; i<paddingBytes.length; i++){
                newTag[offset+i] = paddingBytes[i];
            }
        }

        ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, track.getTrackId()), "rw");

        //Write AudioData to TempFile
        File tempAudioData = File.createTempFile("tempAudioData", null, context.getCacheDir());
        FileOutputStream tmpOutputStream = new FileOutputStream(tempAudioData);

        int chunkSize = 8192;
        byte[] chunk = new byte[chunkSize];
        is = new FileInputStream(parcelFileDescriptor.getFileDescriptor());
        is.skip(oldTagSize);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(is, chunkSize);

        //Read Chunkwise
        while (bufferedInputStream.available() > 0){
            bufferedInputStream.read(chunk,0,chunkSize);
            tmpOutputStream.write(chunk);
        }
        //Read last chunk < chunkSize
        byte[] lastChunk = new byte[bufferedInputStream.available()];
        bufferedInputStream.read(lastChunk);
        tmpOutputStream.write(lastChunk);

        bufferedInputStream.close();
        tmpOutputStream.close();
        is.close();

        //Write Tag
        FileOutputStream fileOutputStream = new FileOutputStream(parcelFileDescriptor.getFileDescriptor());
        fileOutputStream.getChannel().truncate(0);
        fileOutputStream.write(newTag);

        //Write AudioData from tmpFile to AudioFile, reuse InputStream; BufferedInputStream
        is = new FileInputStream(tempAudioData);
        bufferedInputStream = new BufferedInputStream(is,chunkSize);

        while (bufferedInputStream.available() > 0){
            bufferedInputStream.read(chunk,0,chunkSize);
            fileOutputStream.write(chunk);
        }
        lastChunk = new byte[bufferedInputStream.available()];
        bufferedInputStream.read(lastChunk);
        fileOutputStream.write(lastChunk);

        is.close();
        bufferedInputStream.close();
        fileOutputStream.close();
        tempAudioData.delete();
        parcelFileDescriptor.close();

        //Get real path for rescanning media
        String yourRealPath = "";
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(trackUri, filePathColumn, null, null, null);
        if(cursor.moveToFirst()){
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            yourRealPath = cursor.getString(columnIndex);
        } else {
            //boooo, cursor doesn't have rows ...
        }
        cursor.close();

        //Rescan media
        String[] scanned = {yourRealPath};
        MediaScannerConnection.scanFile(context, scanned, null, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String s, Uri uri) {
                Log.i("FileScanner","Scanned " + s + " : " + uri);
            }
        });

    }

    private boolean isFrameRelevant(String frameID){
        switch (frameID){
            case ID3V2FrameIDs.TPE2:
            case ID3V2FrameIDs.TDRC:
            case ID3V2FrameIDs.TRCK:
            case ID3V2FrameIDs.TCON:
            case ID3V2FrameIDs.TCOM:
            case ID3V2FrameIDs.TIT2:
            case ID3V2FrameIDs.TALB:
            case ID3V2FrameIDs.APIC: {
                return true;
            }
            default:{
                return false;
            }
        }
    }

    private byte[] getFrameData(String frameID){
        switch (frameID){
            case ID3V2FrameIDs.TPE2:{
                return track.getFrameAsBytes(TagResolver.FRAME_ARTIST);
            }
            case ID3V2FrameIDs.TDRC:{
                return track.getFrameAsBytes(TagResolver.FRAME_YEAR);
            }
            case ID3V2FrameIDs.TRCK:{
                return track.getFrameAsBytes(TagResolver.FRAME_TRACKID);
            }
            case ID3V2FrameIDs.TCON:{
                return track.getFrameAsBytes(TagResolver.FRAME_GENRE);
            }
            case ID3V2FrameIDs.TCOM:{
                return track.getFrameAsBytes(TagResolver.FRAME_COMPOSER);
            }
            case ID3V2FrameIDs.TIT2:{
                return track.getFrameAsBytes(TagResolver.FRAME_TITLE);
            }
            case ID3V2FrameIDs.TALB: {
                return track.getFrameAsBytes(TagResolver.FRAME_ALBUM);
            }
            case ID3V2FrameIDs.APIC: {
                return track.getFrameAsBytes(TagResolver.FRAME_APIC);
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

    private byte[] getUnusedFrames(){
        if (track.hasUnusedFrames()){
            return track.getUnusedFramesAsBytes();
        }
        return null;
    }

}
