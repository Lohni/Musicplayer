package com.lohni.musicplayer.ui.audioeffects;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.lohni.musicplayer.R;
import com.lohni.musicplayer.database.MusicplayerApplication;
import com.lohni.musicplayer.database.dao.AudioEffectDataAccess;
import com.lohni.musicplayer.database.entity.AdvancedReverbPreset;
import com.lohni.musicplayer.database.viewmodel.AudioEffectViewModel;
import com.lohni.musicplayer.ui.views.ControlKnob;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;


public class ReverbFragment extends Fragment {

    private ControlKnob reverb_delay, reverb_hflevel, reverb_level, reflection_level, reflection_delay,
            reflection_density, reflection_diffusion, decay_time, decay_hfratio, master;
    private SwitchMaterial reverb_switch;
    private ImageButton addPreset, deletePreset;
    private TextInputLayout textInputLayout;
    private int selectedIndex = -1;

    private AudioEffectViewModel audioEffectViewModel;
    private final ArrayList<AdvancedReverbPreset> reverb_presets = new ArrayList<>();
    private AdvancedReverbPreset currentState;

    private MaterialAutoCompleteTextView mACT;
    private ArrayAdapter<AdvancedReverbPreset> arrayAdapter;
    private SharedPreferences sharedPreferences;
    private boolean enabled = false;

    public ReverbFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AudioEffectDataAccess mda = ((MusicplayerApplication) requireActivity().getApplication()).getDatabase().audioEffectDao();
        audioEffectViewModel = new ViewModelProvider(this, new AudioEffectViewModel.AudioEffectViewModelFactory(mda)).get(AudioEffectViewModel.class);
        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        enabled = sharedPreferences.getBoolean(getString(R.string.preference_reverb_isenabled), false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reverb, container, false);
        mACT = view.findViewById(R.id.reverb_autocompletetextView);
        textInputLayout = view.findViewById(R.id.reverb_textField);

