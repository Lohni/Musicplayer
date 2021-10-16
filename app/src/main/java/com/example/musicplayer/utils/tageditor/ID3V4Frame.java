package com.example.musicplayer.utils.tageditor;

import com.example.musicplayer.utils.dictionary.ID3Dictionary;
import com.example.musicplayer.utils.enums.ID3FrameId;

import java.nio.charset.Charset;

public abstract class ID3V4Frame<E> {
    public final ID3V4FrameHeader frameHeader;
    public final int FRAME_ENCODING_LENGHT = 1;
    public int encoding = 0;

    public ID3V4Frame(byte[] rawFrameData, ID3V4FrameHeader frameHeader) {
        this.frameHeader = frameHeader;
        if (rawFrameData != null) decodeFrame(rawFrameData);
    }

    public abstract void setFrameData(E frameData);

    public abstract E getFrameData();

    public abstract byte[] getFrameContentAsBytes();

    public abstract byte[] getFrameAsBytes();

    public ID3V4FrameHeader getFrameHeader() {
        return frameHeader;
    }

    public abstract void decodeFrame(byte[] rawFramedata);

    public abstract int getFrameContentSize();

    //Todo: handle encoding

    public String getText(byte[] text, Charset encoding) {
        if (encoding != null) {
            return new String(text, encoding);
        }
        return null;
    }

    public int getFrameSize() {
        return 10 + frameHeader.FRAME_SIZE;
    }

    public static <E> ID3V4Frame newInstance(ID3V4FrameHeader frameHeader, E frameData) {
        ID3FrameId frameId = ID3FrameId.DEFAULT;
        if (ID3Dictionary.FRAME_ID.containsKey(frameHeader.FRAME_ID)) {
            frameId = ID3Dictionary.FRAME_ID.get(frameHeader.FRAME_ID);
        }
        switch (frameId) {
            case TPE2:
            case TDRC:
            case TRCK:
            case TCON:
            case TCOM:
            case TIT2:
            case TALB: {
                return new ID3V4TextFrame((byte[]) frameData, frameHeader);
            }
            case APIC: {
                return new ID3V4APICFrame((byte[]) frameData, frameHeader);
            }
            default: {
                return new ID3V4DefaultFrame((byte[]) frameData, frameHeader);
            }
        }
    }
}
