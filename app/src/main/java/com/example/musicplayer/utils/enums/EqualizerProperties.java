package com.example.musicplayer.utils.enums;

public class EqualizerProperties {

    private final short numberFreqBands;
    private final short[] bandLevelRange;
    private final int[] centerFreqs;

    public EqualizerProperties(short numberFreqBands, short[] bandLevelRange, int[] centerFreqs) {
        this.numberFreqBands = numberFreqBands;
        this.bandLevelRange = bandLevelRange;
        this.centerFreqs = centerFreqs;
    }

    public short getNumberFreqBands() {
        return numberFreqBands;
    }

    public short getLowerBandLevel() {
        return bandLevelRange[0];
    }

    public short getUpperBandLevel() {
        return bandLevelRange[1];
    }

    public short[] getBandLevelRange() {
        return bandLevelRange;
    }

    public int[] getCenterFreqs() {
        return centerFreqs;
    }

    public int getCenterFreqAtIndex(int index) {
        if (index >= 0 && index < centerFreqs.length) {
            return centerFreqs[index];
        }
        return -1;
    }

}
