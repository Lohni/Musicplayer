package com.example.musicplayer.utils.tageditor;

import com.example.musicplayer.utils.dictionary.ID3Dictionary;
import com.example.musicplayer.utils.enums.ID3FrameId;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;

public class ID3V4Track {
    private ID3V2TagHeader tagHeader;

    private List<ID3V4Frame> id3FrameList = new ArrayList<>();
    private HashMap<ID3FrameId, Integer> relevantFramePositions = new HashMap<>();

    int paddingBytes = 0;

    public ID3V4Track(ID3V2TagHeader id3V2TagHeader){this.tagHeader = id3V2TagHeader;}

    public void setFrame(ID3V4Frame frame){
        Integer framePosition = id3FrameList.size();
        String frameId = frame.getFrameHeader().FRAME_ID;
        id3FrameList.add(frame);
        if (ID3Dictionary.FRAME_ID.containsKey(frameId)
                && !relevantFramePositions.containsKey(frameId)){
            relevantFramePositions.put(ID3Dictionary.FRAME_ID.get(frameId), framePosition);
        }
    }

    public List<ID3V4Frame> getAllFrames(){return id3FrameList;}

    public ID3V2TagHeader getTagHeader(){return tagHeader;}

    public ID3V4Frame getRelevantFrame(ID3FrameId frameId){
        if (relevantFramePositions.containsKey(frameId)){
            return id3FrameList.get(relevantFramePositions.get(frameId));
        }
        return null;
    }

    //Todo: evtl rename
    public <E> void setFrameContent(ID3FrameId frameId, E frameData){
        ID3V4Frame frame = getRelevantFrame(frameId);
        if (frame != null){
            frame.setFrameData(frameData);
            id3FrameList.set(relevantFramePositions.get(frameId), frame);
        } else {
            ID3V4Frame newFrame = createNewFrame(frameId, frameData);
            Integer newFramePosition = id3FrameList.size();
            id3FrameList.add(newFrame);
            relevantFramePositions.put(frameId, newFramePosition);
        }
    }

    public <E> ID3V4Frame createNewFrame(ID3FrameId frameId, E frameData){
        ID3V4FrameHeader frameHeader = new ID3V4FrameHeader();
        frameHeader.setFrameID(frameId.getFrameId());
        ID3V4Frame frame = ID3V4Frame.newInstance(frameHeader, frameData);
        return frame;
    }

    public ID3V4Frame createNewTextFrame(ID3FrameId frameId, String frameData) {
        ID3V4FrameHeader frameHeader = new ID3V4FrameHeader();
        frameHeader.setFrameID(frameId.getFrameId());
        ID3V4Frame frame = ID3V4Frame.newInstance(frameHeader, null);
        frame.setFrameData(frameData);
        return frame;
    }

    public ID3V4APICFrame createNewApicframe(ID3FrameId frameId, byte[] frameData) {
        ID3V4FrameHeader frameHeader = new ID3V4FrameHeader();
        frameHeader.setFrameID(frameId.getFrameId());
        ID3V4APICFrame frame = (ID3V4APICFrame) ID3V4Frame.newInstance(frameHeader, null);
        frame.setFrameData(frameData);
        return frame;
    }

    public int getPaddingBytes() {
        return paddingBytes;
    }

    public void setPaddingBytes(int paddingBytes) {
        this.paddingBytes = paddingBytes;
    }
}
