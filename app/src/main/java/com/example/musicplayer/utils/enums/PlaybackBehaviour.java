package com.example.musicplayer.utils.enums;

public class PlaybackBehaviour {

    public enum PlaybackBehaviourState {
        SHUFFLE, REPEAT_LIST, REPEAT_SONG, PLAY_ORDER
    }

    public static PlaybackBehaviourState getNextState(PlaybackBehaviourState state) {
        switch (state) {
            case SHUFFLE: {
                return PlaybackBehaviourState.REPEAT_LIST;
            }
            case PLAY_ORDER: {
                return PlaybackBehaviourState.SHUFFLE;
            }
            case REPEAT_LIST: {
                return PlaybackBehaviourState.REPEAT_SONG;
            }
            case REPEAT_SONG: {
                return PlaybackBehaviourState.PLAY_ORDER;
            }
        }
        return state;
    }

    public static int getStateAsInteger(PlaybackBehaviourState state) {
        switch (state) {
            case SHUFFLE: {
                return 0;
            }
            case REPEAT_LIST: {
                return 1;
            }
            case REPEAT_SONG: {
                return 2;
            }
            case PLAY_ORDER: {
                return 3;
            }
        }

        return 3;
    }

    public static PlaybackBehaviourState getStateFromInteger(int state) {
        switch (state) {
            case 0: {
                return PlaybackBehaviourState.SHUFFLE;
            }
            case 1: {
                return PlaybackBehaviourState.REPEAT_LIST;
            }
            case 2: {
                return PlaybackBehaviourState.REPEAT_SONG;
            }
            case 3: {
                return PlaybackBehaviourState.PLAY_ORDER;
            }
        }

        return PlaybackBehaviourState.PLAY_ORDER;
    }
}