        audioEffectViewModel.getAllAdvancedReverbPresets().observe(getViewLifecycleOwner(), reverb_presets -> {
            if (reverb_presets != null) {
                this.reverb_presets.clear();
                this.reverb_presets.addAll(reverb_presets);

                if (arrayAdapter == null) {
                    arrayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, this.reverb_presets);
                    mACT.setAdapter(arrayAdapter);
                }
                getCurrentActive();
            }
        });

        mACT.setOnItemClickListener((adapterView, view1, i, l) -> {
            AdvancedReverbPreset activePreset = reverb_presets.get(i);
            activePreset.setArActive(1);
            currentState = activePreset.clone();

            if (selectedIndex >= 0) {
                AdvancedReverbPreset unActivePreset = reverb_presets.get(selectedIndex);
                unActivePreset.setArActive(0);
                selectedIndex = i;
                audioEffectViewModel.updateAdvancedReverbPreset(unActivePreset);
            }

            audioEffectViewModel.updateAdvancedReverbPreset(activePreset);
        });


        addPreset = view.findViewById(R.id.reverb_add);
        deletePreset = view.findViewById(R.id.reverb_delete);

        initialiseViews(view);
        initialiseValues();
        updateKnobsState(enabled);
        initialiseInfo();
        ControlKnob.OnInfoClickedListener onInfoClickedListener = this::showInfoDialog;
        ControlKnob.OnControlKnobActionUpListener onControlKnobActionUpListener = this::persistControlKnobChanged;
        initialiseListener(onInfoClickedListener, onControlKnobActionUpListener);
        reverb_switch.setChecked(enabled);
        reverb_switch.setOnCheckedChangeListener((compoundButton, state) -> {
            sharedPreferences.edit().putBoolean(getString(R.string.preference_reverb_isenabled), state).apply();
            updateKnobsState(state);
        });

        addPreset.setOnClickListener((btnView -> {
            showAddPresetDialog();
        }));

        deletePreset.setOnClickListener((btnView -> {
            if (selectedIndex >= 0) {
                showPresetDeleteDialog();
            }
        }));

        return view;
    }

    /*
    Utils
     */

    private void updateKnobs() {
        if (selectedIndex >= 0) {
            reverb_delay.setCurrentValue(currentState.getArReverbDelay());
            reverb_hflevel.setCurrentValue(currentState.getArRoomHfLevel());
            reverb_level.setCurrentValue(currentState.getArReverbLevel());
            reflection_level.setCurrentValue(currentState.getArReflectionLevel());
            reflection_delay.setCurrentValue(currentState.getArReflectionDelay());
            reflection_density.setCurrentValue(currentState.getArDensity());
            reflection_diffusion.setCurrentValue(currentState.getArDiffusion());
            decay_time.setCurrentValue(currentState.getArDecayTime());
            decay_hfratio.setCurrentValue(currentState.getArDecayHfRatio());
            master.setCurrentValue(currentState.getArMasterLevel());
        }
    }

    private void updateKnobsState(boolean state) {
        reverb_delay.isEnabled(state);
        reverb_hflevel.isEnabled(state);
        reverb_level.isEnabled(state);
        reflection_level.isEnabled(state);
        reflection_delay.isEnabled(state);
        reflection_density.isEnabled(state);
        reflection_diffusion.isEnabled(state);
        decay_time.isEnabled(state);
        decay_hfratio.isEnabled(state);
        master.isEnabled(state);
        textInputLayout.setEnabled(state);
        deletePreset.setEnabled(state);
        addPreset.setEnabled(state);
    }

    private void getCurrentActive() {
        for (int i = 0; i < reverb_presets.size(); i++) {
            AdvancedReverbPreset reverbSettings = reverb_presets.get(i);
            if (reverbSettings.getArActive().equals(1)) {
                selectedIndex = i;
                currentState = reverbSettings.clone();
                mACT.setText(reverbSettings.toString(), false);
                updateKnobs();
                break;
            }
        }

        if (currentState == null) {
            currentState = new AdvancedReverbPreset();
            currentState.setArReverbLevel(-9000);
            currentState.setArReverbDelay(0);
            currentState.setArRoomHfLevel(-9000);
            currentState.setArReflectionLevel(-9000);
            currentState.setArReflectionDelay(0);
            currentState.setArDensity(0);
            currentState.setArDiffusion(0);
            currentState.setArDecayTime(100);
            currentState.setArDecayHfRatio(100);
            currentState.setArMasterLevel(-9000);
        }
    }

    private void persistControlKnobChanged() {
        if (selectedIndex >= 0) {
            audioEffectViewModel.updateAdvancedReverbPreset(currentState);
            requireActivity().sendBroadcast(new Intent(getString(R.string.musicservice_reverb_values)).putExtra("VALUES", currentState));
        }
    }

    /*
    Init views
     */
    private void initialiseViews(View view) {
        reverb_delay = view.findViewById(R.id.reverb_delay);
        reverb_hflevel = view.findViewById(R.id.reverb_hfLevel);
        reverb_level = view.findViewById(R.id.reverb_level);
        reflection_level = view.findViewById(R.id.reverb_reflection_level);
        reflection_delay = view.findViewById(R.id.reverb_reflection_delay);
        reflection_density = view.findViewById(R.id.reverb_reflection_density);
        reflection_diffusion = view.findViewById(R.id.reverb_reflection_diffusion);
        decay_time = view.findViewById(R.id.reverb_delay_time);
        decay_hfratio = view.findViewById(R.id.reverb_delay_hfratio);
        master = view.findViewById(R.id.reverb_master);
        reverb_switch = view.findViewById(R.id.reverb_enabled);
    }

    private void initialiseValues() {
        //Set Min-Max recording to dev-documentation
        reverb_delay.setRange(0, 100);
        reverb_hflevel.setRange(-9000, 0);
        reverb_level.setRange(-9000, 2000);
        reflection_level.setRange(-9000, 1000);
        reflection_delay.setRange(0, 300);
        reflection_density.setRange(0, 1000);
        reflection_diffusion.setRange(0, 1000);
        decay_time.setRange(100, 20000);
        decay_hfratio.setRange(100, 2000);
        master.setRange(-9000, 0);
    }

    private void initialiseListener(ControlKnob.OnInfoClickedListener onInfoClickedListener, ControlKnob.OnControlKnobActionUpListener onControlKnobActionUpListener) {
        reverb_level.setOnControlKnobChangeListener((view, value) -> currentState.setArReverbLevel(value));
        reverb_delay.setOnControlKnobChangeListener((view, value) -> currentState.setArReverbDelay(value));
        reverb_hflevel.setOnControlKnobChangeListener((view, value) -> currentState.setArRoomHfLevel(value));
        reflection_level.setOnControlKnobChangeListener((view, value) -> currentState.setArReflectionLevel(value));
        reflection_delay.setOnControlKnobChangeListener((view, value) -> currentState.setArReflectionDelay(value));
        reflection_density.setOnControlKnobChangeListener((view, value) -> currentState.setArDensity(value));
        reflection_diffusion.setOnControlKnobChangeListener((view, value) -> currentState.setArDiffusion(value));
        decay_time.setOnControlKnobChangeListener((view, value) -> currentState.setArDecayTime(value));
        decay_hfratio.setOnControlKnobChangeListener((view, value) -> currentState.setArDecayHfRatio(value));
        master.setOnControlKnobChangeListener((view, value) -> currentState.setArMasterLevel(value));

        reverb_level.setOnInfoClickListener(onInfoClickedListener);
        reverb_delay.setOnInfoClickListener(onInfoClickedListener);
        reverb_hflevel.setOnInfoClickListener(onInfoClickedListener);
        reflection_level.setOnInfoClickListener(onInfoClickedListener);
        reflection_delay.setOnInfoClickListener(onInfoClickedListener);
        reflection_density.setOnInfoClickListener(onInfoClickedListener);
        reflection_diffusion.setOnInfoClickListener(onInfoClickedListener);
        decay_time.setOnInfoClickListener(onInfoClickedListener);
        decay_hfratio.setOnInfoClickListener(onInfoClickedListener);
        master.setOnInfoClickListener(onInfoClickedListener);

        reverb_level.setOnControlKnobActionUpListener(onControlKnobActionUpListener);
        reverb_delay.setOnControlKnobActionUpListener(onControlKnobActionUpListener);
        reverb_hflevel.setOnControlKnobActionUpListener(onControlKnobActionUpListener);
        reflection_level.setOnControlKnobActionUpListener(onControlKnobActionUpListener);
        reflection_delay.setOnControlKnobActionUpListener(onControlKnobActionUpListener);
        reflection_density.setOnControlKnobActionUpListener(onControlKnobActionUpListener);
        reflection_diffusion.setOnControlKnobActionUpListener(onControlKnobActionUpListener);
        decay_time.setOnControlKnobActionUpListener(onControlKnobActionUpListener);
        decay_hfratio.setOnControlKnobActionUpListener(onControlKnobActionUpListener);
        master.setOnControlKnobActionUpListener(onControlKnobActionUpListener);
    }

    private void initialiseInfo() {
        reverb_delay.setInfoText(requireContext().getResources().getString(R.string.reverb_delay));
        reverb_hflevel.setInfoText(requireContext().getResources().getString(R.string.reverb_hflevel));
        reverb_level.setInfoText(requireContext().getResources().getString(R.string.reverb_level));
        reflection_level.setInfoText(requireContext().getResources().getString(R.string.reflection_level));
        reflection_delay.setInfoText(requireContext().getResources().getString(R.string.reflection_delay));
        reflection_density.setInfoText(requireContext().getResources().getString(R.string.reflection_density));
        reflection_diffusion.setInfoText(requireContext().getResources().getString(R.string.reflection_diffusion));
        decay_time.setInfoText(requireContext().getResources().getString(R.string.decay_time));
        decay_hfratio.setInfoText(requireContext().getResources().getString(R.string.decay_hfratio));
        master.setInfoText(requireContext().getResources().getString(R.string.reverb_master));
    }

    /*
    Dialogs
     */
    private void showAddPresetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext()).setTitle("New preset");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Create", (dialogInterface, i) -> {
            currentState.setArId(null);
            currentState.setArName(input.getText().toString());
            currentState.setArActive(1);
            mACT.setText(currentState.toString(), false);

            if (selectedIndex >= 0) {
                AdvancedReverbPreset unActivePreset = reverb_presets.get(selectedIndex);
                unActivePreset.setArActive(0);
                audioEffectViewModel.updateAdvancedReverbPreset(unActivePreset);
            }

            audioEffectViewModel.insertAdvancedReverbPreset(currentState);
        });
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
        builder.show();
    }

    private void showPresetDeleteDialog() {
        if (selectedIndex >= 0) {
            AdvancedReverbPreset reverbSettings = reverb_presets.get(selectedIndex);
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext()).setTitle("Delete");
            builder.setMessage("Delete preset " + reverbSettings.toString() + "?");
            builder.setPositiveButton("Delete", (dialogInterface, i) -> {
                selectedIndex = -1;
                mACT.setText("", false);
                audioEffectViewModel.deleteAdvancedReverbPreset(reverbSettings);
            });
            builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
            builder.show();
        }
    }

    private void showInfoDialog(String infoText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext()).setTitle("Info");
        builder.setMessage(infoText);
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
        builder.show();
    }
}