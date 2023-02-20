package com.lohni.musicplayer.ui.playbackcontrol;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lohni.musicplayer.R;
import com.lohni.musicplayer.adapter.PlaybackControlViewPagerAdapter;
import com.lohni.musicplayer.database.MusicplayerApplication;
import com.lohni.musicplayer.database.dao.MusicplayerDataAccess;
import com.lohni.musicplayer.database.entity.Track;
import com.lohni.musicplayer.database.viewmodel.MusicplayerViewModel;
import com.lohni.musicplayer.interfaces.PlaybackControlInterface;
import com.lohni.musicplayer.interfaces.ServiceTriggerInterface;
import com.lohni.musicplayer.ui.views.AudioVisualizerView;
import com.lohni.musicplayer.utils.GeneralUtils;
import com.lohni.musicplayer.utils.Permissions;
import com.lohni.musicplayer.utils.enums.PlaybackBehaviour;
import com.lohni.musicplayer.utils.enums.PlaybackBehaviourState;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

public class ExpandedPlaybackControl extends Fragment {
    private TextView expanded_title, expanded_artist, expanded_currtime, expanded_absolute_time, expanded_queue_count;
    private ImageButton expanded_play;
    private ImageButton expanded_skipforward;
    private ImageButton expanded_skipback;
    private ImageButton expanded_fav;
    private ImageButton expanded_behaviourControl;
    private ImageButton expanded_more;
    private ImageButton expanded_add;
    private AudioVisualizerView audioVisualizerView;
    private SeekBar expanded_seekbar;
    private PlaybackBehaviourState playbackBehaviour;
    private MotionLayout parentContainer;
    private ViewPager2 viewPager;
    private PlaybackControlViewPagerAdapter viewPagerAdapter;
    private View indicatorLeft, indicatorMiddle, indicatorRight;

    private PlaybackControlInterface epcInterface;
    private ServiceTriggerInterface serviceTriggerInterface;

    private MusicplayerViewModel musicplayerViewModel;
    private Track currTrack;

    private boolean seekbarUserAction = false, isAudiSessionIdSet = false;

