package com.lohni.musicplayer.ui.album;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lohni.musicplayer.R;
import com.lohni.musicplayer.adapter.AlbumDetailAdapter;
import com.lohni.musicplayer.core.ApplicationDataViewModel;
import com.lohni.musicplayer.database.MusicplayerApplication;
import com.lohni.musicplayer.database.dao.MusicplayerDataAccess;
import com.lohni.musicplayer.database.entity.Album;
import com.lohni.musicplayer.database.entity.Track;
import com.lohni.musicplayer.database.viewmodel.MusicplayerViewModel;
import com.lohni.musicplayer.interfaces.NavigationControlInterface;
import com.lohni.musicplayer.interfaces.PlaybackControlInterface;
import com.lohni.musicplayer.interfaces.QueueControlInterface;
import com.lohni.musicplayer.utils.enums.ListType;
import com.lohni.musicplayer.utils.enums.PlaybackBehaviourState;
import com.lohni.musicplayer.utils.images.ImageUtil;

import java.util.AbstractMap;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AlbumDetailFragment extends Fragment implements AlbumDetailAdapter.AlbumDetailAdapterListener {
    private Drawable albumCoverDrawable;

    private MusicplayerViewModel musicplayerViewModel;
    private ApplicationDataViewModel applicationDataViewModel;
    private final ArrayList<Track> albumSongs = new ArrayList<>();
    private Integer albumId;
    private Album album;

    private QueueControlInterface songInterface;
    private PlaybackControlInterface playbackControlInterface;
    private NavigationControlInterface navigationControlInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        navigationControlInterface = (NavigationControlInterface) context;
        songInterface = (QueueControlInterface) context;
        playbackControlInterface = (PlaybackControlInterface) context;
    }

    public AlbumDetailFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postponeEnterTransition();

        navigationControlInterface.isDrawerEnabledListener(false);
        navigationControlInterface.setHomeAsUpEnabled(true);
        navigationControlInterface.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        navigationControlInterface.setToolbarTitle("");

        albumId = getArguments().getInt("ALBUM_ID");
        if (getArguments().containsKey("COVER")) {
            albumCoverDrawable = ImageUtil.roundCorners((Bitmap) getArguments().get("COVER"), requireContext().getResources());
        } else {
            albumCoverDrawable = ResourcesCompat.getDrawable(requireContext().getResources(), R.drawable.ic_album_black_24dp, null);
            albumCoverDrawable.setTintList(ContextCompat.getColorStateList(requireContext(), R.color.colorOnSurfaceVariant));
        }

        MusicplayerDataAccess mda = ((MusicplayerApplication) requireActivity().getApplication()).getDatabase().musicplayerDao();
        musicplayerViewModel = new ViewModelProvider(this, new MusicplayerViewModel.MusicplayerViewModelFactory(mda)).get(MusicplayerViewModel.class);
        applicationDataViewModel = new ViewModelProvider(requireActivity()).get(ApplicationDataViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album_detail, container, false);

        TextView albumName = view.findViewById(R.id.album_detail_name);
        TextView albumSize = view.findViewById(R.id.album_detail_size);
        TextView albumArtist = view.findViewById(R.id.album_detail_artist);
        ImageView albumCover = view.findViewById(R.id.album_detail_cover);
        RecyclerView albumDetailList = view.findViewById(R.id.album_detail_list);
        LinearLayout albumDetailPlay = view.findViewById(R.id.album_detail_play);
        LinearLayout albumDetailShuffle = view.findViewById(R.id.album_detail_shuffle);

        albumDetailList.setLayoutManager(new LinearLayoutManager(requireContext()));
        albumDetailList.setHasFixedSize(true);


        applicationDataViewModel.getTrackImages().observe(getViewLifecycleOwner(), drawableHashMap -> {
            applicationDataViewModel.getTrackImages().removeObservers(getViewLifecycleOwner());
            AlbumDetailAdapter albumDetailAdapter = new AlbumDetailAdapter(requireContext(), this.albumSongs, this);
            albumDetailAdapter.setDrawableHashMap(drawableHashMap);
            albumDetailList.setAdapter(albumDetailAdapter);

            musicplayerViewModel.getAlbumByAlbumId(albumId).observe(getViewLifecycleOwner(), album -> {
                musicplayerViewModel.getAlbumByAlbumId(albumId).removeObservers(getViewLifecycleOwner());
                this.album = album;
                albumName.setText(album.getAName());
                albumSize.setText(album.getANumSongs() + " songs");
                albumArtist.setText(album.getAArtistName());

                Drawable customCoverBackground = ResourcesCompat.getDrawable(requireContext().getResources(), R.drawable.background_button_secondary, null);
                albumCover.setBackground(customCoverBackground);
                albumCover.setForeground(albumCoverDrawable);

                musicplayerViewModel.getTracksByAlbumId(albumId).observe(getViewLifecycleOwner(), tracks -> {
                    musicplayerViewModel.getTracksByAlbumId(albumId).removeObservers(getViewLifecycleOwner());
                    this.albumSongs.clear();
                    this.albumSongs.addAll(tracks);
                    albumDetailList.getAdapter().notifyItemRangeInserted(0, tracks.size());
                });
            });
        });


        albumDetailPlay.setOnClickListener((button -> {
            playbackControlInterface.onPlaybackBehaviourChangeListener(PlaybackBehaviourState.REPEAT_LIST);
            songInterface.onSongListCreatedListener(albumSongs, album, true);
        }));

        albumDetailShuffle.setOnClickListener((button) -> {
            playbackControlInterface.onPlaybackBehaviourChangeListener(PlaybackBehaviourState.SHUFFLE);
            songInterface.onSongListCreatedListener(albumSongs, album, true);
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startPostponedEnterTransition();
    }

    @Override
    public void onItemClickListener(int position) {
        playbackControlInterface.onPlaybackBehaviourChangeListener(PlaybackBehaviourState.REPEAT_LIST);
        songInterface.onSongListCreatedListener(albumSongs, album, false);
        songInterface.onSongSelectedListener(albumSongs.get(position));
    }
}