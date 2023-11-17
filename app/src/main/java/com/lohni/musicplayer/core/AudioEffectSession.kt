package com.lohni.musicplayer.core

import android.content.Intent
import android.media.audiofx.*
import com.lohni.musicplayer.database.entity.AdvancedReverbPreset
import com.lohni.musicplayer.database.entity.EqualizerPreset
import com.lohni.musicplayer.dto.EqualizerProperties
import com.lohni.musicplayer.utils.enums.AudioEffectType

@Suppress("DEPRECATION")
class AudioEffectSession(audioSessionId: Int) {
    private val environmentalReverb: EnvironmentalReverb = EnvironmentalReverb(1, 0)
    private val equalizer: Equalizer = Equalizer(0, audioSessionId)
    private val bassBoost: BassBoost = BassBoost(1, audioSessionId)
    private val virtualizer: Virtualizer = Virtualizer(1, audioSessionId)
    private val loudnessEnhancer: LoudnessEnhancer = LoudnessEnhancer(audioSessionId)

    init {
        virtualizer.forceVirtualizationMode(Virtualizer.VIRTUALIZATION_MODE_AUTO)
    }

    fun release() {
        environmentalReverb.release()
        equalizer.release()
        bassBoost.release()
        virtualizer.release()
        loudnessEnhancer.release()
    }

    fun setValueFromBundle(intent: Intent) {
        val audioEffectType = AudioEffectType.getAudioEffectFromInt(intent.getIntExtra("EFFECT_TYPE", -1))
        val strength = intent.getIntExtra("STRENGTH", -1)

        if (intent.hasExtra("ENABLED")) setStatus(audioEffectType, intent.getBooleanExtra("ENABLED", false))
        if (strength >= 0) setStrength(audioEffectType, strength)

        val arp: AdvancedReverbPreset? = intent.getParcelableExtra("REVERB")
        if (arp != null) extractReverbValues(arp)

        val eq: EqualizerPreset? = intent.getParcelableExtra("EQUALIZER")
        if (eq != null) setEqualizerBandLevelsByPreset(eq)
    }

    fun getEnvReverbId(): Int {
        return environmentalReverb.id
    }

    fun getEqualizerBandLevels(): ShortArray? {
        val numberBands = equalizer.numberOfBands.toInt()
        val bandLevels = ShortArray(numberBands)
        for (i in 0 until numberBands) {
            bandLevels[i] = equalizer.getBandLevel(i.toShort())
        }
        return bandLevels
    }

    fun getEqualizerProperties(): EqualizerProperties? {
        val centerFreq = IntArray(equalizer.numberOfBands.toInt())
        for (i in centerFreq.indices) {
            centerFreq[i] = equalizer.getCenterFreq(i.toShort())
        }
        return EqualizerProperties(equalizer.bandLevelRange, centerFreq)
    }

    private fun setStatus(audioEffectType: AudioEffectType, enabled: Boolean) {
        when (audioEffectType) {
            AudioEffectType.BASSBOOST -> bassBoost.enabled = enabled
            AudioEffectType.VIRTUALIZER -> virtualizer.enabled = enabled
            AudioEffectType.LOUDNESS_ENHANCER -> virtualizer.enabled = enabled
            AudioEffectType.EQUALIZER -> equalizer.enabled = enabled
            AudioEffectType.ENV_REVERB -> equalizer.enabled = enabled
        }
    }

    private fun setStrength(audioEffectType: AudioEffectType, strength: Int) {
        when (audioEffectType) {
            AudioEffectType.BASSBOOST -> bassBoost.setStrength(strength.toShort())
            AudioEffectType.VIRTUALIZER -> virtualizer.setStrength(strength.toShort())
            AudioEffectType.LOUDNESS_ENHANCER -> loudnessEnhancer.setTargetGain(strength)
            else -> return
        }
    }

    private fun extractReverbValues(reverbSettings: AdvancedReverbPreset) {
        val settings = EnvironmentalReverb.Settings()
        settings.roomLevel = reverbSettings.arMasterLevel.toShort()
        settings.roomHFLevel = reverbSettings.arRoomHfLevel.toShort()
        settings.reverbLevel = reverbSettings.arReverbLevel.toShort()
        settings.reverbDelay = reverbSettings.arReverbDelay
        settings.reflectionsLevel = reverbSettings.arReflectionLevel.toShort()
        settings.reflectionsDelay = reverbSettings.arReflectionDelay
        settings.diffusion = reverbSettings.arDiffusion.toShort()
        settings.density = reverbSettings.arDensity.toShort()
        settings.decayHFRatio = reverbSettings.arDecayHfRatio.toShort()
        settings.decayTime = reverbSettings.arDecayTime
        environmentalReverb.properties = settings
    }

    private fun setEqualizerBandLevelsByPreset(equalizerPreset: EqualizerPreset) {
        val bandLevel = ShortArray(5)
        bandLevel[0] = equalizerPreset.eqLevel1.toShort()
        bandLevel[1] = equalizerPreset.eqLevel2.toShort()
        bandLevel[2] = equalizerPreset.eqLevel3.toShort()
        bandLevel[3] = equalizerPreset.eqLevel4.toShort()
        bandLevel[4] = equalizerPreset.eqLevel5.toShort()
        if (equalizer.numberOfBands.toInt() == bandLevel.size) {
            for (i in bandLevel.indices) {
                equalizer.setBandLevel(i.toShort(), bandLevel[i])
            }
        }
    }
}