    public ExpandedPlaybackControl() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                parentContainer.setInteractionEnabled(true);
                parentContainer.transitionToStart();
            }
        };

        MusicplayerDataAccess mda = ((MusicplayerApplication) requireActivity().getApplication()).getDatabase().musicplayerDao();
        musicplayerViewModel = new ViewModelProvider(this, new MusicplayerViewModel.MusicplayerViewModelFactory(mda)).get(MusicplayerViewModel.class);

        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
        postponeEnterTransition();

        IntentFilter intentFilter = new IntentFilter(getResources().getString(R.string.playback_control_values));
        requireActivity().registerReceiver(receiver, intentFilter);
        serviceTriggerInterface.triggerCurrentDataBroadcast();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            epcInterface = (PlaybackControlInterface) context;
            serviceTriggerInterface = (ServiceTriggerInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + "must implement PlaybackControlInterface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (audioVisualizerView != null) audioVisualizerView.release();
        requireActivity().unregisterReceiver(receiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view1 = inflater.inflate(R.layout.fragment_expanded_playback_control, container, false);
        parentContainer = requireActivity().findViewById(R.id.parentContainer);
        expanded_title = view1.findViewById(R.id.expanded_control_title);
        expanded_artist = view1.findViewById(R.id.expanded_control_artist);
        expanded_play = view1.findViewById(R.id.expanded_control_play);
        expanded_skipforward = view1.findViewById(R.id.expanded_control_skipforward);
        expanded_skipback = view1.findViewById(R.id.expanded_control_skipback);
        expanded_fav = view1.findViewById(R.id.expanded_favourite);
        expanded_behaviourControl = view1.findViewById(R.id.expanded_control_behaviour);
        expanded_currtime = view1.findViewById(R.id.expanded_current_time);
        expanded_absolute_time = view1.findViewById(R.id.expanded_absolute_time);
        expanded_seekbar = view1.findViewById(R.id.expanded_seekbar);
        expanded_more = view1.findViewById(R.id.expanded_more);
        audioVisualizerView = view1.findViewById(R.id.audioView);
        viewPager = view1.findViewById(R.id.expanded_control_viewpager);
        expanded_add = view1.findViewById(R.id.expanded_add);
        expanded_queue_count = view1.findViewById(R.id.expanded_queue_count);
        indicatorLeft = view1.findViewById(R.id.playbackcontrol_viewpager_indicator_left);
        indicatorMiddle = view1.findViewById(R.id.playbackcontrol_viewpager_indicator_middle);
        indicatorRight = view1.findViewById(R.id.playbackcontrol_viewpager_indicator_right);
        ImageButton collapse = view1.findViewById(R.id.expanded_control_collapse);

        expanded_queue_count.setText("0/0");

        viewPagerAdapter = new PlaybackControlViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setCurrentItem(1);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                if (position == 0) {
                    indicatorLeft.setLayoutParams(getLayoutParamsWithSize(8, indicatorLeft.getLayoutParams()));
                    indicatorMiddle.setLayoutParams(getLayoutParamsWithSize(5, indicatorMiddle.getLayoutParams()));
                    indicatorRight.setLayoutParams(getLayoutParamsWithSize(5, indicatorRight.getLayoutParams()));
                    indicatorLeft.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.colorPrimary));
                    indicatorMiddle.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.colorSurfaceVariant));
                    indicatorRight.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.colorSurfaceVariant));
                } else if (position == 1) {
                    indicatorLeft.setLayoutParams(getLayoutParamsWithSize(5, indicatorLeft.getLayoutParams()));
                    indicatorMiddle.setLayoutParams(getLayoutParamsWithSize(8, indicatorMiddle.getLayoutParams()));
                    indicatorRight.setLayoutParams(getLayoutParamsWithSize(5, indicatorRight.getLayoutParams()));
                    indicatorLeft.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.colorSurfaceVariant));
                    indicatorMiddle.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.colorPrimary));
                    indicatorRight.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.colorSurfaceVariant));
                } else if (position == 2) {
                    indicatorLeft.setLayoutParams(getLayoutParamsWithSize(5, indicatorLeft.getLayoutParams()));
                    indicatorMiddle.setLayoutParams(getLayoutParamsWithSize(5, indicatorMiddle.getLayoutParams()));
                    indicatorRight.setLayoutParams(getLayoutParamsWithSize(8, indicatorRight.getLayoutParams()));
                    indicatorLeft.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.colorSurfaceVariant));
                    indicatorMiddle.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.colorSurfaceVariant));
                    indicatorRight.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.colorPrimary));
                }
            }
        });

        requireActivity().startPostponedEnterTransition();
        parentContainer.setInteractionEnabled(false);

        expanded_play.setOnClickListener(view -> epcInterface.onStateChangeListener());

        expanded_skipforward.setOnClickListener(view -> {
            if (currTrack != null) {
                AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) expanded_skipforward.getBackground();
                animatedVectorDrawable.start();
                epcInterface.onNextClickListener();
            }
        });

        expanded_skipback.setOnClickListener(view -> {
            if (currTrack != null) {
                epcInterface.onPreviousClickListener();
            }
        });

        collapse.setOnClickListener(view -> {
            parentContainer.setInteractionEnabled(true);
            parentContainer.transitionToStart();
        });

        expanded_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekbarUserAction = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                epcInterface.onProgressChangeListener(seekBar.getProgress());
                seekbarUserAction = false;
            }
        });

        expanded_behaviourControl.setOnClickListener((imageview -> {
            if (currTrack != null) {
                playbackBehaviour = PlaybackBehaviour.getNextState(playbackBehaviour);
                epcInterface.onPlaybackBehaviourChangeListener(playbackBehaviour);
                updateBehaviourDrawable();
            }
        }));

        expanded_fav.setOnClickListener((imageView -> {
            if (currTrack != null) {
                int newIsFavourite = (currTrack.getTIsFavourite().equals(0)) ? 1 : 0;
                currTrack.setTIsFavourite(newIsFavourite);
                setIsFavouriteBackground();
                musicplayerViewModel.updateTrack(currTrack);
            }
        }));

        return view1;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startPostponedEnterTransition();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (audioVisualizerView != null) audioVisualizerView.setenableVisualizer(false);
    }

    private void updateBehaviourDrawable() {
        switch (playbackBehaviour) {
            case SHUFFLE:
                expanded_behaviourControl.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_round_shuffle_24, null));
                break;
            case REPEAT_LIST:
                expanded_behaviourControl.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_round_repeat_24, null));
                break;
            case REPEAT_SONG:
                expanded_behaviourControl.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_round_repeat_one_24, null));
                break;
        }
    }

    public void setSongInfo(String title, String artist, int length, long id) {
        expanded_absolute_time.setText(GeneralUtils.convertTime(length));

        if (currTrack == null || !currTrack.getTId().equals((int) id)) {
            musicplayerViewModel.getTrackById((int) id).observe(getViewLifecycleOwner(), track -> {
                this.currTrack = track;
                setIsFavouriteBackground();
                viewPagerAdapter.setCurrentTrack(track);
            });
        }

        expanded_title.setText(title);
        expanded_artist.setText(artist);
        expanded_seekbar.setMax(length);
    }

    private void setIsFavouriteBackground() {
        if (currTrack != null) {
            int favResId = (currTrack.getTIsFavourite().equals(0))
                    ? R.drawable.ic_round_favorite_border_24
                    : R.drawable.ic_round_favorite_24;

            expanded_fav.setBackground(ResourcesCompat.getDrawable(getResources(), favResId, null));
        }
    }

    public void updateSeekbar(int time) {
        if (!seekbarUserAction) expanded_seekbar.setProgress(time);
        expanded_currtime.setText(GeneralUtils.convertTime(time));
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            setSongInfo(bundle.getString("TITLE"),
                    bundle.getString("ARTIST"),
                    bundle.getInt("DURATION"),
                    bundle.getLong("ID"));


            playbackBehaviour = PlaybackBehaviour.getStateFromInteger(bundle.getInt("BEHAVIOUR_STATE"));
            updateBehaviourDrawable();
            setAudioSessionID(bundle.getInt("SESSION_ID"));
            setControlButton(bundle.getBoolean("ISONPAUSE"));

            int queue_size = bundle.getInt("QUEUE_SIZE");
            int queue_index = bundle.getInt("QUEUE_INDEX") + 1;
            expanded_queue_count.setText(String.format("%d/%d", queue_index, queue_size));
        }
    };

    private void setAudioSessionID(int audioSessionID) {
        if (!isAudiSessionIdSet) {
            Permissions.permission(requireActivity(), this, Manifest.permission.RECORD_AUDIO);
            audioVisualizerView.initVisualizer(audioSessionID);
            isAudiSessionIdSet = true;
        }
    }

    public void setControlButton(boolean isOnPause) {
        Integer resId = (isOnPause) ? R.drawable.ic_round_play_arrow_24 : R.drawable.ic_round_pause_24;
        expanded_play.setBackground(ContextCompat.getDrawable(requireContext(), resId));
    }

    private int convertDPtoPixel(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, requireContext().getResources().getDisplayMetrics());
    }

    private ViewGroup.LayoutParams getLayoutParamsWithSize(int dp, ViewGroup.LayoutParams layoutParams) {
        layoutParams.width = convertDPtoPixel(dp);
        layoutParams.height = convertDPtoPixel(dp);
        return layoutParams;
    }
}
