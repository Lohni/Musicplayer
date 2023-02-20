package com.lohni.musicplayer.utils;

import android.media.audiofx.Equalizer;

import com.lohni.musicplayer.database.entity.EqualizerPreset;
import com.lohni.musicplayer.utils.enums.PlaybackAction;
import com.lohni.musicplayer.utils.enums.PlaybackBehaviourState;

import java.util.Random;

public class ServiceUtil {
    public static int getNextSongIndex(PlaybackBehaviourState behaviour, PlaybackAction action, Integer songlistSize, int currSongIndex) {
        switch (behaviour) {
            case SHUFFLE:
                Random random = new Random();
                currSongIndex = random.nextInt(songlistSize);
                break;
            case REPEAT_LIST:
                if (action == PlaybackAction.SKIP_NEXT) {
                    currSongIndex++;
                    if (currSongIndex == songlistSize) {
                        currSongIndex = 0;
                    }
                } else if (action == PlaybackAction.SKIP_PREVIOUS) {
                    currSongIndex--;
                    if (currSongIndex < 0) {
                        currSongIndex = songlistSize - 1;
                    }
                }

                break;
            case REPEAT_SONG:
                break;
        }
        return currSongIndex;
    }

    public static void setEqualizerBandLevelsByPreset(Equalizer equalizer, EqualizerPreset equalizerPreset) {
        short[] bandLevel = new short[5];

        bandLevel[0] = equalizerPreset.getEqLevel1().shortValue();
        bandLevel[1] = equalizerPreset.getEqLevel2().shortValue();
        bandLevel[2] = equalizerPreset.getEqLevel3().shortValue();
        bandLevel[3] = equalizerPreset.getEqLevel4().shortValue();
        bandLevel[4] = equalizerPreset.getEqLevel5().shortValue();

        if (equalizer.getNumberOfBands() == bandLevel.length) {
            for (short i = 0; i < bandLevel.length; i++) {
                equalizer.setBandLevel(i, bandLevel[i]);
            }
        }
    }
}
