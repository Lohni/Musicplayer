package com.example.musicplayer.utils.tageditor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.nio.charset.StandardCharsets;

public class ID3V4APICFrame {
    private ID3V4FrameHeader frameHeader;
    private final int ISO88591 = 0, UTF16 = 1, UTF16BE = 2, UTF8=3;
    private boolean isRead = false;

    private int encoding, picture_type;
    private String mimeType = "", description = "";
    //private Bitmap picture;
    private byte[] rawPictureData;

    public ID3V4APICFrame(byte[] data, ID3V4FrameHeader frameHeader){
        this.frameHeader = frameHeader;
        if (data != null)decodeFrame(data);
    }

    public ID3V4APICFrame(){
        //Initialise Standard values
        encoding = ISO88591;
        picture_type = 0x03; //Front-Cover
        frameHeader = new ID3V4FrameHeader();
        frameHeader.FRAME_ID = "APIC";
    }

    /*
    Init-Methods
     */

    private void decodeFrame(byte[] data){
        encoding = data[0];
        int offset = 1;

        //Get MimeType
        int mimeTypeLength = 0;
        while (data[mimeTypeLength+offset] != 0){
            mimeTypeLength++;
        }
        byte[] mimeTypeBytes = new byte[mimeTypeLength];
        for (int i = 0; i<mimeTypeBytes.length;i++){
            mimeTypeBytes[i] = data[offset+i];
        }
        mimeType = getText(mimeTypeBytes, encoding);
        //+1 to skip NULL-Byte after MimeType-String
        offset+=mimeTypeLength + 1;
        picture_type = data[offset];
        offset++;

        int descriptionLength = 0;
        while (data[descriptionLength+offset] != 0){
            descriptionLength++;
        }

        if (descriptionLength > 0){
            byte[] descriptionBytes = new byte[descriptionLength];
            for (int i = 0; i<descriptionBytes.length;i++){
                descriptionBytes[i] = data[offset+i];
            }
            description = getText(descriptionBytes, encoding);
            offset += descriptionLength+1;
        } else {
            description = "";
            offset++;
        }
        rawPictureData = new byte[data.length-offset];
        for (int i = 0; i < rawPictureData.length; i++){
            rawPictureData[i] = data[offset+i];
        }
        //picture = BitmapFactory.decodeByteArray(data, offset, data.length-offset);

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

    /*
    Util-Methods
     */

    private byte[] getTextAsBytes(int encoding, String text){
        switch (encoding) {
            case ISO88591: {
                return text.getBytes(StandardCharsets.ISO_8859_1);
            }
            case UTF16: {
                return text.getBytes(StandardCharsets.UTF_16);
            }
            case UTF16BE: {
                return text.getBytes(StandardCharsets.UTF_16BE);
            }
            case UTF8: {
                return text.getBytes(StandardCharsets.UTF_8);
            }
            default: {
                return null;
            }
        }
    }

    public byte[] getFrameAsBytes(){
        byte[] header = frameHeader.toBytes();
        byte encodingByte = (byte) encoding;
        byte[] mimeTypeBytes = getTextAsBytes(encoding, mimeType);
        byte nullByte = 0x00;
        byte pictureTypeByte = (byte) picture_type;
        byte[] descriptionBytes = getTextAsBytes(encoding, description);

        byte[] rawPictureBytes = getPictureAsBytes();

        int totalFrameLength = header.length + 1 + mimeTypeBytes.length + 1 + 1 + descriptionBytes.length + 1 + rawPictureBytes.length;

        byte[] frame = new byte[totalFrameLength];

        int offset = 0;
        System.arraycopy(header, 0, frame, 0, header.length);
        offset+= header.length;
        frame[offset] = encodingByte;
        offset++;
        System.arraycopy(mimeTypeBytes, 0, frame, offset, mimeTypeBytes.length);
        offset+=mimeTypeBytes.length;
        frame[offset] = nullByte;
        frame[offset+1] = pictureTypeByte;
        offset+=2;
        System.arraycopy(descriptionBytes, 0, frame, offset, descriptionBytes.length);
        offset+=descriptionBytes.length;
        frame[offset] = nullByte;
        offset++;
        System.arraycopy(rawPictureBytes, 0, frame, offset, rawPictureBytes.length);
        return frame;
    }


    private byte[] getPictureAsBytes(){
        if (rawPictureData == null){
            return new byte[0];
        }
        return rawPictureData;
    }

    public int getFrameContentSize(){
        return rawPictureData.length + 1 + mimeType.length() + 1 + 1 + description.length() + 1;
    }

    public int getFrameSize(){
        return getFrameContentSize()+10;
    }

    private void setNewFrameSize(){
        frameHeader.FRAME_SIZE = getFrameContentSize();
    }

    /*
    Getter/Setter of global fields
     */

    public void setPicture(byte[] Picture, String mimeType){
        this.rawPictureData = Picture;
        this.mimeType = mimeType;
        setNewFrameSize();
    }

    public void setMimeType(String mimeType){
        this.mimeType = mimeType;
        setNewFrameSize();
    }

    public void setDescription(String description){
        this.description = description;
        setNewFrameSize();
    }

    public byte[] getPicture(){return rawPictureData;}

    public Bitmap getPictureAsBitmap(){return BitmapFactory.decodeByteArray(rawPictureData, 0, rawPictureData.length);}

    public void setRead(boolean isRead){this.isRead = isRead;}

    public boolean getRead(){return isRead;}

}
