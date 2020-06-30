package com.example.musicplayer.ui.playbackcontrol;

import android.content.Context;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.musicplayer.R;


public class PlaybackControl extends Fragment {

    private SeekBar seekBar;
    private TextView control_current, control_absolute, control_title, control_artist;
    private ImageButton play, skip_forward;
    private View view;

    private int newProgress;
    private boolean seekbarUserAction=false;

    private PlaybackControlInterface playbackControlInterface;

    public PlaybackControl() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            playbackControlInterface = (PlaybackControlInterface) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + "must implement PlaybackControlInterface");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_playback_control, container, false);

        control_current = view.findViewById(R.id.control_current_time);
        control_absolute = view.findViewById(R.id.control_absolut_time);
        control_title = view.findViewById(R.id.control_title);
        control_artist = view.findViewById(R.id.control_artist);
        seekBar = view.findViewById(R.id.seekbar);
        play = view.findViewById(R.id.control_play);
        skip_forward = view.findViewById(R.id.control_skip);

        control_title.setSelected(true);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playbackControlInterface.OnStateChangeListener();
            }
        });
        skip_forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) skip_forward.getBackground();
                animatedVectorDrawable.start();
                playbackControlInterface.OnSkipPressedListener();
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                newProgress=i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekbarUserAction=true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                playbackControlInterface.OnSeekbarChangeListener(newProgress);
                seekbarUserAction=false;
            }
        });

        control_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playbackControlInterface.OnExpandListener(seekBar,control_title);
            }
        });
        return view;
    }

    public void setSongInfo(String title, String artist,int length){
        control_absolute.setText(convertTime(length));
        control_title.setText(title);
        control_artist.setText(artist);
        seekBar.setMax(length);
    }

    public void updateSeekbar(int time){
        if (!seekbarUserAction)seekBar.setProgress(time);
        control_current.setText(convertTime(time));
    }

    private String convertTime(int duration){
        float d = (float)duration /(1000*60);
        int min = (int)d;
        float seconds = (d - min)*60;
        int sec = (int)seconds;
        String minute=min+"", second=sec+"";
        if(min<10) minute="0"+minute;
        if(sec<10) second="0"+second;
        return minute + ":" + second;
    }

    public void setControlButton(boolean isOnPause){
        if(!isOnPause){
            play.setBackground(ContextCompat.getDrawable(requireContext(),R.drawable.play_to_pause_anim));
            AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) play.getBackground();
            animatedVectorDrawable.start();
            //play.setBackground(ContextCompat.getDrawable(requireContext(),R.drawable.ic_pause_black_24dp));
        } else {
            play.setBackground(ContextCompat.getDrawable(requireContext(),R.drawable.pause_to_play_anim));
            AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) play.getBackground();
            animatedVectorDrawable.start();
        }
    }



}
