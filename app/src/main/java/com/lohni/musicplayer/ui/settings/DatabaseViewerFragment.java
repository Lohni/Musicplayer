package com.lohni.musicplayer.ui.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.lohni.musicplayer.R;
import com.lohni.musicplayer.adapter.DatabaseViewerAdapter;
import com.lohni.musicplayer.database.MusicplayerApplication;
import com.lohni.musicplayer.database.dao.MusicplayerDataAccess;
import com.lohni.musicplayer.database.dto.ItemPlayedDTO;
import com.lohni.musicplayer.database.viewmodel.MusicplayerViewModel;
import com.lohni.musicplayer.interfaces.NavigationControlInterface;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DatabaseViewerFragment extends Fragment {
    private MusicplayerViewModel musicplayerViewModel;
    private ArrayList<ItemPlayedDTO> itemPlayedList = new ArrayList<>();
    private NavigationControlInterface navigationControlInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            navigationControlInterface = (NavigationControlInterface) context;
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MusicplayerDataAccess mda = ((MusicplayerApplication) requireActivity().getApplication()).getDatabase().musicplayerDao();
        musicplayerViewModel = new ViewModelProvider(this, new MusicplayerViewModel.MusicplayerViewModelFactory(mda)).get(MusicplayerViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_database_viewer, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.database_viewer_list);
        MaterialAutoCompleteTextView autoCompleteTextView = view.findViewById(R.id.database_viewer_autocompletetextview);

        autoCompleteTextView.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, DatabaseViewerAdapter.PlayedFilter.values()));
        autoCompleteTextView.setText(DatabaseViewerAdapter.PlayedFilter.ALL.toString(), false);

        recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_animation_fall_down));
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(new DatabaseViewerAdapter(requireContext(), itemPlayedList));

        musicplayerViewModel.getItemPlayedOrdered().observe(getViewLifecycleOwner(), itemPlayedList -> {
            this.itemPlayedList.clear();
            this.itemPlayedList.addAll(itemPlayedList);
            recyclerView.getAdapter().notifyDataSetChanged();
        });

        autoCompleteTextView.setOnItemClickListener((parent, view1, position, id) -> {
            DatabaseViewerAdapter.PlayedFilter playedFilter = DatabaseViewerAdapter.PlayedFilter.getPlayedFilterByOrdinal(position);
            ((DatabaseViewerAdapter) recyclerView.getAdapter()).getFilter().filter(playedFilter.toString());
        });

        navigationControlInterface.setToolbarTitle("History");

        return view;
    }
}
