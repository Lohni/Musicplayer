package com.example.musicplayer.ui.audioeffects;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.example.musicplayer.R;
import com.example.musicplayer.ui.views.ControlKnob;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class EffectFragment extends Fragment {

    private View view;
    private ControlKnob bassBoost, virtualizer, loudnessEnhancer;
    private AudioEffectInterface audioEffectInterface;
    private SwitchMaterial bassBoostSwitch, virtualizerSwitch, loudnessEnhancerSwitch;
    private int bassBoostStrength, virtualizerStrength, loudnessEnhancerStrength;
    private boolean bassBoostEnabled, virtualizerEnabled, loudnessEnhancerEnabled;

    public EffectFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            audioEffectInterface = (AudioEffectInterface) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + "must implement EqualizerInterface");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_effect, container, false);
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
        bassBoost.setRange(0,1000);
        virtualizer.setRange(0,1000);
        loudnessEnhancer.setRange(0,1000);

        bassBoost.isInfoDrawn(false);
        virtualizer.isInfoDrawn(false);
        loudnessEnhancer.isInfoDrawn(false);
    }

    private void initListener(){
        bassBoost.setOnControlKnobChangeListener((view, value) -> {
            bassBoostStrength = value; });
        virtualizer.setOnControlKnobChangeListener((view, value) -> {
            virtualizerStrength = value; });
        loudnessEnhancer.setOnControlKnobChangeListener((view, value) -> {
            if (value < 10)value = 0;
            loudnessEnhancerStrength = value; });

        bassBoost.setOnControlKnobActionUpListener(() -> {
            audioEffectInterface.onBassBoostChanged(bassBoostStrength);
        });
        virtualizer.setOnControlKnobActionUpListener(() -> {
            audioEffectInterface.onVirtualizerChanged(virtualizerStrength);
        });
        loudnessEnhancer.setOnControlKnobActionUpListener(() -> {
            audioEffectInterface.onLoudnessEnhancerChanged(loudnessEnhancerStrength);
        });
        
        bassBoostSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                bassBoostEnabled = b;
                audioEffectInterface.onBassBoostStatusChanged(b);
                bassBoost.isEnabled(b);
            }
        });
        virtualizerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                virtualizerEnabled = b;
                audioEffectInterface.onVirtualizerStatusChanged(b);
                virtualizer.isEnabled(b);
            }
        });
        loudnessEnhancerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                loudnessEnhancerEnabled = b;
                audioEffectInterface.onLoudnessEnhancerStatusChanged(b);
                loudnessEnhancer.isEnabled(b);
            }
        });
    }

    private void initStartValues(){
        bassBoost.setCurrentValue(bassBoostStrength);
        virtualizer.setCurrentValue(virtualizerStrength);
        loudnessEnhancer.setCurrentValue(loudnessEnhancerStrength);

        bassBoost.isEnabled(bassBoostEnabled);
        virtualizer.isEnabled(virtualizerEnabled);
        loudnessEnhancer.isEnabled(loudnessEnhancerEnabled);

        bassBoostSwitch.setChecked(bassBoostEnabled);
        virtualizerSwitch.setChecked(virtualizerEnabled);
        loudnessEnhancerSwitch.setChecked(loudnessEnhancerEnabled);
    }

    public void setEffectStartingValues(int bassBoostStrength, boolean bassBoostEnabled, int virtualizerStrength, boolean virtualizerEnabled, int loudnessEnhancerStrength, boolean loudnessEnhancerEnabled){
        this.bassBoostStrength = bassBoostStrength;
        this.virtualizerStrength = virtualizerStrength;
        this.loudnessEnhancerStrength = loudnessEnhancerStrength;
        this.bassBoostEnabled = bassBoostEnabled;
        this.virtualizerEnabled = virtualizerEnabled;
        this.loudnessEnhancerEnabled = loudnessEnhancerEnabled;
    }

}