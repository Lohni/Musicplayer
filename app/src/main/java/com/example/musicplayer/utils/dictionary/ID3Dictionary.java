package com.example.musicplayer.utils.dictionary;

import com.example.musicplayer.utils.enums.ID3FrameId;
import com.example.musicplayer.utils.tageditor.ID3V4FrameHeader;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class ID3Dictionary {
    public static final HashMap<String, ID3FrameId> FRAME_ID;
    public static final HashMap<Integer, Charset> ENCODING_CHARSET;

    public static ID3FrameId getFrameIdFromHeader(ID3V4FrameHeader header) {
        if (FRAME_ID.containsKey(header.FRAME_ID)) {
            return FRAME_ID.get(header.FRAME_ID);
        }
        return ID3FrameId.DEFAULT;
    }

    public static Charset getEncodingCharset(int encoding) {
        return ENCODING_CHARSET.containsKey(encoding) ?
                ENCODING_CHARSET.get(encoding) :
                ENCODING_CHARSET.get(0);
    }

    static {
        FRAME_ID = new HashMap<>();
        FRAME_ID.put("APIC", ID3FrameId.APIC);
        FRAME_ID.put("TPE1", ID3FrameId.TPE2);
        FRAME_ID.put("TIT2", ID3FrameId.TIT2);
        FRAME_ID.put("TALB", ID3FrameId.TALB);
        FRAME_ID.put("TCOM", ID3FrameId.TCOM);
        FRAME_ID.put("TCON", ID3FrameId.TCON);
        FRAME_ID.put("TRCK", ID3FrameId.TRCK);
        FRAME_ID.put("TDRC", ID3FrameId.TDRC);
        FRAME_ID.put("POPM", ID3FrameId.POPM);

        ENCODING_CHARSET = new HashMap<>();
        ENCODING_CHARSET.put(0, StandardCharsets.ISO_8859_1);
        ENCODING_CHARSET.put(1, StandardCharsets.UTF_16);
        ENCODING_CHARSET.put(2, StandardCharsets.UTF_16BE);
        ENCODING_CHARSET.put(3, StandardCharsets.UTF_8);
    }

}
