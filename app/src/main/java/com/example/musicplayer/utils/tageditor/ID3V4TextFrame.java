package com.example.musicplayer.utils.tageditor;

import com.example.musicplayer.utils.dictionary.ID3Dictionary;

import java.nio.charset.Charset;
import java.util.Arrays;

public class ID3V4TextFrame extends ID3V4Frame<String> {
    private String FRAME_DATA;

    public ID3V4TextFrame(byte[] rawFrameData, ID3V4FrameHeader frameHeader) {
        super(rawFrameData, frameHeader);
    }

    @Override
    public void setFrameData(String frameData) {
        FRAME_DATA = frameData;
        //+1 for encoding byte
        frameHeader.setNewFrameSize(getFrameContentAsBytes().length + 1);
    }

    @Override
    public String getFrameData() {
        return FRAME_DATA;
    }

    @Override
    public byte[] getFrameContentAsBytes() {
        return FRAME_DATA.getBytes(ID3Dictionary.getEncodingCharset(encoding));
    }

    @Override
    public byte[] getFrameAsBytes() {
        byte[] header = frameHeader.toBytes();
        byte encodingByte = (byte) encoding;
        byte[] content = getFrameContentAsBytes();
        byte[] frame = new byte[header.length + FRAME_ENCODING_LENGHT + content.length];

        System.arraycopy(header, 0, frame, 0, header.length);
        frame[10] = encodingByte;
        System.arraycopy(content, 0, frame, header.length + FRAME_ENCODING_LENGHT, content.length);
        return frame;
    }

    @Override
    public void decodeFrame(byte[] data) {
        if (frameHeader.FRAME_ID != null && ID3Dictionary.ENCODING_CHARSET.containsKey((int) data[0])) {
            encoding = data[0];
            FRAME_DATA = getText(Arrays.copyOfRange(data, 1, frameHeader.FRAME_SIZE), ID3Dictionary.ENCODING_CHARSET.get(encoding));
        } else {
            FRAME_DATA = "";
        }
    }

    @Override
    public int getFrameContentSize() {
        return getFrameContentAsBytes().length + 1;
    }

    public int getFrameSize() {
        return ID3V4FrameHeader.FRAME_HEADER_LENGTH + FRAME_ENCODING_LENGHT + getFrameContentAsBytes().length;
    }

    public String getFrameId() {
        return frameHeader.FRAME_ID;
    }
}
