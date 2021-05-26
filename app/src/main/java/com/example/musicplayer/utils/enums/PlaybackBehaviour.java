package com.example.musicplayer.utils.enums;

public class PlaybackBehaviour {

    public enum PlaybackBehaviourState{
        SHUFFLE, REPEAT_LIST, REPEAT_SONG, PLAY_ORDER
    }

    public static PlaybackBehaviourState getNextState(PlaybackBehaviourState state){
        switch (state){
            case SHUFFLE:{
                return PlaybackBehaviourState.REPEAT_LIST;
            }
            case PLAY_ORDER:{
                return PlaybackBehaviourState.SHUFFLE;
            }
            case REPEAT_LIST:{
                return PlaybackBehaviourState.REPEAT_SONG;
            }
            case REPEAT_SONG:{
                return PlaybackBehaviourState.PLAY_ORDER;
            }
        }
        return state;
    }
}
