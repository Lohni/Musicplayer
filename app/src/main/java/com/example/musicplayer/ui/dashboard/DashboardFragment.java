package com.example.musicplayer.ui.dashboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.musicplayer.R;
import com.example.musicplayer.adapter.DashboardPlaylistAdapter;
import com.example.musicplayer.adapter.DashboardTrackAdapter;
import com.example.musicplayer.database.MusicplayerApplication;
import com.example.musicplayer.database.dao.MusicplayerDataAccess;
import com.example.musicplayer.database.dao.PlaylistDataAccess;
import com.example.musicplayer.database.dto.PlaylistDTO;
import com.example.musicplayer.database.dto.TrackDTO;
import com.example.musicplayer.database.viewmodel.MusicplayerViewModel;
import com.example.musicplayer.database.viewmodel.PlaylistViewModel;
import com.example.musicplayer.interfaces.SongInterface;
import com.example.musicplayer.ui.playlist.PlaylistInterface;
import com.example.musicplayer.ui.playlistdetail.PlaylistDetail;
import com.example.musicplayer.ui.views.DashboardListDialog;
import com.example.musicplayer.utils.enums.DashboardEnumDeserializer;
import com.example.musicplayer.utils.enums.DashboardFilterType;
import com.example.musicplayer.utils.enums.DashboardListType;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DashboardFragment extends Fragment implements PlaylistInterface {
    private MusicplayerViewModel musicplayerViewModel;
    private PlaylistViewModel playlistViewModel;

    private DashboardListType firstListType = DashboardListType.PLAYLIST;
    private DashboardListType secondlistType = DashboardListType.TRACK;
    private DashboardFilterType firstFilterType = DashboardFilterType.LAST_PLAYED;
    private DashboardFilterType secondFilterType = DashboardFilterType.TIMES_PLAYED;

    private ArrayList<?> firstAdapterList = new ArrayList<>();
    private ArrayList<?> secondAdapterList = new ArrayList<>();

    RecyclerView firstList;
    RecyclerView secondList;
    TextView first;
    TextView second;

    private SharedPreferences sharedPreferences;
    private SongInterface songInterface;

    public DashboardFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        songInterface = (SongInterface) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MusicplayerDataAccess mda = ((MusicplayerApplication) requireActivity().getApplication()).getDatabase().musicplayerDao();
        musicplayerViewModel = new ViewModelProvider(this, new MusicplayerViewModel.MusicplayerViewModelFactory(mda)).get(MusicplayerViewModel.class);
        PlaylistDataAccess pda = ((MusicplayerApplication) requireActivity().getApplication()).getDatabase().playlistDao();
        playlistViewModel = new ViewModelProvider(this, new PlaylistViewModel.PlaylistViewModelFactory(pda)).get(PlaylistViewModel.class);

        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dashboard, container, false);
        firstList = root.findViewById(R.id.dashboard_first_list);
        secondList = root.findViewById(R.id.dashboard_second_list);
        first = root.findViewById(R.id.dashboard_first_element);
        second = root.findViewById(R.id.dashboard_second_element);
        View firstEdit = root.findViewById(R.id.dashboard_first_element_edit);
        View secondEdit = root.findViewById(R.id.dashboard_second_element_edit);

        firstListType = DashboardEnumDeserializer.getDashboardListType(sharedPreferences.getInt(getString(R.string.preference_dashboard_first_list_type), 1));
        secondlistType = DashboardEnumDeserializer.getDashboardListType(sharedPreferences.getInt(getString(R.string.preference_dashboard_second_list_type), 0));

        firstFilterType = DashboardEnumDeserializer.getDashboardListFilter(sharedPreferences.getInt(getString(R.string.preference_dashboard_first_list_filter), 1));
        secondFilterType = DashboardEnumDeserializer.getDashboardListFilter(sharedPreferences.getInt(getString(R.string.preference_dashboard_second_list_filter), 1));

        LinearLayoutManager firstListManager = new LinearLayoutManager(requireContext());
        firstListManager.setOrientation(RecyclerView.HORIZONTAL);
        firstList.setHasFixedSize(true);
        firstList.setLayoutManager(firstListManager);
        firstList.setAdapter(getAdapter(firstListType, firstAdapterList, firstFilterType));

        LinearLayoutManager secondListManager = new LinearLayoutManager(requireContext());
        secondListManager.setOrientation(RecyclerView.HORIZONTAL);
        secondList.setHasFixedSize(true);
        secondList.setLayoutManager(secondListManager);
        secondList.setAdapter(getAdapter(secondlistType, secondAdapterList, secondFilterType));

        setFirstList();
        setSecondList();

        firstEdit.setOnClickListener((view) -> {
            DashboardListDialog dialog = new DashboardListDialog(requireContext(), "Configure first list", firstListType, firstFilterType);
            dialog.show();
            dialog.setOnFinishListener((res) -> {
                firstListType = dialog.getSelectedListType();
                firstFilterType = dialog.getSelectedFilterType();
                firstAdapterList.clear();
                firstList.setAdapter(getAdapter(firstListType, firstAdapterList, firstFilterType));
                setFirstList();

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(getString(R.string.preference_dashboard_first_list_type), firstListType.getTypeId());
                editor.putInt(getString(R.string.preference_dashboard_first_list_filter), firstFilterType.getFilterType());
                editor.apply();
            });
        });

        secondEdit.setOnClickListener((view) -> {
            DashboardListDialog dialog = new DashboardListDialog(requireContext(), "Configure second list", secondlistType, secondFilterType);
            dialog.show();
            dialog.setOnFinishListener((res) -> {
                secondlistType = dialog.getSelectedListType();
                secondFilterType = dialog.getSelectedFilterType();
                secondAdapterList.clear();
                secondList.setAdapter(getAdapter(secondlistType, secondAdapterList, secondFilterType));
                setSecondList();

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(getString(R.string.preference_dashboard_second_list_type), secondlistType.getTypeId());
                editor.putInt(getString(R.string.preference_dashboard_second_list_filter), secondFilterType.getFilterType());
                editor.apply();
            });
        });

        return root;
    }

    private void setFirstList() {
        first.setText(DashboardEnumDeserializer.getTitleForFilterType(firstFilterType));
        switch (firstListType) {
            case TRACK:
                useTrackViewModel(firstFilterType, (ArrayList<TrackDTO>) firstAdapterList, firstList.getAdapter());
                break;
            case PLAYLIST:
                usePlaylistViewModel(firstFilterType, (ArrayList<PlaylistDTO>) firstAdapterList, firstList.getAdapter());
                break;
        }
    }

    private void setSecondList() {
        second.setText(DashboardEnumDeserializer.getTitleForFilterType(secondFilterType));
        switch (secondlistType) {
            case TRACK:
                useTrackViewModel(secondFilterType, (ArrayList<TrackDTO>) secondAdapterList, secondList.getAdapter());
                break;
            case PLAYLIST:
                usePlaylistViewModel(secondFilterType, (ArrayList<PlaylistDTO>) secondAdapterList, secondList.getAdapter());
                break;
        }
    }

    private RecyclerView.Adapter getAdapter(DashboardListType listType, ArrayList<?> list, DashboardFilterType filterType) {
        switch (listType) {
            case ALBUM:
                return null;
            case PLAYLIST:
                return new DashboardPlaylistAdapter(requireContext(), (ArrayList<PlaylistDTO>) list, filterType, this);
            default:
                return new DashboardTrackAdapter(requireContext(), (ArrayList<TrackDTO>) list, songInterface);
        }
    }

    private synchronized void useTrackViewModel(DashboardFilterType filterType, ArrayList<TrackDTO> listToFill, RecyclerView.Adapter adapter) {
        musicplayerViewModel.getTrackListByFilter(filterType).observe(getViewLifecycleOwner(), trackDTOS -> {
            int oldSize = listToFill.size();
            listToFill.clear();
            adapter.notifyItemRangeRemoved(0, oldSize);
            listToFill.addAll(trackDTOS);
            adapter.notifyItemRangeInserted(0, listToFill.size());
        });
    }

    private synchronized void usePlaylistViewModel(DashboardFilterType filterType, ArrayList<PlaylistDTO> listToFill, RecyclerView.Adapter adapter) {
        playlistViewModel.getPlaylistByFilter(filterType).observe(getViewLifecycleOwner(), playlistDTOS -> {
            playlistViewModel.getPlaylistByFilter(filterType).removeObservers(getViewLifecycleOwner());
            listToFill.clear();
            listToFill.addAll(playlistDTOS);
            adapter.notifyItemRangeInserted(0, listToFill.size());
        });
    }

    @Override
    public void OnClickListener(Integer playlistID, View view) {
        PlaylistDetail playlistDetailFragment = new PlaylistDetail();

        Bundle bundle = new Bundle();
        bundle.putInt("PLAYLIST_ID", playlistID);
        playlistDetailFragment.setArguments(bundle);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, playlistDetailFragment, getString(R.string.fragment_playlist_detail))
                .addToBackStack("DASH").commit();
    }


}
