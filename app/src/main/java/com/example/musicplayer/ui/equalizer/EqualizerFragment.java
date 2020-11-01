package com.example.musicplayer.ui.equalizer;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.audiofx.Equalizer;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.musicplayer.R;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public class EqualizerFragment extends Fragment {

    private Equalizer equalizer;
    private static short numberFreqBands,lowerEQBandLevel, upperEQBandLevel,BandLevel;
    private short[] bandlevelstart;
    private ArrayList<Integer> centerfreq;
    private ArrayList<String> presetlist;
    private View view;
    private ChipGroup chipGroup;

    public EqualizerFragment() {
        // Required empty public constructor
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_equalizer, container, false);
        chipGroup = view.findViewById(R.id.chip_group);

        createChipGroup();
        createLayout();
        return view;
    }

    public void initEqualizerFragment(Equalizer equalizer){
        this.equalizer=equalizer;
    }

    private void createChipGroup(){
        presetlist = new ArrayList<>();
        System.out.println(equalizer.getNumberOfPresets());
        for(short i=0;i<equalizer.getNumberOfPresets();i++){
            String presetName = equalizer.getPresetName(i);
            presetlist.add(presetName);
            Chip preset = (Chip) getLayoutInflater().inflate(R.layout.equalizer_chip_layout,chipGroup,false);
            preset.setText(presetName);
            preset.setId(i);
            chipGroup.addView(preset);
        }
        chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                equalizer.usePreset((short) checkedId);
                updateSeekbars();
            }
        });
    }

    private void updateSeekbars(){
        for (short i=0; i < equalizer.getNumberOfBands();i++){
            SeekBar seekBar = view.findViewById(i);
            int bandlevel = equalizer.getBandLevel(i);
            seekBar.setProgress(bandlevel - equalizer.getBandLevelRange()[0]);
        }
    }

    private void createLayout(){
        numberFreqBands = equalizer.getNumberOfBands();
        lowerEQBandLevel = equalizer.getBandLevelRange()[0];
        upperEQBandLevel = equalizer.getBandLevelRange()[1];

        Log.e("SIZE",""+ equalizer.getBandLevelRange().length);

        bandlevelstart = new short[numberFreqBands];

        for(short i=0;i<numberFreqBands;i++){
            bandlevelstart[i]=equalizer.getBandLevel(i);
        }

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
            freq.setText((equalizer.getCenterFreq(i))/1000 + "Hz");
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
            seekBar.setProgress(bandlevelstart[i] + upperEQBandLevel);
            seekBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorSecondaryLight)));
            seekBar.setThumbTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorSecondaryLight)));
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int value, boolean b) {
                    equalizer.setBandLevel(bandIndex,(short)(value+lowerEQBandLevel));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            rowLayout.addView(seekBar);
            rowLayout.addView(freq);
            linearLayout.addView(rowLayout);
        }
    }
}
