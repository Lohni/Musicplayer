package com.example.musicplayer.ui.expandedplaybackcontrol;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.musicplayer.R;
import com.example.musicplayer.database.MusicplayerApplication;
import com.example.musicplayer.database.dao.MusicplayerDataAccess;
import com.example.musicplayer.database.entity.Track;
import com.example.musicplayer.database.viewmodel.MusicplayerViewModel;
import com.example.musicplayer.inter.PlaybackControlInterface;
import com.example.musicplayer.inter.ServiceTriggerInterface;
import com.example.musicplayer.ui.views.AudioVisualizerView;
import com.example.musicplayer.utils.GeneralUtils;
import com.example.musicplayer.utils.Permissions;
import com.example.musicplayer.utils.enums.PlaybackBehaviour;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import static com.example.musicplayer.utils.enums.PlaybackBehaviour.PlaybackBehaviourState.REPEAT_LIST;
import static com.example.musicplayer.utils.enums.PlaybackBehaviour.PlaybackBehaviourState.REPEAT_SONG;
import static com.example.musicplayer.utils.enums.PlaybackBehaviour.PlaybackBehaviourState.SHUFFLE;

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
    private ImageView cover;
    private SeekBar expanded_seekbar;
    private PlaybackBehaviour.PlaybackBehaviourState playbackBehaviour;
    private MotionLayout parentContainer;

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
        ImageButton collapse = view1.findViewById(R.id.expanded_control_collapse);
        cover = view1.findViewById(R.id.expanded_cover);
        expanded_add = view1.findViewById(R.id.expanded_add);
        expanded_queue_count = view1.findViewById(R.id.expanded_queue_count);

        requireActivity().startPostponedEnterTransition();

        expanded_play.setOnClickListener(view -> epcInterface.onStateChangeListener());

        expanded_skipforward.setOnClickListener(view -> {
            AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) expanded_skipforward.getBackground();
            animatedVectorDrawable.start();
            epcInterface.onNextClickListener();
        });

        expanded_skipback.setOnClickListener(view -> epcInterface.onPreviousClickListener());

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
            playbackBehaviour = PlaybackBehaviour.getNextState(playbackBehaviour);
            epcInterface.onPlaybackBehaviourChangeListener(playbackBehaviour);
            updateBehaviourImage();
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

    private void updateBehaviourImage() {
        switch (playbackBehaviour) {
            case SHUFFLE:
                expanded_behaviourControl.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_shuffle_black_24dp, null));
                epcInterface.onPlaybackBehaviourChangeListener(SHUFFLE);
                break;
            case REPEAT_LIST:
                expanded_behaviourControl.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_repeat_black_24dp, null));
                epcInterface.onPlaybackBehaviourChangeListener(REPEAT_LIST);
                break;
            case REPEAT_SONG:
                expanded_behaviourControl.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_repeat_one_24, null));
                epcInterface.onPlaybackBehaviourChangeListener(REPEAT_SONG);
                break;
        }
    }

    public void setSongInfo(String title, String artist, int length, long id) {
        expanded_absolute_time.setText(GeneralUtils.convertTime(length));
        loadCover(id);

        if (currTrack == null || !currTrack.getTId().equals((int) id)) {
            musicplayerViewModel.getTrackById((int) id).observe(getViewLifecycleOwner(), track -> {
                this.currTrack = track;
                setIsFavouriteBackground();
            });
        }

        expanded_title.setText(title);
        expanded_artist.setText(artist);
        expanded_seekbar.setMax(length);
    }

    private void setIsFavouriteBackground() {
        if (currTrack != null) {
            int favResId = (currTrack.getTIsFavourite().equals(0))
                    ? R.drawable.ic_outline_favorite_border_24
                    : R.drawable.ic_baseline_favorite_24;

            expanded_fav.setBackground(ResourcesCompat.getDrawable(getResources(), favResId, null));
        }
    }

    private void loadCover(long song) {
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song);
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(requireContext(), trackUri);
        byte[] thumbnail = mmr.getEmbeddedPicture();
        mmr.release();
        if (thumbnail != null) {
            setCoverImage(new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length)), false);
        } else {
            setCoverImage(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_music_note_24, null), true);
        }
    }

    private void setCoverImage(Drawable coverImage, boolean custom) {
        if (custom) cover.setImageTintList(AppCompatResources.getColorStateList(requireContext(), R.color.colorPrimaryNight));
        else cover.setImageTintList(null);
        this.cover.setImageDrawable(coverImage);
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
        Integer resId = (isOnPause) ? R.drawable.ic_play_arrow_black_24dp : R.drawable.ic_pause_black_24dp;
        expanded_play.setBackground(ContextCompat.getDrawable(requireContext(), resId));
    }
}
