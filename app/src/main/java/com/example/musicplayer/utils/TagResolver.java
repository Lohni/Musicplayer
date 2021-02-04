package com.example.musicplayer.utils;

public class TagResolver {

    private ID3V4Frame titleFrame, artistFrame, albumFrame, genreFrame, yearFrame, composerFrame, trackIdFrame;
    private int oldCombinedSize=0;
    private long trackid;
    public TagResolver(long trackid){this.trackid = trackid;}

    public void setTitleFrame(ID3V4Frame titleFrame) {
        this.titleFrame = titleFrame;
    }

    public void setArtistFrame(ID3V4Frame artistFrame) {
        this.artistFrame = artistFrame;
    }

    public void setAlbumFrame(ID3V4Frame albumFrame) {
        this.albumFrame = albumFrame;
    }

    public void setGenreFrame(ID3V4Frame genreFrame) {
        this.genreFrame = genreFrame;
    }

    public void setYearFrame(ID3V4Frame yearFrame) {
        this.yearFrame = yearFrame;
    }

    public void setComposerFrame(ID3V4Frame composerFrame) {
        this.composerFrame = composerFrame;
    }

    public void setTrackIdFrame(ID3V4Frame trackIdFrame){
        this.trackIdFrame = trackIdFrame;
    }

    //Has to be called after Audiofile first read
    public void calculateCombinedSize(){
        if (titleFrame != null)oldCombinedSize+=titleFrame.getFrameSize();
        if (artistFrame != null)oldCombinedSize+=artistFrame.getFrameSize();
        if (albumFrame != null)oldCombinedSize+=albumFrame.getFrameSize();
        if (genreFrame != null)oldCombinedSize+=genreFrame.getFrameSize();
        if (yearFrame != null)oldCombinedSize+=yearFrame.getFrameSize();
        if (composerFrame != null)oldCombinedSize+=composerFrame.getFrameSize();
        if (trackIdFrame != null)oldCombinedSize+=trackIdFrame.getFrameSize();
    }

    public int getChangedContentSize(){
        int newCombinedSize=0;
        if (titleFrame != null)newCombinedSize+=titleFrame.getFrameSize();
        if (artistFrame != null)newCombinedSize+=artistFrame.getFrameSize();
        if (albumFrame != null)newCombinedSize+=albumFrame.getFrameSize();
        if (genreFrame != null)newCombinedSize+=genreFrame.getFrameSize();
        if (yearFrame != null)newCombinedSize+=yearFrame.getFrameSize();
        if (composerFrame != null)newCombinedSize+=composerFrame.getFrameSize();
        if (trackIdFrame != null)newCombinedSize+=trackIdFrame.getFrameSize();

        return newCombinedSize - oldCombinedSize;
    }

    public ID3V4Frame getTitleFrame() {
        return titleFrame;
    }

    public ID3V4Frame getArtistFrame() {
        return artistFrame;
    }

    public ID3V4Frame getAlbumFrame() {
        return albumFrame;
    }

    public ID3V4Frame getGenreFrame() {
        return genreFrame;
    }

    public ID3V4Frame getYearFrame() {
        return yearFrame;
    }

    public ID3V4Frame getComposerFrame() {
        return composerFrame;
    }

    public ID3V4Frame getTrackIdFrame() {
        return trackIdFrame;
    }

    public long getTrackId(){return trackid;}
}
