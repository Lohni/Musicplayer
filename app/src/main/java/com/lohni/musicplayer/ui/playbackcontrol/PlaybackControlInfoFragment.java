package com.lohni.musicplayer.ui.playbackcontrol;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.lohni.musicplayer.R;
import com.lohni.musicplayer.core.ApplicationDataViewModel;
import com.lohni.musicplayer.database.MusicplayerApplication;
import com.lohni.musicplayer.database.dao.PlaylistDataAccess;
import com.lohni.musicplayer.database.viewmodel.PlaylistViewModel;
import com.lohni.musicplayer.utils.GeneralUtils;
import com.lohni.musicplayer.utils.images.ImageUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class PlaybackControlInfoFragment extends PlaybackControlDetailFragment {

    private View cover;
    private TextView info1, info2, info3, info4;
    private MaterialButton tags, playlists;
    private PlaylistViewModel playlistViewModel;
    private ApplicationDataViewModel applicationDataViewModel;
    private Drawable customCoverImage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PlaylistDataAccess aod = ((MusicplayerApplication) requireActivity().getApplication()).getDatabase().playlistDao();
        playlistViewModel = new ViewModelProvider(requireActivity(), new PlaylistViewModel.PlaylistViewModelFactory(aod)).get(PlaylistViewModel.class);
        applicationDataViewModel = new ViewModelProvider(requireActivity()).get(ApplicationDataViewModel.class);
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
            Optional<Drawable> drawable = applicationDataViewModel.getImageForTrack(currentTrack.getTId());

            if (drawable.isPresent()) {
                cover.setBackground(ImageUtil.roundCorners(ImageUtil.getBitmapFromDrawable(requireContext(), drawable.get()), requireContext().getResources()));
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
    public void onTrackChange() {
        setCoverImage();
        setInformation();
    }
}
