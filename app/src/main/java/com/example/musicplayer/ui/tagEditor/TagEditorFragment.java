package com.example.musicplayer.ui.tagEditor;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.musicplayer.R;
import com.example.musicplayer.adapter.TagEditorAdapter;
import com.example.musicplayer.database.MusicplayerApplication;
import com.example.musicplayer.database.dao.MusicplayerDataAccess;
import com.example.musicplayer.database.dto.TrackDTO;
import com.example.musicplayer.database.entity.Track;
import com.example.musicplayer.database.viewmodel.MusicplayerViewModel;
import com.example.musicplayer.utils.NavigationControlInterface;

import java.util.ArrayList;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class TagEditorFragment extends Fragment implements TagEditorInterface {

    private RecyclerView tagList;
    private EditText search;

    private TagEditorAdapter adapter;
    private ArrayList<Track> trackList;

    private TagEditorInterface tagEditorInterface;
    private NavigationControlInterface navigationControlInterface;

    private MusicplayerViewModel musicplayerViewModel;

    public TagEditorFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MusicplayerDataAccess mda = ((MusicplayerApplication) requireActivity().getApplication()).getDatabase().musicplayerDao();
        musicplayerViewModel = new ViewModelProvider(this, new MusicplayerViewModel.MusicplayerViewModelFactory(mda)).get(MusicplayerViewModel.class);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            navigationControlInterface = (NavigationControlInterface) context;
            tagEditorInterface = this;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + "must implement TagEditorInterface");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tag_editor, container, false);
        tagList = view.findViewById(R.id.tagEditor_songlist);
        search = view.findViewById(R.id.tagEditor_search);

        navigationControlInterface.setToolbarTitle("Tag-Editor");
        navigationControlInterface.setHomeAsUpEnabled(false);
        navigationControlInterface.isDrawerEnabledListener(true);

        trackList = new ArrayList<>();
        adapter = new TagEditorAdapter(trackList, requireContext(), tagEditorInterface);
        tagList.setAdapter(adapter);
        tagList.setHasFixedSize(true);
        tagList.setLayoutManager(new LinearLayoutManager(requireContext()));

        musicplayerViewModel.getAllTracks().observe(getViewLifecycleOwner(), tracks -> {
            this.trackList.clear();
            this.trackList.addAll(tracks.stream().map(TrackDTO::getTrack).collect(Collectors.toList()));

            adapter.notifyItemRangeInserted(0, tracks.size());
        });

        return view;
    }

    @Override
    public void onTrackSelectedListener(Track musicResolver) {
        TagEditorDetailFragment tagEditorDetailFragment = new TagEditorDetailFragment();

        Bundle bundle = new Bundle();
        bundle.putInt("TRACK_ID", musicResolver.getTId());
        tagEditorDetailFragment.setArguments(bundle);
        getParentFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, tagEditorDetailFragment).addToBackStack(null).commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.trackList.clear();
    }
}