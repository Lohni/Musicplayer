package com.lohni.musicplayer.ui.playlist;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lohni.musicplayer.R;
import com.lohni.musicplayer.adapter.TrackSelectionAdapter;
import com.lohni.musicplayer.database.MusicplayerApplication;
import com.lohni.musicplayer.database.dao.MusicplayerDataAccess;
import com.lohni.musicplayer.database.dao.PlaylistDataAccess;
import com.lohni.musicplayer.database.dto.TrackDTO;
import com.lohni.musicplayer.database.entity.PlaylistItem;
import com.lohni.musicplayer.database.entity.Track;
import com.lohni.musicplayer.database.viewmodel.MusicplayerViewModel;
import com.lohni.musicplayer.database.viewmodel.PlaylistViewModel;
import com.lohni.musicplayer.ui.views.CustomDividerItemDecoration;
import com.lohni.musicplayer.ui.views.SideIndex;
import com.lohni.musicplayer.interfaces.NavigationControlInterface;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class PlaylistDetailAdd extends Fragment {
    private RecyclerView selection;
    private EditText search;
    private ArrayList<Track> trackList = new ArrayList<>();
    private TrackSelectionAdapter mAdapter;
    private ExtendedFloatingActionButton confirm;
    private NavigationControlInterface navigationControlInterface;
    private String title = "";

    private PlaylistViewModel playlistViewModel;
    private MusicplayerViewModel musicplayerViewModel;
    private Integer playlistId, playlistCurrentSize;

    private SideIndex sideIndex;

    public PlaylistDetailAdd() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            navigationControlInterface = (NavigationControlInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + "must implement interface");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            title = getArguments().getString("PLAYLIST_NAME");
            playlistId = getArguments().getInt("PLAYLIST_ID");
            playlistCurrentSize = getArguments().getInt("PLAYLIST_SIZE");
        }

        PlaylistDataAccess pda = ((MusicplayerApplication) requireActivity().getApplication()).getDatabase().playlistDao();
        MusicplayerDataAccess mda = ((MusicplayerApplication) requireActivity().getApplication()).getDatabase().musicplayerDao();

        playlistViewModel = new ViewModelProvider(requireActivity(), new PlaylistViewModel.PlaylistViewModelFactory(pda)).get(PlaylistViewModel.class);
        musicplayerViewModel = new ViewModelProvider(requireActivity(), new MusicplayerViewModel.MusicplayerViewModelFactory(mda)).get(MusicplayerViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_playlist_detail_add, container, false);
        selection = view.findViewById(R.id.selection_list);
        search = view.findViewById(R.id.playlist_add_search);
        confirm = view.findViewById(R.id.playlist_detail_add_confirm);
        TextView indexZoom = view.findViewById(R.id.playlist_detail_add_indexzoom);
        FrameLayout indexZoomHolder = view.findViewById(R.id.playlist_add_indexzoom_holder);
        indexZoomHolder.setVisibility(View.GONE);
        confirm.setVisibility(View.INVISIBLE);

        navigationControlInterface.isDrawerEnabledListener(false);
        navigationControlInterface.setHomeAsUpEnabled(true);
        navigationControlInterface.setHomeAsUpIndicator(R.drawable.ic_clear_black_24dp);
        navigationControlInterface.setToolbarTitle(title);

        selection.setHasFixedSize(true);

        CustomDividerItemDecoration dividerItemDecoration = new CustomDividerItemDecoration(requireContext(), R.drawable.recyclerview_divider);
        selection.addItemDecoration(dividerItemDecoration);
        selection.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_animation_fall_down));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        selection.setLayoutManager(linearLayoutManager);

        LinearLayout linearLayout = view.findViewById(R.id.playlist_detail_add_side_index);
        sideIndex = new SideIndex(requireActivity(), linearLayout, indexZoomHolder, indexZoom, linearLayoutManager);

        musicplayerViewModel.getTrackAlphabetical().observe(getViewLifecycleOwner(), tracklist -> {
            this.trackList.clear();
            this.trackList.addAll(tracklist.stream().map(TrackDTO::getTrack).collect(Collectors.toList()));

            Collections.sort(this.trackList, (a, b) -> a.getTTitle().compareToIgnoreCase(b.getTTitle()));
            mAdapter = new TrackSelectionAdapter(requireContext(), trackList);
            mAdapter.setOnTrackSelectedListener((position) -> {
                int selectedCount = mAdapter.getSelectedCount();
                if (selectedCount > 0) {
                    if (selectedCount == 1) {
                        confirm.setVisibility(View.VISIBLE);
                        confirm.setText("ADD " + selectedCount + " SONG");
                    } else confirm.setText("ADD " + selectedCount + " SONGS");
                } else confirm.setVisibility(View.INVISIBLE);
            });
            selection.setAdapter(mAdapter);
            sideIndex.setIndexList(trackList).displayIndex();
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        confirm.setOnClickListener(view1 -> {
            ArrayList<Track> selected = mAdapter.getSelected();
            if (!selected.isEmpty()) {
                ArrayList<PlaylistItem> toInsert = new ArrayList<>();
                int customOrdinal = playlistCurrentSize;
                for (Track track : selected) {
                    PlaylistItem playlistItem = new PlaylistItem();
                    playlistItem.setPiCustomOrdinal(customOrdinal++);
                    playlistItem.setPiTId(track.getTId());
                    playlistItem.setPiPId(playlistId);
                    toInsert.add(playlistItem);
                }

                playlistViewModel.insertPlaylistItems(toInsert);
            }

            navigationControlInterface.onBackPressedListener();
        });

        return view;
    }
}
