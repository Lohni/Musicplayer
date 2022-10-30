package com.example.musicplayer.ui.album;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.musicplayer.R;
import com.example.musicplayer.adapter.AlbumDetailAdapter;
import com.example.musicplayer.database.MusicplayerApplication;
import com.example.musicplayer.database.dao.MusicplayerDataAccess;
import com.example.musicplayer.database.entity.Track;
import com.example.musicplayer.database.viewmodel.MusicplayerViewModel;
import com.example.musicplayer.interfaces.PlaybackControlInterface;
import com.example.musicplayer.interfaces.SongInterface;
import com.example.musicplayer.interfaces.NavigationControlInterface;
import com.example.musicplayer.utils.enums.DashboardListType;
import com.example.musicplayer.utils.enums.PlaybackBehaviour;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AlbumDetailFragment extends Fragment implements AlbumDetailAdapter.AlbumDetailAdapterListener {

    private TextView albumName, albumSize, albumArtist;
    private ImageView albumCover;
    private RecyclerView albumDetailList;
    private ImageButton albumDetailPlay, albumDetailShuffle;

    private MusicplayerViewModel musicplayerViewModel;
    private ArrayList<Track> albumSongs = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
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
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postponeEnterTransition();

        navigationControlInterface.isDrawerEnabledListener(false);
        navigationControlInterface.setHomeAsUpEnabled(true);
        navigationControlInterface.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);

        albumId = getArguments().getInt("ALBUM_ID");

        MusicplayerDataAccess mda = ((MusicplayerApplication) requireActivity().getApplication()).getDatabase().musicplayerDao();
        musicplayerViewModel = new ViewModelProvider(this, new MusicplayerViewModel.MusicplayerViewModelFactory(mda)).get(MusicplayerViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_album_detail, container, false);

        albumName = view.findViewById(R.id.album_detail_name);
        albumSize = view.findViewById(R.id.album_detail_size);
        albumCover = view.findViewById(R.id.album_detail_cover);
        albumArtist = view.findViewById(R.id.album_detail_artist);
        albumDetailList = view.findViewById(R.id.album_detail_list);
        albumDetailPlay = view.findViewById(R.id.album_detail_play);
        albumDetailShuffle = view.findViewById(R.id.album_detail_shuffle);

        albumDetailList.setLayoutManager(linearLayoutManager = new LinearLayoutManager(requireContext()));
        albumDetailList.setHasFixedSize(true);

        musicplayerViewModel.getAlbumByAlbumId(albumId).observe(getViewLifecycleOwner(), album -> {

            albumName.setText(album.getAName());
            albumSize.setText(album.getANumSongs() + "songs");
            albumArtist.setText(album.getAArtistName());

            Glide.with(this)
                    .load(album.getAArtUri())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(new RequestOptions().error(R.drawable.ic_album_black_24dp).format(DecodeFormat.PREFER_RGB_565))
                    .override(albumCover.getWidth(), albumCover.getHeight())
                    .into(albumCover);

            musicplayerViewModel.getTracksByAlbumId(albumId).observe(getViewLifecycleOwner(), tracks -> {
                this.albumSongs.clear();
                this.albumSongs.addAll(tracks);
                albumDetailList.setAdapter(new AlbumDetailAdapter(requireContext(), this.albumSongs, albumCover.getDrawingCache(), this));

                startPostponedEnterTransition();
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
    }

    @Override
    public void onItemClickListener(int position) {
        songInterface.onSongListCreatedListener(albumSongs, DashboardListType.ALBUM);
        songInterface.onSongSelectedListener(albumSongs.get(position));
    }
}