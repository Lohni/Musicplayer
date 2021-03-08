package com.example.musicplayer.utils;

public class TagResolver {
    public static final int FRAME_TITLE = 0, FRAME_ARTIST = 1, FRAME_ALBUM = 2, FRAME_GENRE = 3, FRAME_YEAR = 4, FRAME_COMPOSER = 5, FRAME_TRACKID = 6, FRAME_APIC = 7;
    private ID3V4APICFrame apicFrame = null;
    private ID3V4Frame[] frameList = {null, null, null, null, null, null, null};

    private int oldCombinedSize=0;
    private long trackid;
    public TagResolver(long trackid){
        this.trackid = trackid;
    }

    public void setFrame(int frameID, ID3V4Frame frameData){
        frameList[frameID] = frameData;
    }

    public void setFrame(ID3V4APICFrame apicFrame){
        this.apicFrame = apicFrame;
    }

    public ID3V4Frame getFrame(int frameID){
        if (frameID > frameList.length || frameID < 0)return null;
        return frameList[frameID];
    }

    public ID3V4APICFrame getFrame(){return apicFrame;}

    public byte[] getFrameAsBytes(int frameID){
        if (frameID == FRAME_APIC){
            apicFrame.setRead(true);
            return apicFrame.getFrameAsBytes();
        } else if (frameID < frameList.length && frameID >= 0){
            frameList[frameID].setRead(true);
            return frameList[frameID].getFrameAsBytes();
        }
        return null;
    }

    //Has to be called after Audiofile first read
    public void calculateCombinedSize(){
        for (int i = 0; i < frameList.length; i++){
            if (frameList[i] != null) oldCombinedSize+=frameList[i].getFrameSize();
        }
        if (apicFrame != null) oldCombinedSize+=apicFrame.getFrameSize();
        initFrames();
    }

    public int getChangedContentSize(){
        int newCombinedSize = 0;
        for (int i = 0; i < frameList.length; i++){
            if (frameList[i] != null) newCombinedSize+=frameList[i].getFrameSize();
        }
        if (apicFrame!=null)newCombinedSize+=apicFrame.getFrameSize();
        return newCombinedSize - oldCombinedSize;
    }

    public long getTrackId(){return trackid;}

    public boolean hasUnusedFrames(){
        for (int i = 0; i < frameList.length; i++){
            if (!frameList[i].getRead()) return true;
        }
        if (apicFrame!=null){
            if (!apicFrame.getRead())return true;
        }
        return false;
    }

    public byte[] getUnusedFramesAsBytes(){
        int unusedFrameSize = 0;
        for (int i = 0; i < frameList.length; i++){
            if (!frameList[i].getRead()) unusedFrameSize += frameList[i].getFrameSize();
        }
        if (apicFrame!=null){
            if (!apicFrame.getRead())unusedFrameSize+=apicFrame.getFrameSize();
        }

        byte[] unusedFrameData = new byte[unusedFrameSize];
        int offset = 0;

        for (int i = 0; i < frameList.length; i++){
            if (!frameList[i].getRead()){
                byte[] frameData = getFrame(i).getFrameAsBytes();
                for (int x = 0; x < frameData.length; x++){
                    unusedFrameData[offset + x] = frameData[x];
                }
                offset += (frameData.length);
            }
        }
        if (apicFrame!=null){
            if (!apicFrame.getRead()){
                byte[] apicFrameData = apicFrame.getFrameAsBytes();
                for (int x = 0; x<apicFrame.getFrameSize(); x++){
                    unusedFrameData[offset+x] = apicFrameData[x];
                }
            }
        }


        return unusedFrameData;
    }

    private void initFrames(){
        for (int i = 0; i<frameList.length;i++){
            if (frameList[i] == null){
                ID3V4FrameHeader frameHeader = new ID3V4FrameHeader();
                frameHeader.setFrameID(getFrameIDs(i));
                frameList[i] = new ID3V4Frame(null, frameHeader);
            }
        }
    }

    private String getFrameIDs(int position){
        switch (position){
            case FRAME_TITLE:{
                return ID3V2FrameIDs.TIT2;
            }
            case FRAME_ARTIST:{
                return ID3V2FrameIDs.TPE2;
            }
            case FRAME_ALBUM:{
                return ID3V2FrameIDs.TALB;
            }
            case FRAME_GENRE:{
                return ID3V2FrameIDs.TCON;
            }
            case FRAME_YEAR:{
                return ID3V2FrameIDs.TDRC;
            }
            case FRAME_COMPOSER:{
                return ID3V2FrameIDs.TCOM;
            }
            case FRAME_TRACKID:{
                return ID3V2FrameIDs.TRCK;
            }
            default:{
                return "";
            }
        }
    }

}
