package com.lohni.musicplayer.ui.playbackcontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.lohni.musicplayer.R;
import com.lohni.musicplayer.interfaces.PlaybackControlInterface;
import com.lohni.musicplayer.interfaces.QueueControlInterface;
import com.lohni.musicplayer.interfaces.ServiceTriggerInterface;
import com.lohni.musicplayer.ui.queue.QueueFragment;
import com.lohni.musicplayer.ui.views.AudioVisualizerView;
import com.lohni.musicplayer.ui.views.PlaybackControlSeekbar;
import com.lohni.musicplayer.utils.images.ImageUtil;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.transition.Slide;


public class PlaybackControl extends Fragment {
    private PlaybackControlSeekbar playbackControlSeekbar;
    private TextView control_title, control_artist, queue_count;
    private ImageButton play, skip_forward;
    private View queue;
    private AudioVisualizerView audioVisualizerView;
    private ConstraintLayout parentLayout;
    private boolean seekbarUserAction = false, queueFragmentCommited = false, isPause = true, isPopupShown = false;

    private PlaybackControlInterface playbackControlInterface;
    private ServiceTriggerInterface serviceTriggerInterface;
    private QueueControlInterface songInterface;

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
            songInterface = (QueueControlInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + "must implement PlaybackControlInterface");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view1 = inflater.inflate(R.layout.fragment_playback_control, container, false);

        control_title = view1.findViewById(R.id.control_title);
        control_artist = view1.findViewById(R.id.control_artist);
        play = view1.findViewById(R.id.control_play);
        audioVisualizerView = view1.findViewById(R.id.playbackcontrol_visualizer);
        skip_forward = view1.findViewById(R.id.control_skip);
        queue = view1.findViewById(R.id.control_queue);
        control_title.setSelected(true);
        parentLayout = view1.findViewById(R.id.playbackcontrol_parent);
        queue_count = view1.findViewById(R.id.playbackcontrol_queue_count);
        queue_count.setVisibility(View.GONE);

        playbackControlSeekbar = view1.findViewById(R.id.new_seekbar);
        playbackControlSeekbar.init(R.color.colorSecondary, R.color.colorSurface);

        play.setOnClickListener(view -> playbackControlInterface.onStateChangeListener());
        skip_forward.setOnClickListener(view -> {
            AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) skip_forward.getBackground();
            animatedVectorDrawable.start();
            playbackControlInterface.onNextClickListener();
        });

        playbackControlSeekbar.setSeekbarChangeListener(new PlaybackControlSeekbar.OnSeekbarChangeListener() {
            @Override
            public void onStartTrackingTouch(PlaybackControlSeekbar seekbar) {
                seekbarUserAction = true;
            }

            @Override
            public void onStopTrackingTouch(PlaybackControlSeekbar seekbar, int progress) {
                playbackControlInterface.onProgressChangeListener(progress);
                seekbarUserAction = false;
            }
        });

        queue.setOnClickListener((queueView) -> {
            if (!queueFragmentCommited && getParentFragmentManager().findFragmentByTag(getString(R.string.fragment_queue)) == null) {
                queueFragmentCommited = true;
                QueueFragment queueFragment = new QueueFragment();

                Slide anim = new Slide();
                anim.setSlideEdge(Gravity.BOTTOM);
                anim.setDuration(200);

                queueFragment.setEnterTransition(anim);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, queueFragment, getString(R.string.fragment_queue))
                        .addToBackStack(null).commit();

                //OnClickListener gets called twice somehow - Quickfix to avoid multiple QueueFragment layers
                queue.postDelayed(() -> queueFragmentCommited = false, 500);
            }
        });

        queue.setOnLongClickListener((queueView) -> {
            if (!isPopupShown) {
                isPopupShown = true;
                PopupMenu popupMenu = new PopupMenu(requireContext(), queueView);
                popupMenu.getMenuInflater().inflate(R.menu.queue_quick_delete, popupMenu.getMenu());
                queue.setBackground(ImageUtil.getDrawableFromVectorDrawable(requireContext(), R.drawable.ic_delete_sweep_black_24dp));

                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_queue_quick_delete) {
                        songInterface.onRemoveAllSongsListener();
                        queue.setBackground(ImageUtil.getDrawableFromVectorDrawable(requireContext(), R.drawable.ic_baseline_queue_music_24));
                        popupMenu.dismiss();
                        isPopupShown = false;
                    }
                    return true;
                });

                popupMenu.setOnDismissListener(menu -> {
                    queue.setBackground(ImageUtil.getDrawableFromVectorDrawable(requireContext(), R.drawable.ic_baseline_queue_music_24));
                    popupMenu.dismiss();
                    isPopupShown = false;
                });

                popupMenu.show();
            }
            return false;
        });

        return view1;
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
        if (newCount > 0) queue_count.setVisibility(View.VISIBLE);
        else queue_count.setVisibility(View.GONE);
        queue_count.setText(String.valueOf(newCount));
    }

    public void updateSeekbar(int time) {
        if (!seekbarUserAction) {
            playbackControlSeekbar.setProgress(time);
        }
    }

    public void setAudioSessionID(int audioSessionID) {
        audioVisualizerView.initVisualizer(audioSessionID);
    }

    public void setControlButton(boolean isOnPause) {
        if (isOnPause != isPause) {
            if (!isOnPause) {
                play.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.play_to_pause_anim));
                AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) play.getBackground();
                animatedVectorDrawable.start();
            } else {
                play.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.pause_to_play_anim));
                AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) play.getBackground();
                animatedVectorDrawable.start();
            }
            isPause = isOnPause;
        }
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
