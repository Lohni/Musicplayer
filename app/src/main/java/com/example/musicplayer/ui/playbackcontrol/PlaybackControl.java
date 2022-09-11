package com.example.musicplayer.ui.playbackcontrol;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.musicplayer.R;
import com.example.musicplayer.interfaces.PlaybackControlInterface;
import com.example.musicplayer.interfaces.ServiceTriggerInterface;
import com.example.musicplayer.ui.views.AudioVisualizerView;
import com.example.musicplayer.ui.views.PlaybackControlSeekbar;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


public class PlaybackControl extends Fragment {
    private static final int PERMISSION_REQUEST_CODE = 0x03;
    private PlaybackControlSeekbar playbackControlSeekbar;
    private TextView control_title, control_artist, queue_count;
    private ImageButton play, skip_forward, queue;
    private View view;
    private AudioVisualizerView audioVisualizerView;
    private ConstraintLayout parentLayout;

    private int newProgress, queueCount = 0;
    private boolean seekbarUserAction = false;

    private PlaybackControlInterface playbackControlInterface;
    private ServiceTriggerInterface serviceTriggerInterface;

    public PlaybackControl() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter intentFilter = new IntentFilter(getResources().getString(R.string.playback_control_values));
        requireActivity().registerReceiver(receiver, intentFilter);
        serviceTriggerInterface.triggerCurrentDataBroadcast();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            playbackControlInterface = (PlaybackControlInterface) context;
            serviceTriggerInterface = (ServiceTriggerInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + "must implement PlaybackControlInterface");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
        playbackControlSeekbar.init(R.color.colorSecondary, R.color.colorSurface);

        play.setOnClickListener(view -> playbackControlInterface.onStateChangeListener());
        skip_forward.setOnClickListener(view -> {
            AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) skip_forward.getBackground();
            animatedVectorDrawable.start();
            playbackControlInterface.onNextClickListener();
        });

        playbackControlSeekbar.setSeekbarChangeListener(new PlaybackControlSeekbar.OnSeekbarChangeListener() {
            @Override
            public void onProgressChanged(PlaybackControlSeekbar seekbar, int progress, boolean fromUser) {
                newProgress = progress;
            }

            @Override
            public void onStartTrackingTouch(PlaybackControlSeekbar seekbar) {
                seekbarUserAction = true;
            }

            @Override
            public void onStopTrackingTouch(PlaybackControlSeekbar seekbar) {
                playbackControlInterface.onProgressChangeListener(newProgress);
                seekbarUserAction = false;
            }
        });

        return view;
    }

    public View getParentView() {
        return parentLayout;
    }

    public View getTitleView() {
        return control_title;
    }

    @Override
    public void onDetach() {
        audioVisualizerView.release();
        requireActivity().unregisterReceiver(receiver);
        super.onDetach();
    }

    @Override
    public void onPause() {
        audioVisualizerView.setenableVisualizer(false);
        super.onPause();
    }

    public void setSongInfo(String title, String artist, int length) {
        control_title.setText(title);
        control_artist.setText(artist);

        playbackControlSeekbar.setMax(length);
    }

    public void updateQueueCount(int newCount) {
        if (queueCount != newCount) {
            if (newCount > 0) queue_count.setVisibility(View.VISIBLE);
            else queue_count.setVisibility(View.GONE);
            animateCount(queueCount, newCount);
            queueCount = newCount;
        }
    }

    private void animateCount(int old, int newCount) {
        int dur = 500 / Math.abs(old - newCount);
        if (old < newCount) {
            new Thread(() -> {
                int i = old;
                while (i <= newCount) {
                    try {
                        Thread.sleep(dur);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int finalI = i;
                    queue_count.post(() -> queue_count.setText(String.valueOf(finalI)));
                    i++;
                }
                if (newCount == 0) {
                    queue_count.setText("");
                }
            }).start();
        } else {
            new Thread(() -> {
                int i = old;
                while (i >= newCount) {
                    try {
                        Thread.sleep(dur);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int finalI = i;
                    queue_count.post(() -> queue_count.setText(String.valueOf(finalI)));
                    i--;
                }
                if (newCount == 0) {
                    queue_count.setText("");
                }
            }).start();
        }
    }

    public void updateSeekbar(int time) {
        if (!seekbarUserAction) {
            playbackControlSeekbar.setProgress(time);
        }
    }

    public void setAudioSessionID(int audioSessionID) {
        permission();
        audioVisualizerView.initVisualizer(audioSessionID);
    }

    public void setControlButton(boolean isOnPause) {
        if (!isOnPause) {
            play.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.play_to_pause_anim));
            AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) play.getBackground();
            animatedVectorDrawable.start();
        } else {
            play.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.pause_to_play_anim));
            AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) play.getBackground();
            animatedVectorDrawable.start();
        }
    }

    private void permission() {
        if (checkPermission()) {
            Log.e("permission", "Permission already granted.");
        } else {
            requestPermission();
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            setSongInfo(bundle.getString("TITLE"),
                    bundle.getString("ARTIST"),
                    bundle.getInt("DURATION"));

            setAudioSessionID(bundle.getInt("SESSION_ID"));
            setControlButton(bundle.getBoolean("ISONPAUSE"));

            updateQueueCount(bundle.getInt("QUEUE_SIZE"));
        }
    };
}
