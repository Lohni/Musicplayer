package com.example.musicplayer.ui.audioeffects;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.musicplayer.R;
import com.example.musicplayer.ui.audioeffects.database.EqualizerSettings;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class EqualizerFragment extends Fragment {
    private EqualizerProperties equalizerProperties;
    private short[] bandlevel;
    private ArrayList<EqualizerSettings> equalizerPresets= new ArrayList<>();
    private View view;
    private MaterialAutoCompleteTextView mACT;
    private TextInputLayout textInputLayout;
    private SwitchMaterial enabledSwitch;
    private ImageButton presetDelete, presetAdd;

    private AudioEffectInterface audioEffectInterface;
    private AudioEffectViewModel audioEffectViewModel;
    private ArrayAdapter<EqualizerSettings> arrayAdapter;

    private int selectedIndex = -1;
    private boolean equalizerEnabled = false;

    public EqualizerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            audioEffectInterface = (AudioEffectInterface) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + "must implement AudioEffectInterface");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_equalizer, container, false);
        mACT = view.findViewById(R.id.equalizer_autoCompleteTextView);
        textInputLayout = view.findViewById(R.id.equalizer_textInputLayout);
        enabledSwitch = view.findViewById(R.id.equalizer_enabledSwitch);
        presetAdd = view.findViewById(R.id.equalizer_add);
        presetDelete = view.findViewById(R.id.equalizer_delete);

        audioEffectViewModel = new ViewModelProvider(requireActivity()).get(AudioEffectViewModel.class);
        audioEffectViewModel.getAllEqualizerPresets().observe(getViewLifecycleOwner(), list -> {
            if (list != null){
                equalizerPresets.clear();
                equalizerPresets.addAll(list);
                if (arrayAdapter == null){
                    arrayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, equalizerPresets);
                    mACT.setAdapter(arrayAdapter);
                }
                getCurrentActive();
            }
        });

        mACT.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                EqualizerSettings activePreset = equalizerPresets.get(i);
                activePreset.setIsSelected(1);

                if (selectedIndex >= 0){
                    EqualizerSettings unActivePreset = equalizerPresets.get(selectedIndex);
                    unActivePreset.setIsSelected(0);

                    selectedIndex = i;

                    audioEffectViewModel.updateEqualizerSettings(unActivePreset, activePreset);
                } else{
                    audioEffectViewModel.updateEqualizerSetting(activePreset);
                }
            }
        });

        createLayout();
        setViewsEnabled();
        presetAdd.setOnClickListener((button -> {
            if (equalizerEnabled)showAddPresetDialog();
        }));
        presetDelete.setOnClickListener(button -> {
            if (equalizerEnabled)showPresetDeleteDialog();
        });
        enabledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                audioEffectInterface.onEqualizerStatusChanged(b);
                equalizerEnabled = b;
                setViewsEnabled();
            }
        });
        enabledSwitch.setChecked(equalizerEnabled);

        return view;
    }

    public void initEqualizerFragment(short[] equalizerBandLevels, boolean equalizerEnabled, EqualizerProperties equalizerProperties){
        this.bandlevel = equalizerBandLevels;
        this.equalizerEnabled = equalizerEnabled;
        this.equalizerProperties = equalizerProperties;
    }


    private void getCurrentActive(){
        for (int i = 0; i<equalizerPresets.size(); i++){
            EqualizerSettings equalizerSettings = equalizerPresets.get(i);
            if (equalizerSettings.getIsSelected() == 1){
                selectedIndex = i;
                bandlevel = equalizerSettings.getBandLevels();
                mACT.setText(equalizerSettings.toString(), false);
                updateSeekbars();
                audioEffectInterface.onEqualizerChanged(bandlevel);
                break;
            }
        }
    }

    private void updateSeekbars(){
        for (short i=0; i < bandlevel.length;i++){
            SeekBar seekBar = view.findViewById(i);
            int bandlevel = this.bandlevel[i];
            seekBar.setProgress(bandlevel - equalizerProperties.getLowerBandLevel());
        }
    }

    private void createLayout(){
        int numberFreqBands = (short) bandlevel.length;
        short lowerEQBandLevel = equalizerProperties.getLowerBandLevel();
        short upperEQBandLevel = equalizerProperties.getUpperBandLevel();

        LinearLayout linearLayout = view.findViewById(R.id.eqBandHolder);

        long width = getResources().getDisplayMetrics().widthPixels;
        long height = getResources().getDisplayMetrics().heightPixels;

        int laywidth = (int) (width/(numberFreqBands));
        Resources r = getResources();

        int playbackControlHeight =(int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                64f,
                r.getDisplayMetrics()
        );

        float appBarHeight = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                50f,
                r.getDisplayMetrics()
        );

        float seekbarMargin = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                16f,
                r.getDisplayMetrics()
        );

        float freqtextHeight = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                16f,
                r.getDisplayMetrics()
        );

        float headertextHeight = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                22f,
                r.getDisplayMetrics()
        );

        float chipLayoutHeight = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                66f,
                r.getDisplayMetrics()
        );

        float statusBarHeight = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                25f,
                r.getDisplayMetrics()
        );

        float navigationBarHeight = getResources().getDimensionPixelSize(getResources().getIdentifier("navigation_bar_height", "dimen", "android"));

        for(short i=0;i<numberFreqBands;i++){
            final short bandIndex = i;
            LinearLayout.LayoutParams textLay = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            textLay.gravity=Gravity.CENTER;
            TextView freq = new TextView(requireContext());
            freq.setGravity(Gravity.CENTER);
            freq.setText((equalizerProperties.getCenterFreqAtIndex(i))/1000 + "Hz");
            freq.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
            freq.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextLight));

            LinearLayout rowLayout = new LinearLayout(requireContext());
            rowLayout.setOrientation(LinearLayout.VERTICAL);
            rowLayout.setLayoutParams(new ViewGroup.LayoutParams(laywidth, ViewGroup.LayoutParams.MATCH_PARENT));

            LinearLayout.LayoutParams lowBandlvlLay = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lowBandlvlLay.gravity=Gravity.CENTER;

            LinearLayout.LayoutParams seeklay = new LinearLayout.LayoutParams(
                    (int) (height-appBarHeight-playbackControlHeight - seekbarMargin - freqtextHeight - headertextHeight - chipLayoutHeight - statusBarHeight - navigationBarHeight),
                    10);
            seeklay.weight=1;
            seeklay.gravity=Gravity.CENTER;

            final SeekBar seekBar = new SeekBar(requireContext());
            seekBar.setId(i);
            seekBar.setRotation(270);
            seekBar.setLayoutParams(seeklay);
            seekBar.setMax(upperEQBandLevel - lowerEQBandLevel);
            seekBar.setProgress(bandlevel[i] + upperEQBandLevel);
            seekBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.equalizer_background)));
            seekBar.setThumbTintList(ColorStateList.valueOf(getResources().getColor(R.color.equalizer_background)));
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int value, boolean b) {
                    bandlevel[bandIndex] = (short)(value+lowerEQBandLevel);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (selectedIndex >= 0){
                        EqualizerSettings equalizerSettings = equalizerPresets.get(selectedIndex);
                        equalizerSettings.setBandLevels(bandlevel);
                        audioEffectViewModel.updateEqualizerSetting(equalizerSettings);
                    }
                }
            });

            rowLayout.addView(seekBar);
            rowLayout.addView(freq);
            linearLayout.addView(rowLayout);
        }
    }

    private void setViewsEnabled(){
        textInputLayout.setEnabled(equalizerEnabled);
        presetDelete.setEnabled(equalizerEnabled);
        presetAdd.setEnabled(equalizerEnabled);
        for (short i=0; i < bandlevel.length;i++){
            SeekBar seekBar = view.findViewById(i);
            seekBar.setEnabled(equalizerEnabled);
        }
    }

    /*
    Dialogs
    */
    private void showAddPresetDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext()).setTitle("New preset");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                EqualizerSettings equalizerSettings = new EqualizerSettings();
                equalizerSettings.setEqualizer_preset_name(input.getText().toString());
                equalizerSettings.setBandLevels(bandlevel);
                equalizerSettings.setIsSelected(1);
                mACT.setText(equalizerSettings.toString(), false);

                if (selectedIndex >= 0) {
                    EqualizerSettings unActivePreset = equalizerPresets.get(selectedIndex);
                    unActivePreset.setIsSelected(0);
                    audioEffectViewModel.updateEqualizerSetting(unActivePreset);
                }

                audioEffectViewModel.createNewEqualizerPreset(equalizerSettings);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.show();
    }

    private void showPresetDeleteDialog(){
        if (selectedIndex >= 0){
            EqualizerSettings equalizerSettings = equalizerPresets.get(selectedIndex);
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext()).setTitle("Delete");
            builder.setMessage("Delete preset " + equalizerSettings.toString() + "?");
            builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    selectedIndex = -1;
                    mACT.setText("", false);
                    mACT.clearFocus();
                    audioEffectViewModel.deleteEqualizerPreset(equalizerSettings);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            builder.show();
        }
    }

}
