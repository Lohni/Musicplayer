package com.example.musicplayer.ui.expandedplaybackcontrol;

import android.content.ContentUris;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;

import com.example.musicplayer.R;
import com.example.musicplayer.database.entity.Track;
import com.example.musicplayer.utils.images.ImageTransformUtil;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

public class PlaybackControlCoverFragment extends PlaybackControlDetailFragment{

    private View cover;
    private Drawable customCoverImage;

    public PlaybackControlCoverFragment(){}

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
            Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currentTrack.getTId());
            byte[] thumbnail = null;
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            try {
                mmr.setDataSource(requireContext(), trackUri);
                thumbnail = mmr.getEmbeddedPicture();
            } catch (IllegalArgumentException e) {
                System.out.println("MediaMetadataRetriever IllegalArgument");
            } finally {
                try {
                    mmr.release();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (thumbnail != null) {
                    Bitmap coverImage = BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length);
                    cover.setBackground(ImageTransformUtil.roundCorners(coverImage, requireContext().getResources()));
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
    }

    @Override
    public void setCurrentTrack(Track currentTrack) {
        super.setCurrentTrack(currentTrack);
        setCoverImage();
    }
}
