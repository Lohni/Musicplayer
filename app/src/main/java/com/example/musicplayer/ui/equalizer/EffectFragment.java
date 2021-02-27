package com.example.musicplayer.ui.equalizer;

import android.content.Context;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;
import android.media.audiofx.Virtualizer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.musicplayer.R;
import com.example.musicplayer.ui.playlist.PlaylistInterface;
import com.example.musicplayer.ui.views.ControlKnob;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public class EffectFragment extends Fragment {

    private View view;
    private int audioSessionID;
    private ControlKnob bass, virtualizerView;
    private BassBoost bassBoost;
    private Virtualizer virtualizer;
    private float bassBoostStrengthScale;
    private ChipGroup chipGroup;
    private PresetReverb presetReverb;
    private String[] presetList = {"None", "Small Room", "Medium Room", "Large Room", "Medium Hall", "Large Hall", "Plate"};

    private EqualizerInterface equalizerInterface;

    public EffectFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            equalizerInterface = (EqualizerInterface) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + "must implement EqualizerInterface");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_effect, container, false);
        bass = view.findViewById(R.id.bass_boost);
        chipGroup = view.findViewById(R.id.equalizer_effect_chipgroup);
        virtualizerView = view.findViewById(R.id.virtualizer);
        bass.setOnControlKnobChangeListener(new ControlKnob.OnControlKnobChangeListener() {
            @Override
            public void onChange(float angle) {
                BassBoost.Settings settings = bassBoost.getProperties();
                settings.strength = (short) (bassBoostStrengthScale*angle);
                bassBoost.setProperties(settings);
                AudioEffect.Descriptor descriptor = bassBoost.getDescriptor();
            }
        });
        virtualizerView.setOnControlKnobChangeListener(new ControlKnob.OnControlKnobChangeListener() {
            @Override
            public void onChange(float angle) {
                virtualizer.setStrength((short) (bassBoostStrengthScale*angle));
            }
        });

        createChipGroup();

        return view;
    }

    public void init(int audioSessionid) {
        audioSessionID = audioSessionid;
        bassBoost = new BassBoost(1,audioSessionid);
        virtualizer = new Virtualizer(1,audioSessionid);
        virtualizer.forceVirtualizationMode(Virtualizer.VIRTUALIZATION_MODE_BINAURAL);
        virtualizer.setEnabled(true);
        bassBoost.setEnabled(true);
        presetReverb = new PresetReverb(1,audioSessionid);
        presetReverb.setEnabled(true);
        presetReverb.setEnableStatusListener(new AudioEffect.OnEnableStatusChangeListener() {
            @Override
            public void onEnableStatusChange(AudioEffect audioEffect, boolean b) {
                equalizerInterface.onPresetReverbCreated(presetReverb.getId());
            }
        });
        bassBoostStrengthScale = 1000f/270f;
    }

    private void createChipGroup(){
        for(short i=0;i<7;i++){
            Chip preset = (Chip) getLayoutInflater().inflate(R.layout.equalizer_chip_layout,chipGroup,false);
            preset.setText(presetList[i]);
            preset.setId(i);
            chipGroup.addView(preset);
        }
        chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                presetReverb.setPreset((short) checkedId);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bassBoost.release();
        virtualizer.release();
        presetReverb.release();
    }
}