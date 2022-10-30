package com.example.musicplayer.ui.playbackcontrol;

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
import android.widget.TextView;

import com.example.musicplayer.R;
import com.example.musicplayer.database.MusicplayerApplication;
import com.example.musicplayer.database.dao.MusicplayerDataAccess;
import com.example.musicplayer.database.dao.PlaylistDataAccess;
import com.example.musicplayer.database.entity.Track;
import com.example.musicplayer.database.viewmodel.MusicplayerViewModel;
import com.example.musicplayer.database.viewmodel.PlaylistViewModel;
import com.example.musicplayer.utils.GeneralUtils;
import com.example.musicplayer.utils.images.ImageUtil;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;

public class PlaybackControlInfoFragment extends PlaybackControlDetailFragment {

    private View cover;
    private TextView info1, info2, info3, info4;
    private MaterialButton tags, playlists;
    private MusicplayerViewModel musicplayerViewModel;
    private PlaylistViewModel playlistViewModel;
    private Drawable customCoverImage;

    public PlaybackControlInfoFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MusicplayerDataAccess mda = ((MusicplayerApplication) requireActivity().getApplication()).getDatabase().musicplayerDao();
        musicplayerViewModel = new ViewModelProvider(this, new MusicplayerViewModel.MusicplayerViewModelFactory(mda)).get(MusicplayerViewModel.class);
        PlaylistDataAccess aod = ((MusicplayerApplication) requireActivity().getApplication()).getDatabase().playlistDao();
        playlistViewModel = new ViewModelProvider(requireActivity(), new PlaylistViewModel.PlaylistViewModelFactory(aod)).get(PlaylistViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playback_control_info, container, false);
        cover = view.findViewById(R.id.playbackcontrol_info_cover);
        info1 = view.findViewById(R.id.playbackcontrol_info_info1);
        info2 = view.findViewById(R.id.playbackcontrol_info_info2);
        info3 = view.findViewById(R.id.playbackcontrol_info_info3);
        info4 = view.findViewById(R.id.playbackcontrol_info_info4);
        tags = view.findViewById(R.id.playbackcontrol_info_tags);
        playlists = view.findViewById(R.id.playbackcontrol_info_playlists);

        customCoverImage = ResourcesCompat.getDrawable(requireContext().getResources(), R.drawable.ic_baseline_music_note_24, null);

        setCoverImage();
        setInformation();
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
                    cover.setBackground(ImageUtil.roundCorners(coverImage, requireContext().getResources()));
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

    private void setInformation() {
        if (currentTrack != null && cover != null) {
            Integer id = currentTrack.getTId();
            musicplayerViewModel.getTimesPlayedByTrackId(id).observe(getViewLifecycleOwner(), timesPlayed -> {
                musicplayerViewModel.getTimePlayedByTrackId(id).removeObservers(this);
                info1.setText(timesPlayed + " times played");
            });

            //Todo: Doesnt make much sense does it
            musicplayerViewModel.getLastPlayedByTrackId(id).observe(getViewLifecycleOwner(), lastPlayed -> {
                musicplayerViewModel.getLastPlayedByTrackId(id).removeObservers(this);
                LocalDateTime ldt = LocalDateTime.parse(lastPlayed, GeneralUtils.DB_TIMESTAMP);
                info3.setText("Last Played: " + GeneralUtils.getTimeDiffAsText(ldt));
            });

            musicplayerViewModel.getTimePlayedByTrackId(id).observe(getViewLifecycleOwner(), timePlayed -> {
                musicplayerViewModel.getTimePlayedByTrackId(id).removeObservers(this);
                info2.setText("Time played: " + GeneralUtils.convertTimeWithUnit(Integer.parseInt(timePlayed)));
            });

            musicplayerViewModel.getTagsByTrackId(id).observe(getViewLifecycleOwner(), tagList -> {
                musicplayerViewModel.getTagsByTrackId(id).removeObservers(this);
                tags.setText(tagList.size() + " tags");
            });

            playlistViewModel.getPlaylistsByTrackId(id).observe(getViewLifecycleOwner(), playlistList -> {
                playlistViewModel.getPlaylistsByTrackId(id).removeObservers(this);
                playlists.setText(playlistList.size() + " playlists");
            });

            if (currentTrack.getTCreated() != null && !currentTrack.getTCreated().isEmpty()) {
                LocalDateTime ldt = LocalDateTime.parse(currentTrack.getTCreated().replace(" ", "T"));
                info4.setText(ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
        }
    }

    @Override
    public void setCurrentTrack(Track currentTrack) {
        super.setCurrentTrack(currentTrack);
        setCoverImage();
        setInformation();
    }
}
