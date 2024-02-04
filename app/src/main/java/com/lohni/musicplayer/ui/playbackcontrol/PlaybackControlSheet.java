package com.lohni.musicplayer.ui.playbackcontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.textview.MaterialTextView;
import com.lohni.musicplayer.R;
import com.lohni.musicplayer.adapter.PlaybackControlViewPagerAdapter;
import com.lohni.musicplayer.core.ApplicationDataViewModel;
import com.lohni.musicplayer.database.MusicplayerApplication;
import com.lohni.musicplayer.database.dao.MusicplayerDataAccess;
import com.lohni.musicplayer.database.entity.Track;
import com.lohni.musicplayer.database.viewmodel.MusicplayerViewModel;
import com.lohni.musicplayer.ui.views.AudioVisualizerView;
import com.lohni.musicplayer.ui.views.PlaybackControlSeekbar;
import com.lohni.musicplayer.utils.GeneralUtils;
import com.lohni.musicplayer.utils.enums.PlaybackBehaviour;
import com.lohni.musicplayer.utils.images.ImageUtil;

import java.util.Objects;
import java.util.Optional;

public class PlaybackControlSheet {
    private final Context context;
    private final PlaybackControlSeekbar seekbar;
    private final SeekBar expandedSeekbar;
    private final View play, skip, cover, skipPrevious, add, favourite, behaviour, indicatorLeft, indicatorMiddle, indicatorRight;
    private final MaterialTextView title, subtitle, queue, currTime, absTime;
    private final AudioVisualizerView audioVisualizerView;

    private final PlaybackBottomSheetBehaviour bottomSheetBehavior;
    private boolean isPause = true, seekbarUserAction = false, isActive = false;
    private final ApplicationDataViewModel applicationDataViewModel;
    private MusicplayerViewModel musicplayerViewModel;
    private final Drawable customCoverImage;
    private Track currTrack;
    private PlaybackBehaviour playbackBehaviour;

    public PlaybackControlSheet(FrameLayout view, Context context) {
        this.context = context;
        this.customCoverImage = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_note_inset, null);

        applicationDataViewModel = new ViewModelProvider((FragmentActivity) context).get(ApplicationDataViewModel.class);
        MusicplayerDataAccess mda = ((MusicplayerApplication) ((FragmentActivity) context).getApplication()).getDatabase().musicplayerDao();
        musicplayerViewModel = new ViewModelProvider(((FragmentActivity) context).getViewModelStore() , new MusicplayerViewModel.MusicplayerViewModelFactory(mda)).get(MusicplayerViewModel.class);

        IntentFilter filter = new IntentFilter();
        filter.addAction(context.getString(R.string.playback_control_values));
        context.registerReceiver(broadcastReceiver, filter);

        MotionLayout motionLayout = view.findViewById(R.id.playback_control_motionlayout);
        ViewPager2 viewPager2 = view.findViewById(R.id.playback_control_viewpager);
        play = view.findViewById(R.id.playback_control_play);
        skip = view.findViewById(R.id.playback_control_skip);
        cover = view.findViewById(R.id.playback_control_cover);
        title = view.findViewById(R.id.playback_control_title);
        subtitle = view.findViewById(R.id.playback_control_subtitle);
        seekbar = view.findViewById(R.id.playback_control_collapsed_seekbar);
        skipPrevious = view.findViewById(R.id.playback_control_skip_back);
        add = view.findViewById(R.id.playback_control_add);
        favourite = view.findViewById(R.id.playback_control_favourite);
        behaviour = view.findViewById(R.id.playback_control_behaviour);
        expandedSeekbar = view.findViewById(R.id.playback_control_expanded_seekbar);
        currTime = view.findViewById(R.id.playback_control_current_time);
        absTime = view.findViewById(R.id.playback_control_abs_time);
        queue = view.findViewById(R.id.playback_control_queue_count);
        audioVisualizerView = view.findViewById(R.id.playback_control_audio_vis);
        indicatorLeft = view.findViewById(R.id.playback_control_indicator_left);
        indicatorMiddle = view.findViewById(R.id.playback_control_indicator_middle);
        indicatorRight = view.findViewById(R.id.playback_control_indicator_right);

