package com.example.musicplayer.ui.playbackcontrol;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.transition.Fade;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.musicplayer.R;
import com.example.musicplayer.transition.AlbumDetailTransition;
import com.example.musicplayer.ui.expandedplaybackcontrol.ExpandedPlaybackControl;
import com.example.musicplayer.ui.views.AudioVisualizerView;
import com.example.musicplayer.ui.views.PlaybackControlSeekbar;


public class PlaybackControl extends Fragment {
    private static final int PERMISSION_REQUEST_CODE = 0x03;
    private PlaybackControlSeekbar playbackControlSeekbar;
    private TextView control_title, control_artist, queue_count;
    private ImageButton play, skip_forward, queue;
    private View view;
    private AudioVisualizerView audioVisualizerView;
    private ConstraintLayout parentLayout;

    private int newProgress,audioSessionID, queueCount = 0;
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
        queue = view.findViewById(R.id.control_queue);
        control_title.setSelected(true);
        parentLayout = view.findViewById(R.id.playbackcontrol_parent);
        queue_count = view.findViewById(R.id.playbackcontrol_queue_count);
        queue_count.setVisibility(View.GONE);

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

        return view;
    }

    public View getParentView(){
        return parentLayout;
    }

    public View getTitleView(){
        return control_title;
    }

    @Override
    public void onDetach() {
        audioVisualizerView.release();
        super.onDetach();
    }

    @Override
    public void onPause() {
        audioVisualizerView.setenableVisualizer(false);
        super.onPause();
    }

    public void setSongInfo(String title, String artist, int length){
        control_title.setText(title);
        control_artist.setText(artist);

        playbackControlSeekbar.setMax(length);
    }

    public void updateQueueCount(int newCount){
        if (newCount>0)queue_count.setVisibility(View.VISIBLE);
        else queue_count.setVisibility(View.GONE);
        animateCount(queueCount, newCount);
        queueCount = newCount;
    }

    private void animateCount(int old, int newCount){
        if (old == newCount){
            return;
        }
        int dur = 500/Math.abs(old-newCount);
        if (old < newCount){
            new Thread(new Runnable() {
                public void run() {
                    int i = old;
                    while (i < newCount) {
                        try {
                            Thread.sleep(dur);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        int finalI = i;
                        queue_count.post(new Runnable() {
                            public void run() {
                                queue_count.setText(String.valueOf(finalI));
                            }
                        });
                        i++;
                    }
                    if (newCount == 0){
                        queue_count.setText("");
                    }
                }
            }).start();
        } else {
            new Thread(new Runnable() {
                public void run() {
                    int i = old;
                    while (i > newCount) {
                        try {
                            Thread.sleep(dur);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        int finalI = i;
                        queue_count.post(new Runnable() {
                            public void run() {
                                queue_count.setText(String.valueOf(finalI));
                            }
                        });
                        i--;
                    }
                    if (newCount == 0){
                        queue_count.setText("");
                    }
                }
            }).start();
        }
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

    public int[] getQueueScreenLocation(){
        int[] loc = new int[2];
        if (queue != null)queue.getLocationOnScreen(loc);
        return loc;
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
