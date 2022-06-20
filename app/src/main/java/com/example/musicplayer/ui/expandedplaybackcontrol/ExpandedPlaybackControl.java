package com.example.musicplayer.ui.expandedplaybackcontrol;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.musicplayer.R;
import com.example.musicplayer.inter.PlaybackControlInterface;
import com.example.musicplayer.inter.ServiceTriggerInterface;
import com.example.musicplayer.ui.views.AudioVisualizerView;
import com.example.musicplayer.utils.Permissions;
import com.example.musicplayer.utils.enums.PlaybackBehaviour;

public class ExpandedPlaybackControl extends Fragment {
    private TextView expanded_title,expanded_artist,expanded_currtime,expanded_absolute_time, expanded_queue_count;
    private ImageButton expanded_play,expanded_skipforward,expanded_skipback,collapse, expanded_fav, expanded_behaviourControl, expanded_more, expanded_add;
    private AudioVisualizerView audioVisualizerView;
    private ImageView cover;
    private SeekBar expanded_seekbar;
    private PlaybackBehaviour.PlaybackBehaviourState playbackBehaviour;
    private View view;
    private MotionLayout parentContainer;

    private PlaybackControlInterface epcInterface;
    private ServiceTriggerInterface serviceTriggerInterface;

    private int newProgress, queue_size = 0, queue_index = 0;
    private boolean seekbarUserAction=false;

    public ExpandedPlaybackControl() {
        // Required empty public constructor
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
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
        postponeEnterTransition();
        serviceTriggerInterface.triggerCurrentDataBroadcast();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            epcInterface = (PlaybackControlInterface) context;
            serviceTriggerInterface = (ServiceTriggerInterface) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + "must implement PlaybackControlInterface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (audioVisualizerView != null)audioVisualizerView.release();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_expanded_playback_control, container, false);
        parentContainer = view.findViewById(R.id.parentContainer);
        expanded_title = view.findViewById(R.id.expanded_control_title);
        expanded_artist = view.findViewById(R.id.expanded_control_artist);
        expanded_play = view.findViewById(R.id.expanded_control_play);
        expanded_skipforward = view.findViewById(R.id.expanded_control_skipforward);
        expanded_skipback = view.findViewById(R.id.expanded_control_skipback);
        expanded_fav = view.findViewById(R.id.expanded_favourite);
        expanded_behaviourControl = view.findViewById(R.id.expanded_control_behaviour);
        expanded_currtime = view.findViewById(R.id.expanded_current_time);
        expanded_absolute_time = view.findViewById(R.id.expanded_absolute_time);
        expanded_seekbar = view.findViewById(R.id.expanded_seekbar);
        expanded_more = view.findViewById(R.id.expanded_more);
        audioVisualizerView = view.findViewById(R.id.audioView);
        collapse = view.findViewById(R.id.expanded_control_collapse);
        cover = view.findViewById(R.id.expanded_cover);
        expanded_add = view.findViewById(R.id.expanded_add);
        expanded_queue_count = view.findViewById(R.id.expanded_queue_count);
        
        requireActivity().startPostponedEnterTransition();

        expanded_play.setOnClickListener(view -> epcInterface.onStateChangeListener());

        expanded_skipforward.setOnClickListener(view -> {
            AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) expanded_skipforward.getBackground();
            animatedVectorDrawable.start();
            epcInterface.onNextClickListener();
        });

        expanded_skipback.setOnClickListener(view -> epcInterface.onPreviousClickListener());

        collapse.setOnClickListener(view -> {parentContainer.setInteractionEnabled(true); parentContainer.transitionToStart();});

        expanded_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
                epcInterface.onProgressChangeListener(newProgress);
                seekbarUserAction=false;
            }
        });

        expanded_behaviourControl.setOnClickListener((imageview -> {
            playbackBehaviour = PlaybackBehaviour.getNextState(playbackBehaviour);
            epcInterface.onPlaybackBehaviourChangeListener(playbackBehaviour);
            updateBehaviourImage();
        }));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startPostponedEnterTransition();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (audioVisualizerView != null)audioVisualizerView.setenableVisualizer(false);
    }

    public void updateSeekbar(int time){
        if (!seekbarUserAction)expanded_seekbar.setProgress(time);
        expanded_currtime.setText(convertTime(time));
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

    private void updateQueueState(){
        expanded_queue_count.setText(String.format("%d/%d", queue_index, queue_size));
    }

    private void updateBehaviourImage(){
        switch (playbackBehaviour){
            case SHUFFLE:
                expanded_behaviourControl.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_shuffle_black_24dp,null));
                break;
            case REPEAT_LIST:
                expanded_behaviourControl.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_repeat_black_24dp,null));
                break;
            case REPEAT_SONG:
                expanded_behaviourControl.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_repeat_one_24,null));
                break;
            case PLAY_ORDER:
                //Todo: Create Play Order
                expanded_behaviourControl.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_shuffle_black_24dp,null));
                break;
        }
    }

    public void setSongInfo(String title, String artist,int length, long id){
        expanded_absolute_time.setText(convertTime(length));
        expanded_title.setText(title);
        expanded_artist.setText(artist);
        expanded_seekbar.setMax(length);
        loadCover(id);
    }

    public void setAudioSessionID(int audioSessionID){
        Permissions.permission(requireActivity(), this, Manifest.permission.RECORD_AUDIO);
        audioVisualizerView.initVisualizer(audioSessionID);
    }

    public void setControlButton(boolean isOnPause){
        if(!isOnPause){
            expanded_play.setBackground(ContextCompat.getDrawable(requireContext(),R.drawable.ic_pause_black_24dp));
        } else {
            expanded_play.setBackground(ContextCompat.getDrawable(requireContext(),R.drawable.ic_play_arrow_black_24dp));
        }
    }

    private void loadCover(long song){
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,song);
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(requireContext(),trackUri);
        byte [] thumbnail = mmr.getEmbeddedPicture();
        mmr.release();
        if (thumbnail != null){
            setCoverImage(new BitmapDrawable(getResources(),BitmapFactory.decodeByteArray(thumbnail,0,thumbnail.length)),false);
        } else {
            setCoverImage(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_music_note_24,null),true);
        }
    }

    private void setCoverImage(Drawable coverImage, boolean custom){
        if (custom)cover.setImageTintList(AppCompatResources.getColorStateList(requireContext(),R.color.colorPrimaryNight));
        this.cover.setImageDrawable(coverImage);
    }

    public void setBehaviourState(PlaybackBehaviour.PlaybackBehaviourState playbackBehaviour){
         this.playbackBehaviour = playbackBehaviour;
         updateBehaviourImage();
    }

    public void setQueueSize(int size){
        queue_size = size;
        updateQueueState();
    }

    public void setQueueIndex(int index){
        queue_index = index;
        updateQueueState();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            setSongInfo(bundle.getString("TITLE"),
                    bundle.getString("ARTIST"),
                    bundle.getInt("DURATION"),
                    bundle.getLong("ID"));

            setAudioSessionID(bundle.getInt("SESSION_ID"));
            setControlButton(bundle.getBoolean("ISONPAUSE"));
            updateSeekbar(bundle.getInt("CURRENT_POSITION"));
            setQueueSize(bundle.getInt("QUEUE_SIZE"));
            setQueueIndex(bundle.getInt("QUEUE_INDEX"));
            setBehaviourState(PlaybackBehaviour.getStateFromInteger(bundle.getInt("BEHAVIOUR_STATE")));
        }
    };
}
