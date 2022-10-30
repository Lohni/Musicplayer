package com.lohni.musicplayer.utils.tageditor;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class ID3Editor {
    private static final int HEADER_SIZE = 10;
    private final byte[] id3Identifier = {0x49, 0x44, 0x33};

    private ID3V4Track track;

    private final long trackId;
    private final Context context;
    private final ID3EditorInterface id3EditorInterface;

    public ID3Editor(Uri uri, Context context, long trackId, ID3EditorInterface id3EditorInterface) {
        this.context = context;
        this.trackId = trackId;
        this.id3EditorInterface = id3EditorInterface;
        decode(uri);
    }

    private void decode(Uri uri) {
        try (InputStream is = context.getContentResolver().openInputStream(uri)) {
            if (is != null && is.available() > 0) {
                byte[] header = new byte[HEADER_SIZE];
                if (isID3V2HeaderPresent(is, header)) {
                    ID3V2TagHeader tagHeader = new ID3V2TagHeader(header);
                    track = new ID3V4Track(tagHeader);
                    if (tagHeader.getTAG_VERSION_MAJOR() == 4 || tagHeader.getTAG_VERSION_MAJOR() == 3) {
                        processFrames(is);
                    }
                } else {
                    ID3V2TagHeader tagHeader = new ID3V2TagHeader();
                    track = new ID3V4Track(tagHeader);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isID3V2HeaderPresent(InputStream is, byte[] headerData) throws IOException {
        return headerData.length == is.read(headerData, 0, headerData.length)
                && checkIfV2TagIsPresent(Arrays.copyOfRange(headerData, 0, 3));
    }

    private boolean checkIfV2TagIsPresent(byte[] tagIdent) {
        return Arrays.equals(tagIdent, id3Identifier);
    }

    private void processFrames(InputStream is) throws IOException {
        byte[] frameHeaderBytes = new byte[10];
        byte[] frameData;
        int readBytes = 0;
        try {
            int tagSize = track.getTagHeader().getTAG_SIZE();
            while (readBytes < tagSize) {
                int read = is.read(frameHeaderBytes, 0, frameHeaderBytes.length);
                if (read == frameHeaderBytes.length) {
                    ID3V4FrameHeader frameHeader = new ID3V4FrameHeader(frameHeaderBytes, track.getTagHeader().getTAG_VERSION_MAJOR());
                    //Necessary?
                    if (frameHeader.FRAME_ID.equals(String.copyValueOf(new char[]{0x00, 0x00, 0x00, 0x00}))) {
                        track.setPaddingBytes(tagSize - readBytes);
                        readBytes += tagSize - readBytes;
                    } else {
                        frameData = new byte[frameHeader.FRAME_SIZE];
                        read = is.read(frameData, 0, frameData.length);
                        if (read == frameData.length) {
                            ID3V4Frame newFrame = ID3V4Frame.newInstance(frameHeader, frameData);
                            track.setFrame(newFrame);
                        } else {
                            throw new Exception("Bytes read does not match Framesize");
                        }
                        readBytes += frameHeaderBytes.length + frameData.length;
                    }
                }
            }
            id3EditorInterface.onDataLoadedListener(track);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            is.close();
        }
    }

    public ID3V4Track getTrackData() {
        return track;
    }

    public long getTrackId() {
        return trackId;
    }

    public void saveTrack() throws IOException {
        ID3V2TagHeader tagHeader = track.getTagHeader();
        int oldTagSize = tagHeader.getTAG_SIZE();
        tagHeader.setNewTagSize(getNewTagSize());
        byte[] newTag = new byte[tagHeader.getTAG_SIZE() + tagHeader.getTagHeaderLength()];

        byte[] tagHeaderBytes = tagHeader.toBytes();
        System.arraycopy(tagHeaderBytes, 0, newTag, 0, tagHeaderBytes.length);

        int offset = tagHeader.getTagHeaderLength();

        List<ID3V4Frame> frameList = track.getAllFrames();
        for (ID3V4Frame frame : frameList) {
            byte[] frameBytes = frame.getFrameAsBytes();
            System.arraycopy(frameBytes, 0, newTag, offset, frameBytes.length);
            offset += frameBytes.length;
        }

        Arrays.fill(newTag, offset, newTag.length, (byte) 0x00);

        ParcelFileDescriptor parcelFileDescriptor = context
                .getContentResolver()
                .openFileDescriptor(ContentUris
                                .withAppendedId(
                                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, trackId),
                        "rw");

        File tempAudioData = File.createTempFile("tempAudioData", null, context.getCacheDir());
        FileOutputStream tmpOutputStream = new FileOutputStream(tempAudioData);

        int chunkSize = 8192;
        byte[] chunk = new byte[chunkSize];
        InputStream is = new FileInputStream(parcelFileDescriptor.getFileDescriptor());
        is.skip(oldTagSize);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(is, chunkSize);

        //Read Chunkwise
        while (bufferedInputStream.available() > 0) {
            bufferedInputStream.read(chunk, 0, chunkSize);
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
        bufferedInputStream = new BufferedInputStream(is, chunkSize);

        while (bufferedInputStream.available() > 0) {
            bufferedInputStream.read(chunk, 0, chunkSize);
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
    }

    private int getNewTagSize() {
        List<ID3V4Frame> frameList = track.getAllFrames();

        int tagSize = 0;
        for (ID3V4Frame frame : frameList) {
            tagSize += frame.getFrameSize();
        }
        return tagSize + track.getPaddingBytes();
    }

}
