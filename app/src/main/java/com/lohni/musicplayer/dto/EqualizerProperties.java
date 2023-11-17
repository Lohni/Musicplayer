package com.lohni.musicplayer.dto;

public class EqualizerProperties {

    private final short[] bandLevelRange;
    private final int[] centerFreqs;

    public EqualizerProperties(short[] bandLevelRange, int[] centerFreqs) {
        this.bandLevelRange = bandLevelRange;
        this.centerFreqs = centerFreqs;
    }

    public short getLowerBandLevel() {
        return bandLevelRange[0];
    }

    public short getUpperBandLevel() {
        return bandLevelRange[1];
    }


    public int getCenterFreqAtIndex(int index) {
        if (index >= 0 && index < centerFreqs.length) {
            return centerFreqs[index];
        }
        return -1;
    }

}
