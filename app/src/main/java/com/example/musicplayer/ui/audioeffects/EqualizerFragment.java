package com.example.musicplayer.ui.audioeffects;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.musicplayer.R;
import com.example.musicplayer.database.MusicplayerApplication;
import com.example.musicplayer.database.dao.AudioEffectDataAccess;
import com.example.musicplayer.database.entity.EqualizerPreset;
import com.example.musicplayer.database.viewmodel.AudioEffectViewModel;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Optional;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class EqualizerFragment extends Fragment {
    private EqualizerProperties equalizerProperties;
    private short[] bandlevel = new short[5];
    private View view;
    private MaterialAutoCompleteTextView mACT;
    private TextInputLayout textInputLayout;
    private SwitchMaterial enableSwitchMenu;
    private ImageButton presetDelete, presetAdd;

    private AudioEffectInterface audioEffectInterface;
    private AudioEffectViewModel audioEffectViewModel;
    private ArrayAdapter<EqualizerPreset> arrayAdapter;

    private ArrayList<EqualizerPreset> equalizerPresets = new ArrayList<>();

    private int selectedIndex = -1;
    private boolean equalizerEnabled = false;

    public EqualizerFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            audioEffectInterface = (AudioEffectInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + "must implement AudioEffectInterface");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        AudioEffectDataAccess mda = ((MusicplayerApplication) requireActivity().getApplication()).getDatabase().audioEffectDao();
        audioEffectViewModel = new ViewModelProvider(this, new AudioEffectViewModel.AudioEffectViewModelFactory(mda)).get(AudioEffectViewModel.class);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.audioeffect_enable, menu);
        enableSwitchMenu = menu.findItem(R.id.audioeffect_enable).getActionView().findViewById(R.id.audioeffect_enable_switch);

        enableSwitchMenu.setOnCheckedChangeListener((compoundButton, b) -> {
            audioEffectInterface.onEqualizerStatusChanged(b);
            equalizerEnabled = b;
            setViewsEnabled();
        });
        enableSwitchMenu.setChecked(equalizerEnabled);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_equalizer, container, false);
        mACT = view.findViewById(R.id.equalizer_autoCompleteTextView);
        textInputLayout = view.findViewById(R.id.equalizer_textInputLayout);
        presetAdd = view.findViewById(R.id.equalizer_add);
        presetDelete = view.findViewById(R.id.equalizer_delete);

        arrayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, equalizerPresets);
        mACT.setAdapter(arrayAdapter);

        audioEffectViewModel.getAllEqualizerPresets().observe(getViewLifecycleOwner(), list -> {
            if (list != null) {
                equalizerPresets.clear();
                equalizerPresets.addAll(list);

                arrayAdapter.notifyDataSetChanged();

                getCurrentActive();
            }
        });

        mACT.setOnItemClickListener((adapterView, view, i, l) -> {
            EqualizerPreset activePreset = equalizerPresets.get(i);

            if (activePreset.getEqActive().equals(0)) {
                activePreset.setEqActive(1);
            }

            if (selectedIndex >= 0) {
                EqualizerPreset unActivePreset = equalizerPresets.get(selectedIndex);

                if (!unActivePreset.getEqActive().equals(2)) {
                    unActivePreset.setEqActive(0);
                    selectedIndex = i;
                    audioEffectViewModel.updateEqualizerPreset(unActivePreset);
                }
            }

            audioEffectViewModel.updateEqualizerPreset(activePreset);

            getCurrentActive();
        });

        createLayout();
        setViewsEnabled();

        presetAdd.setOnClickListener((button -> {
            if (equalizerEnabled) showAddPresetDialog();
        }));

        presetDelete.setOnClickListener(button -> {
            if (equalizerEnabled && !equalizerPresets.get(selectedIndex).getEqActive().equals(2)) showPresetDeleteDialog();
        });

        return view;
    }

    public void initEqualizerFragment(short[] equalizerBandLevels, boolean equalizerEnabled, EqualizerProperties equalizerProperties) {
        this.bandlevel = equalizerBandLevels;
        this.equalizerEnabled = equalizerEnabled;
        this.equalizerProperties = equalizerProperties;
    }

    private void getCurrentActive() {
        Optional<EqualizerPreset> eqp = equalizerPresets.stream().filter(eq -> eq.getEqActive().equals(1)).findFirst();
        Integer targetActive = (eqp.isPresent()) ? 1 : 2;

        for (int i = 0; i < equalizerPresets.size(); i++) {
            EqualizerPreset equalizerSettings = equalizerPresets.get(i);

            if (equalizerSettings.getEqActive().equals(targetActive)) {
                selectedIndex = i;
                mACT.setText(equalizerSettings.getEqName(), false);

                bandlevel[0] = equalizerSettings.getEqLevel1().shortValue();
                bandlevel[1] = equalizerSettings.getEqLevel2().shortValue();
                bandlevel[2] = equalizerSettings.getEqLevel3().shortValue();
                bandlevel[3] = equalizerSettings.getEqLevel4().shortValue();
                bandlevel[4] = equalizerSettings.getEqLevel5().shortValue();

                updateSeekbars();

                presetDelete.setEnabled(targetActive.equals(1));
                break;
            }
        }
    }

    private void updateSeekbars() {
        for (short i = 0; i < bandlevel.length; i++) {
            SeekBar seekBar = view.findViewById(i);
            int bandlevel = this.bandlevel[i];
            seekBar.setProgress(bandlevel - equalizerProperties.getLowerBandLevel());
        }
    }

    private void createLayout() {
        int numberFreqBands = (short) bandlevel.length;
        short lowerEQBandLevel = equalizerProperties.getLowerBandLevel();
        short upperEQBandLevel = equalizerProperties.getUpperBandLevel();

        LinearLayout linearLayout = view.findViewById(R.id.eqBandHolder);

        long width = getResources().getDisplayMetrics().widthPixels;
        long height = getResources().getDisplayMetrics().heightPixels;

        int laywidth = (int) (width / (numberFreqBands));
        Resources r = getResources();

        int playbackControlHeight = (int) TypedValue.applyDimension(
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

        float margin = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                18f,
                r.getDisplayMetrics()
        );

        float statusBarHeight = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                25f,
                r.getDisplayMetrics()
        );

        float navigationBarHeight = getResources().getDimensionPixelSize(getResources().getIdentifier("navigation_bar_height", "dimen", "android"));

        for (short i = 0; i < numberFreqBands; i++) {
            final short bandIndex = i;
            LinearLayout.LayoutParams textLay = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            textLay.gravity = Gravity.CENTER;
            TextView freq = new TextView(requireContext());
            freq.setGravity(Gravity.CENTER);
            freq.setText((equalizerProperties.getCenterFreqAtIndex(i)) / 1000 + "Hz");
            freq.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
            freq.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorOnBackground));

            LinearLayout rowLayout = new LinearLayout(requireContext());
            rowLayout.setOrientation(LinearLayout.VERTICAL);
            rowLayout.setLayoutParams(new ViewGroup.LayoutParams(laywidth, ViewGroup.LayoutParams.MATCH_PARENT));

            LinearLayout.LayoutParams lowBandlvlLay = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lowBandlvlLay.gravity = Gravity.CENTER;

            LinearLayout.LayoutParams seeklay = new LinearLayout.LayoutParams(
                    (int) (height - appBarHeight - playbackControlHeight - seekbarMargin - freqtextHeight - headertextHeight - statusBarHeight - navigationBarHeight - margin),
                    10);
            seeklay.weight = 1;
            seeklay.gravity = Gravity.CENTER;

            final SeekBar seekBar = new SeekBar(requireContext());
            seekBar.setId(i);
            seekBar.setRotation(270);
            seekBar.setLayoutParams(seeklay);
            seekBar.setMax(upperEQBandLevel - lowerEQBandLevel);
            seekBar.setProgress(bandlevel[i] + upperEQBandLevel);
            seekBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.equalizer_background, requireContext().getTheme())));
            seekBar.setThumbTintList(ColorStateList.valueOf(getResources().getColor(R.color.equalizer_background, requireContext().getTheme())));
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int value, boolean b) {
                    bandlevel[bandIndex] = (short) (value + lowerEQBandLevel);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (selectedIndex >= 0) {
                        EqualizerPreset equalizerSettings = equalizerPresets.get(selectedIndex);

                        equalizerSettings.setEqLevel1((int) bandlevel[0]);
                        equalizerSettings.setEqLevel2((int) bandlevel[1]);
                        equalizerSettings.setEqLevel3((int) bandlevel[2]);
                        equalizerSettings.setEqLevel4((int) bandlevel[3]);
                        equalizerSettings.setEqLevel5((int) bandlevel[4]);

                        audioEffectViewModel.updateEqualizerPreset(equalizerSettings);
                    }
                }
            });

            rowLayout.addView(seekBar);
            rowLayout.addView(freq);
            linearLayout.addView(rowLayout);
        }
    }

    private void setViewsEnabled() {
        textInputLayout.setEnabled(equalizerEnabled);
        presetDelete.setEnabled(equalizerEnabled);
        presetAdd.setEnabled(equalizerEnabled);
        for (short i = 0; i < bandlevel.length; i++) {
            SeekBar seekBar = view.findViewById(i);
            seekBar.setEnabled(equalizerEnabled);
        }
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
            EqualizerPreset equalizerSettings = new EqualizerPreset();
            equalizerSettings.setEqName(input.getText().toString());

            equalizerSettings.setEqLevel1((int) bandlevel[0]);
            equalizerSettings.setEqLevel2((int) bandlevel[1]);
            equalizerSettings.setEqLevel3((int) bandlevel[2]);
            equalizerSettings.setEqLevel4((int) bandlevel[3]);
            equalizerSettings.setEqLevel5((int) bandlevel[4]);

            equalizerSettings.setEqActive(1);
            mACT.setText(equalizerSettings.toString(), false);

            if (selectedIndex >= 0) {
                EqualizerPreset unActivePreset = equalizerPresets.get(selectedIndex);

                if (!unActivePreset.getEqActive().equals(2)) {
                    unActivePreset.setEqActive(0);
                    audioEffectViewModel.updateEqualizerPreset(unActivePreset);
                }
            }

            audioEffectViewModel.insertEqualizerPreset(equalizerSettings);
        });

        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());

        builder.show();
    }

    private void showPresetDeleteDialog() {
        if (selectedIndex >= 0) {
            EqualizerPreset equalizerSettings = equalizerPresets.get(selectedIndex);

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext()).setTitle("Delete");
            builder.setMessage("Delete preset " + equalizerSettings.getEqName() + "?");
            builder.setPositiveButton("Delete", (dialogInterface, i) -> {
                selectedIndex = -1;
                mACT.setText("", false);
                mACT.clearFocus();
                audioEffectViewModel.deleteEqualizerPreset(equalizerSettings);
            });
            builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
            builder.show();
        }
    }
}
