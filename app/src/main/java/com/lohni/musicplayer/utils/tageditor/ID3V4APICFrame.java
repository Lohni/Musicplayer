package com.lohni.musicplayer.utils.tageditor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ID3V4APICFrame extends ID3V4Frame<byte[]> {
    private int picture_type;
    private String mimeType = "", description = "";
    private byte[] FRAME_DATA;

    public ID3V4APICFrame(byte[] rawPictureData, ID3V4FrameHeader frameHeader){
        super(rawPictureData, frameHeader);
    }

    @Override
    public void decodeFrame(byte[] data){
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
        mimeType = getText(mimeTypeBytes, ID3Dictionary.getEncodingCharset(encoding));
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
            description = getText(descriptionBytes, ID3Dictionary.getEncodingCharset(encoding));
            offset += descriptionLength+1;
        } else {
            description = "";
            offset++;
        }
        FRAME_DATA = new byte[data.length-offset];
        for (int i = 0; i < FRAME_DATA.length; i++){
            FRAME_DATA[i] = data[offset+i];
        }
        //picture = BitmapFactory.decodeByteArray(data, offset, data.length-offset);

    }

    private byte[] getTextAsBytes(String text){
       return text.getBytes(ID3Dictionary.getEncodingCharset(encoding));
    }

    @Override
    public byte[] getFrameAsBytes(){
        byte[] header = frameHeader.toBytes();
        byte encodingByte = (byte) encoding;
        byte[] mimeTypeBytes = getTextAsBytes(mimeType);
        byte nullByte = 0x00;
        byte pictureTypeByte = (byte) picture_type;
        byte[] descriptionBytes = getTextAsBytes(description);

        byte[] frame = new byte[getFrameSize()];

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
        System.arraycopy(FRAME_DATA, 0, frame, offset, FRAME_DATA.length);
        return frame;
    }

    @Override
    public byte[] getFrameContentAsBytes(){
        int offset = 0;
        byte[] rawFrameContent = new byte[getFrameContentSize()];
        rawFrameContent[offset] = (byte) encoding;
        offset++;
        byte[] mimeTypeBytes = getTextAsBytes(mimeType);
        System.arraycopy(mimeTypeBytes, 0, rawFrameContent, offset, mimeTypeBytes.length);
        offset+=mimeTypeBytes.length;
        rawFrameContent[offset] = 0x00;
        offset++;
        rawFrameContent[offset] = (byte) picture_type;
        offset++;
        byte[] descriptionBytes = getTextAsBytes(description);
        System.arraycopy(descriptionBytes, 0, rawFrameContent, offset, descriptionBytes.length);
        offset+=descriptionBytes.length;
        rawFrameContent[offset] = 0x00;
        offset++;
        System.arraycopy(FRAME_DATA, 0, rawFrameContent, offset, FRAME_DATA.length);
        return rawFrameContent;
    }

    @Override
    public int getFrameContentSize(){
        int preDescriptionPadding = 2;
        int finalPadding = 1;
        return FRAME_DATA.length + FRAME_ENCODING_LENGHT + mimeType.length()
                + preDescriptionPadding + description.length() + finalPadding;
    }

    @Override
    public void setFrameData(byte[] rawPictureData){
        this.FRAME_DATA = rawPictureData;
        frameHeader.setNewFrameSize(getFrameContentSize());
    }

    @Override
    public byte[] getFrameData(){return FRAME_DATA;}

    public void setMimeType(String mimeType){
        this.mimeType = mimeType;
        frameHeader.setNewFrameSize(getFrameContentSize());
    }

    public void setDescription(String description){
        this.description = description;
        frameHeader.setNewFrameSize(getFrameContentSize());
    }

    public Bitmap getPictureAsBitmap(){return BitmapFactory.decodeByteArray(FRAME_DATA, 0, FRAME_DATA.length);}

}
