package com.lohni.musicplayer.ui.playbackcontrol;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;

import com.lohni.musicplayer.R;
import com.lohni.musicplayer.core.ApplicationDataViewModel;
import com.lohni.musicplayer.utils.images.ImageUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;

public class PlaybackControlCoverFragment extends PlaybackControlDetailFragment {
    private View cover;
    private Drawable customCoverImage;
    private ApplicationDataViewModel applicationDataViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applicationDataViewModel = new ViewModelProvider(requireActivity()).get(ApplicationDataViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playback_control_cover, container, false);
        cover = view.findViewById(R.id.playbackcontrol_cover);
        customCoverImage = ResourcesCompat.getDrawable(requireContext().getResources(), R.drawable.ic_baseline_music_note_24, null);

        setCoverImage();
        return view;
    }

    private void setCoverImage() {
        if (currentTrack != null && cover != null) {
            Drawable drawable = applicationDataViewModel.getImageForTrack(currentTrack.getTId());

            if (drawable != null) {
                cover.setBackground(ImageUtil.roundCorners(ImageUtil.getBitmapFromDrawable(requireContext(), drawable), requireContext().getResources()));
                cover.setForeground(null);
                Animation fadeIn = new AlphaAnimation(0, 1);
                fadeIn.setInterpolator(new DecelerateInterpolator());
                fadeIn.setDuration(350);
                cover.setAnimation(fadeIn);
            } else {
                cover.setBackground(customCoverImage);
                cover.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.colorOnSecondaryContainer));
            }
        }
    }

    @Override
    void onTrackChange() {
        setCoverImage();
    }
}
