package com.example.musicplayer.ui.songlist;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.musicplayer.R;
import com.example.musicplayer.adapter.SongListAdapter;
import com.example.musicplayer.database.MusicplayerApplication;
import com.example.musicplayer.database.dao.MusicplayerDataAccess;
import com.example.musicplayer.database.entity.Track;
import com.example.musicplayer.database.viewmodel.MusicplayerViewModel;
import com.example.musicplayer.interfaces.PlaybackControlInterface;
import com.example.musicplayer.interfaces.SongInterface;
import com.example.musicplayer.ui.views.SideIndex;
import com.example.musicplayer.utils.GeneralUtils;
import com.example.musicplayer.utils.NavigationControlInterface;
import com.example.musicplayer.utils.enums.PlaybackBehaviour;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SongList extends Fragment implements SongListInterface {
    private RecyclerView listView;
    private View view;
    private SongListAdapter songListAdapter;
    private MaterialButton shuffle;

    private final ArrayList<Track> songList = new ArrayList<>();
    private NavigationControlInterface navigationControlInterface;
    private SongInterface songInterface;
    private PlaybackControlInterface playbackControlInterface;
    private SongListInterface songListInterface;
    private TextView shuffle_size;
    private SideIndex sideIndex;

    private MusicplayerViewModel musicplayerViewModel;

    private boolean isSonglistSet = false;

    public SongList() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            navigationControlInterface = (NavigationControlInterface) context;
            songInterface = (SongInterface) context;
            songListInterface = this;
            playbackControlInterface = (PlaybackControlInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + "must implement Interface");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MusicplayerDataAccess mda = ((MusicplayerApplication) requireActivity().getApplication()).getDatabase().musicplayerDao();
        musicplayerViewModel = new ViewModelProvider(this, new MusicplayerViewModel.MusicplayerViewModelFactory(mda)).get(MusicplayerViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_song_list, container, false);
        navigationControlInterface.isDrawerEnabledListener(true);
        navigationControlInterface.setHomeAsUpEnabled(false);
        navigationControlInterface.setToolbarTitle("Tracklist");
        listView = view.findViewById(R.id.songList);
        shuffle = view.findViewById(R.id.songlist_shuffle);
        shuffle_size = view.findViewById(R.id.songlist_size);

        LinearLayout linearLayout = view.findViewById(R.id.side_index);
        FrameLayout indexZoomHolder = view.findViewById(R.id.songlist_indexzoom_holder);
        TextView indexZoom = view.findViewById(R.id.songlist_indexzoom);
        LinearLayoutManager listViewManager = new LinearLayoutManager(requireContext());
        sideIndex = new SideIndex(requireActivity(), linearLayout, indexZoomHolder, indexZoom, listViewManager);

        listView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_animation_fall_down));
        listView.setHasFixedSize(true);
        listView.setLayoutManager(listViewManager);

        musicplayerViewModel.getAllTracks().observe(getViewLifecycleOwner(), tracks -> {
            this.songList.clear();
            this.songList.addAll(tracks);

            sideIndex.setIndexList(songList).displayIndex();

            songListAdapter = new SongListAdapter(requireContext(), songList, songListInterface);
            listView.setAdapter(songListAdapter);

            int time = tracks.stream().map(Track::getTDuration).reduce(0, Integer::sum);
            shuffle_size.setText(tracks.size() + " songs - " + GeneralUtils.convertTime(time));
        });

        shuffle.setOnClickListener(view -> {
            if (!isSonglistSet) {
                songInterface.onSongListCreatedListener(songList);
                isSonglistSet = true;
            }

            playbackControlInterface.onPlaybackBehaviourChangeListener(PlaybackBehaviour.PlaybackBehaviourState.SHUFFLE);
            playbackControlInterface.onNextClickListener();
        });

        return view;
    }

    @Override
    public void OnSongSelectedListener(int index) {
        if (!isSonglistSet) {
            songInterface.onSongListCreatedListener(songList);
            isSonglistSet = true;
        }

        songInterface.onSongSelectedListener(songList.get(index));
    }
}
