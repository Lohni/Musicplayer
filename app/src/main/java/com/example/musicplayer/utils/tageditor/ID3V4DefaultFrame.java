package com.example.musicplayer.utils.tageditor;

public class ID3V4DefaultFrame extends ID3V4Frame<byte[]>{
    private byte[] FRAME_DATA;

    public ID3V4DefaultFrame(byte[] rawFrameData, ID3V4FrameHeader frameHeader){
        super(rawFrameData, frameHeader);
    }

    @Override
    public void setFrameData(byte[] frameData) {
        this.FRAME_DATA = frameData;
    }

    @Override
    public byte[] getFrameData() {
        return FRAME_DATA;
    }

    @Override
    public byte[] getFrameContentAsBytes() {
        return FRAME_DATA;
    }

    @Override
    public byte[] getFrameAsBytes() {
        byte[] frameBytes = new byte[10 + FRAME_DATA.length];

        System.arraycopy(frameHeader.toBytes(), 0, frameBytes, 0, 10);
        int offset = 10;
        System.arraycopy(FRAME_DATA, 0, frameBytes, offset, FRAME_DATA.length);

        return frameBytes;
    }

    @Override
    public void decodeFrame(byte[] rawFramedata) {
        FRAME_DATA = rawFramedata;
    }

    @Override
    public int getFrameContentSize() {
        return FRAME_DATA.length;
    }
}
