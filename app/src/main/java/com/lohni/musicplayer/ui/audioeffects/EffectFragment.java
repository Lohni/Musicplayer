package com.lohni.musicplayer.ui.audioeffects;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.lohni.musicplayer.R;
import com.lohni.musicplayer.ui.views.ControlKnob;
import com.lohni.musicplayer.utils.enums.AudioEffectType;

import androidx.fragment.app.Fragment;

public class EffectFragment extends Fragment {
    private ControlKnob bassBoost, virtualizer, loudnessEnhancer;
    private SwitchMaterial bassBoostSwitch, virtualizerSwitch, loudnessEnhancerSwitch;
    private int bassBoostStrength, virtualizerStrength, loudnessEnhancerStrength;

    public EffectFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_effect, container, false);
        bassBoost = view.findViewById(R.id.effects_bass_boost);
        virtualizer = view.findViewById(R.id.effects_virtualizer);
        loudnessEnhancer = view.findViewById(R.id.effects_loudnessEnhancer);
        bassBoostSwitch = view.findViewById(R.id.effects_bass_boost_enabled);
        virtualizerSwitch = view.findViewById(R.id.effects_virtualizer_enabled);
        loudnessEnhancerSwitch = view.findViewById(R.id.effects_loudnessEnhancer_enabled);

        initControlKnobRange();
        initStartValues();
        initListener();

        return view;
    }

    private void initControlKnobRange() {
        bassBoost.setRange(0, 1000);
        virtualizer.setRange(0, 1000);
        loudnessEnhancer.setRange(0, 1000);

        bassBoost.isInfoDrawn(false);
        virtualizer.isInfoDrawn(false);
        loudnessEnhancer.isInfoDrawn(false);
    }

    private void initListener() {
        SharedPreferences sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        bassBoost.setOnControlKnobChangeListener((view, value) -> {
            bassBoostStrength = value;
        });
        virtualizer.setOnControlKnobChangeListener((view, value) -> {
            virtualizerStrength = value;
        });
        loudnessEnhancer.setOnControlKnobChangeListener((view, value) -> {
            if (value < 10) value = 0;
            loudnessEnhancerStrength = value;
        });

        bassBoost.setOnControlKnobActionUpListener(() -> {
            requireActivity().sendBroadcast(new Intent(getString(R.string.musicservice_audioeffect))
                    .putExtra("EFFECT_TYPE", AudioEffectType.Companion.getIntFromAudioEffectType(AudioEffectType.BASSBOOST))
                    .putExtra("STRENGTH", bassBoostStrength));
            sharedPreferences.edit().putInt(getString(R.string.preference_bassboost_strength), bassBoostStrength).apply();
        });
        virtualizer.setOnControlKnobActionUpListener(() -> {
            requireActivity().sendBroadcast(new Intent(getString(R.string.musicservice_audioeffect))
                    .putExtra("EFFECT_TYPE", AudioEffectType.Companion.getIntFromAudioEffectType(AudioEffectType.VIRTUALIZER))
                    .putExtra("STRENGTH", virtualizerStrength));
            sharedPreferences.edit().putInt(getString(R.string.preference_virtualizer_strength), virtualizerStrength).apply();
        });
        loudnessEnhancer.setOnControlKnobActionUpListener(() -> {
            requireActivity().sendBroadcast(new Intent(getString(R.string.musicservice_audioeffect))
                    .putExtra("EFFECT_TYPE", AudioEffectType.Companion.getIntFromAudioEffectType(AudioEffectType.LOUDNESS_ENHANCER))
                    .putExtra("STRENGTH", loudnessEnhancerStrength));
            sharedPreferences.edit().putInt(getString(R.string.preference_loudnessenhancer_strength), loudnessEnhancerStrength).apply();
        });

        bassBoostSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            requireActivity().sendBroadcast(new Intent(getString(R.string.musicservice_audioeffect))
                    .putExtra("EFFECT_TYPE", AudioEffectType.Companion.getIntFromAudioEffectType(AudioEffectType.BASSBOOST))
                    .putExtra("ENABLED", b));
            sharedPreferences.edit().putBoolean(getString(R.string.preference_bassboost_isenabled), b).apply();
            bassBoost.isEnabled(b);
        });
        virtualizerSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            requireActivity().sendBroadcast(new Intent(getString(R.string.musicservice_audioeffect))
                    .putExtra("EFFECT_TYPE", AudioEffectType.Companion.getIntFromAudioEffectType(AudioEffectType.VIRTUALIZER))
                    .putExtra("ENABLED", b));
            sharedPreferences.edit().putBoolean(getString(R.string.preference_virtualizer_isenabled), b).apply();
            virtualizer.isEnabled(b);
        });
        loudnessEnhancerSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            requireActivity().sendBroadcast(new Intent(getString(R.string.musicservice_audioeffect))
                    .putExtra("EFFECT_TYPE", AudioEffectType.Companion.getIntFromAudioEffectType(AudioEffectType.LOUDNESS_ENHANCER))
                    .putExtra("ENABLED", b));
            sharedPreferences.edit().putBoolean(getString(R.string.preference_loudnessenhancer_isenabled), b).apply();
            loudnessEnhancer.isEnabled(b);
        });
    }

    private void initStartValues() {
        bassBoost.setCurrentValue(bassBoostStrength);
        virtualizer.setCurrentValue(virtualizerStrength);
        loudnessEnhancer.setCurrentValue(loudnessEnhancerStrength);
    }
}