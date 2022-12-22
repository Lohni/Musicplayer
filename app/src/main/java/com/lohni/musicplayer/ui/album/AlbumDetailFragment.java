package com.lohni.musicplayer.ui.album;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lohni.musicplayer.R;
import com.lohni.musicplayer.adapter.AlbumDetailAdapter;
import com.lohni.musicplayer.database.MusicplayerApplication;
import com.lohni.musicplayer.database.dao.MusicplayerDataAccess;
import com.lohni.musicplayer.database.entity.Track;
import com.lohni.musicplayer.database.viewmodel.MusicplayerViewModel;
import com.lohni.musicplayer.interfaces.NavigationControlInterface;
import com.lohni.musicplayer.interfaces.PlaybackControlInterface;
import com.lohni.musicplayer.interfaces.SongInterface;
import com.lohni.musicplayer.utils.enums.DashboardListType;
import com.lohni.musicplayer.utils.enums.PlaybackBehaviour;
import com.lohni.musicplayer.utils.images.ImageUtil;

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
    private final ArrayList<Track> albumSongs = new ArrayList<>();
    private Integer albumId;

    private SongInterface songInterface;
    private PlaybackControlInterface playbackControlInterface;
    private NavigationControlInterface navigationControlInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        navigationControlInterface = (NavigationControlInterface) context;
        songInterface = (SongInterface) context;
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

        albumId = getArguments().getInt("ALBUM_ID");
        if (getArguments().containsKey("COVER")) {
            albumCoverDrawable = ImageUtil.roundCorners((Bitmap) getArguments().get("COVER"), requireContext().getResources());
        } else {
            albumCoverDrawable = ResourcesCompat.getDrawable(requireContext().getResources(), R.drawable.ic_album_black_24dp, null);
            albumCoverDrawable.setTintList(ContextCompat.getColorStateList(requireContext(), R.color.colorOnSurfaceVariant));
        }

        MusicplayerDataAccess mda = ((MusicplayerApplication) requireActivity().getApplication()).getDatabase().musicplayerDao();
        musicplayerViewModel = new ViewModelProvider(this, new MusicplayerViewModel.MusicplayerViewModelFactory(mda)).get(MusicplayerViewModel.class);
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
        albumDetailList.setAdapter(new AlbumDetailAdapter(requireContext(), this.albumSongs, this));


        musicplayerViewModel.getAlbumByAlbumId(albumId).observe(getViewLifecycleOwner(), album -> {
            musicplayerViewModel.getAlbumByAlbumId(albumId).removeObservers(getViewLifecycleOwner());
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
                ((AlbumDetailAdapter) albumDetailList.getAdapter()).getAllBackgroundImages(tracks, albumDetailList);
            });
        });

        albumDetailPlay.setOnClickListener((button -> {
            songInterface.onSongListCreatedListener(albumSongs, DashboardListType.ALBUM);
            playbackControlInterface.onPlaybackBehaviourChangeListener(PlaybackBehaviour.PlaybackBehaviourState.REPEAT_LIST);
        }));

        albumDetailShuffle.setOnClickListener((button) -> {
            songInterface.onSongListCreatedListener(albumSongs, DashboardListType.ALBUM);
            playbackControlInterface.onPlaybackBehaviourChangeListener(PlaybackBehaviour.PlaybackBehaviourState.SHUFFLE);
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
        songInterface.onSongListCreatedListener(albumSongs, DashboardListType.ALBUM);
        songInterface.onSongSelectedListener(albumSongs.get(position));
    }
}