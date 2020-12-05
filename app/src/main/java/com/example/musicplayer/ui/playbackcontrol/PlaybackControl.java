package com.example.musicplayer.ui.playbackcontrol;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.musicplayer.R;
import com.example.musicplayer.ui.views.AudioVisualizerView;
import com.example.musicplayer.ui.views.PlaybackControlSeekbar;


public class PlaybackControl extends Fragment {
    private static final int PERMISSION_REQUEST_CODE = 0x03;
    private PlaybackControlSeekbar playbackControlSeekbar;
    private TextView control_title, control_artist;
    private ImageButton play, skip_forward;
    private View view;
    private AudioVisualizerView audioVisualizerView;

    private int newProgress,audioSessionID;
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

        control_title = view.findViewById(R.id.control_title);
        control_artist = view.findViewById(R.id.control_artist);
        play = view.findViewById(R.id.control_play);
        audioVisualizerView = view.findViewById(R.id.playbackcontrol_visualizer);
        skip_forward = view.findViewById(R.id.control_skip);

        control_title.setSelected(true);

        playbackControlSeekbar = view.findViewById(R.id.new_seekbar);
        playbackControlSeekbar.init(R.color.colorSecondaryLight,R.color.colorPrimaryNight);

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

        playbackControlSeekbar.setSeekbarChangeListener(new PlaybackControlSeekbar.OnSeekbarChangeListener() {
            @Override
            public void onProgressChanged(PlaybackControlSeekbar seekbar, int progress, boolean fromUser) {
                newProgress = progress;
            }

            @Override
            public void onStartTrackingTouch(PlaybackControlSeekbar seekbar) {
                seekbarUserAction=true;
            }

            @Override
            public void onStopTrackingTouch(PlaybackControlSeekbar seekbar) {
                playbackControlInterface.OnSeekbarChangeListener(newProgress);
                seekbarUserAction=false;
            }
        });

        control_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioVisualizerView.release();
                playbackControlInterface.OnExpandListener(playbackControlSeekbar,control_title);
            }
        });
        return view;
    }

    public void setSongInfo(String title, String artist,int length){
        control_title.setText(title);
        control_artist.setText(artist);

        playbackControlSeekbar.setMax(length);
    }

    public void updateSeekbar(int time){
        if (!seekbarUserAction){
            playbackControlSeekbar.setProgress(time);
        }
    }

    public void setAudioSessionID(int audioSessionID){
        this.audioSessionID=audioSessionID;
        permission();
        audioVisualizerView.initVisualizer(audioSessionID);
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

    private void permission(){
        //if (Build.VERSION.SDK_INT >= 23) {
        //Check whether your app has access to the READ permission//
        if (checkPermission()) {
            //If your app has access to the device’s storage, then print the following message to Android Studio’s Logcat//
            Log.e("permission", "Permission already granted.");
        } else {
            //If your app doesn’t have permission to access external storage, then call requestPermission//
            requestPermission();
        }
        //}
    }

    private boolean checkPermission() {
        //Check for READ_EXTERNAL_STORAGE access, using ContextCompat.checkSelfPermission()//
        int result = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.RECORD_AUDIO);
        //If the app does have this permission, then return true//
        //If the app doesn’t have this permission, then return false//
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
    }

}