        seekbar.init(R.color.colorPrimary, R.color.colorSurfaceVariant);
        queue.setText("0/0");

        bottomSheetBehavior = (PlaybackBottomSheetBehaviour) BottomSheetBehavior.from(view);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setPeekHeight((int) ImageUtil.convertDpToPixel(70f, context.getResources()));

        play.setOnClickListener((view1) -> {
            String action = (isPause) ? context.getString(R.string.playback_action_play) : context.getString(R.string.playback_action_pause);
            context.sendBroadcast(new Intent(action));
        });
        skip.setOnClickListener((view1) -> context.sendBroadcast(new Intent(context.getString(R.string.playback_action_next))));
        skipPrevious.setOnClickListener((view1) -> context.sendBroadcast(new Intent(context.getString(R.string.playback_action_previous))));
        behaviour.setOnClickListener((view1) -> {
            playbackBehaviour = PlaybackBehaviour.Companion.getNextState(playbackBehaviour);
            context.sendBroadcast(new Intent(context.getString(R.string.playback_set_behaviour)).putExtra("BEHAVIOUR_STATE", PlaybackBehaviour.Companion.getStateAsInteger(playbackBehaviour)));
            updateBehaviourDrawable();
        });
        favourite.setOnClickListener((view1) -> {
            if (currTrack != null) {
                currTrack.setTIsFavourite(currTrack.getTIsFavourite() == null || currTrack.getTIsFavourite().equals(0) ? 1 : 0);
                musicplayerViewModel.updateTrack(currTrack);
                setFavouriteBackground();
            }
        });
        add.setOnClickListener((view1) -> {
        });
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    viewPager2.setAdapter(new PlaybackControlViewPagerAdapter((FragmentActivity) context));
                    viewPager2.setCurrentItem(1, false);
                    audioVisualizerView.setEnableVisualizer(true);
                    view.setClickable(true);
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    audioVisualizerView.setEnableVisualizer(false);
                    view.setClickable(false);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                motionLayout.setProgress(slideOffset);
            }
        });
        seekbar.setSeekbarChangeListener(new PlaybackControlSeekbar.OnSeekbarChangeListener() {
            @Override
            public void onStartTrackingTouch(PlaybackControlSeekbar seekbar) {
                seekbarUserAction = true;
            }

            @Override
            public void onStopTrackingTouch(PlaybackControlSeekbar seekbar, int progress) {
                seekbarUserAction = false;
                context.sendBroadcast(new Intent(context.getString(R.string.playback_set_progress)).putExtra("PROGRESS", progress));
            }
        });
        expandedSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekbarUserAction = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekbarUserAction = false;
                context.sendBroadcast(new Intent(context.getString(R.string.playback_set_progress)).putExtra("PROGRESS", seekBar.getProgress()));
            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                if (position == 0) {
                    indicatorLeft.setLayoutParams(ImageUtil.getLayoutParamsWithSize(8f, indicatorLeft.getLayoutParams(), context.getResources()));
                    indicatorMiddle.setLayoutParams(ImageUtil.getLayoutParamsWithSize(5f, indicatorMiddle.getLayoutParams(), context.getResources()));
                    indicatorRight.setLayoutParams(ImageUtil.getLayoutParamsWithSize(5f, indicatorRight.getLayoutParams(), context.getResources()));
                    indicatorLeft.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorPrimary));
                    indicatorMiddle.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorSurfaceVariant));
                    indicatorRight.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorSurfaceVariant));
                } else if (position == 1) {
                    indicatorLeft.setLayoutParams(ImageUtil.getLayoutParamsWithSize(5f, indicatorLeft.getLayoutParams(), context.getResources()));
                    indicatorMiddle.setLayoutParams(ImageUtil.getLayoutParamsWithSize(8f, indicatorMiddle.getLayoutParams(), context.getResources()));
                    indicatorRight.setLayoutParams(ImageUtil.getLayoutParamsWithSize(5f, indicatorRight.getLayoutParams(), context.getResources()));
                    indicatorLeft.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorSurfaceVariant));
                    indicatorMiddle.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorPrimary));
                    indicatorRight.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorSurfaceVariant));
                } else if (position == 2) {
                    indicatorLeft.setLayoutParams(ImageUtil.getLayoutParamsWithSize(5f, indicatorLeft.getLayoutParams(), context.getResources()));
                    indicatorMiddle.setLayoutParams(ImageUtil.getLayoutParamsWithSize(5f, indicatorMiddle.getLayoutParams(), context.getResources()));
                    indicatorRight.setLayoutParams(ImageUtil.getLayoutParamsWithSize(8f, indicatorRight.getLayoutParams(), context.getResources()));
                    indicatorLeft.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorSurfaceVariant));
                    indicatorMiddle.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorSurfaceVariant));
                    indicatorRight.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorPrimary));
                }
            }
        });
    }

    public void updateSeekbar(int progress) {
        if (!seekbarUserAction) {
            seekbar.setProgress(progress);
            expandedSeekbar.setProgress(progress);
            currTime.setText(GeneralUtils.convertTime(progress));
        }
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    private void setControlButton(boolean isOnPause) {
        if (isOnPause != isPause) {
            int drawable = (!isOnPause) ? R.drawable.play_to_pause_anim : R.drawable.pause_to_play_anim;
            play.setBackground(ContextCompat.getDrawable(context, drawable));
            AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) play.getBackground();
            animatedVectorDrawable.start();
            isPause = isOnPause;
        }
    }

    private void setFavouriteBackground() {
        int favResId = (currTrack == null || currTrack.getTIsFavourite().equals(0))
                ? R.drawable.ic_round_favorite_border_24
                : R.drawable.ic_round_favorite_24;
        favourite.setBackground(ResourcesCompat.getDrawable(context.getResources(), favResId, null));
    }

    private void updateBehaviourDrawable() {
        behaviour.setBackground(ResourcesCompat.getDrawable(context.getResources(), PlaybackBehaviour.Companion.getDrawableResourceIdForState(playbackBehaviour), null));
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int id = intent.getIntExtra("ID", -1);
            if (Objects.equals(intent.getAction(), context.getString(R.string.playback_control_values)) && id >= 0 && isActive) {
                Bundle bundle = intent.getExtras();
                playbackBehaviour = PlaybackBehaviour.Companion.getStateFromInteger(bundle.getInt("BEHAVIOUR_STATE"));
                updateBehaviourDrawable();
                setControlButton(bundle.getBoolean("ISONPAUSE"));

                int queue_size = bundle.getInt("QUEUE_SIZE");
                int queue_index = bundle.getInt("QUEUE_INDEX") + 1;
                queue.setText(String.format("%d/%d", queue_index, queue_size));
                audioVisualizerView.initVisualizer(bundle.getInt("SESSION_ID"));

                musicplayerViewModel.observeOnce(musicplayerViewModel.getTrackById(id), (FragmentActivity) context, track -> {
                    currTrack = track;
                    title.setText(currTrack.getTTitle());
                    subtitle.setText(currTrack.getTArtist());
                    seekbar.setMax(currTrack.getTDuration());
                    expandedSeekbar.setMax(currTrack.getTDuration());
                    absTime.setText(GeneralUtils.convertTime(currTrack.getTDuration()));

                    setFavouriteBackground();

                    Optional<Drawable> drawable = applicationDataViewModel.getImageForTrack(currTrack.getTId());
                    if (drawable.isPresent()) {
                        RoundedBitmapDrawable bmp = RoundedBitmapDrawableFactory.create(context.getResources(), ((RoundedBitmapDrawable) drawable.get()).getBitmap());
                        bmp.setCornerRadius(0f);
                        cover.setBackground(bmp);
                        cover.setPadding(0, 0, 0, 0);
                    } else {
                        cover.setBackground(customCoverImage);
                    }
                });
            }
        }
    };
}
