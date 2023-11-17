package com.lohni.musicplayer.utils.enums

enum class AudioEffectType {
    BASSBOOST, VIRTUALIZER, LOUDNESS_ENHANCER, EQUALIZER, ENV_REVERB;

    companion object {
        fun getIntFromAudioEffectType(audioEffectType: AudioEffectType): Int {
            return when(audioEffectType) {
                BASSBOOST -> 0
                VIRTUALIZER -> 1
                LOUDNESS_ENHANCER -> 2
                EQUALIZER -> 3
                ENV_REVERB -> 4
            }
        }

        fun getAudioEffectFromInt(value: Int): AudioEffectType {
            return when(value) {
                0 -> BASSBOOST
                1 -> VIRTUALIZER
                2 -> LOUDNESS_ENHANCER
                3 -> EQUALIZER
                4 -> ENV_REVERB
                else -> BASSBOOST
            }
        }
    }
}