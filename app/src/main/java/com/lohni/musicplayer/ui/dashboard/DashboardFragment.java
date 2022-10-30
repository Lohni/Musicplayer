package com.lohni.musicplayer.ui.dashboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lohni.musicplayer.R;
import com.lohni.musicplayer.adapter.DashboardPlaylistAdapter;
import com.lohni.musicplayer.adapter.DashboardTrackAdapter;
import com.lohni.musicplayer.database.MusicplayerApplication;
import com.lohni.musicplayer.database.dao.MusicplayerDataAccess;
import com.lohni.musicplayer.database.dao.PlaylistDataAccess;
import com.lohni.musicplayer.database.dto.PlaylistDTO;
import com.lohni.musicplayer.database.dto.TrackDTO;
import com.lohni.musicplayer.database.viewmodel.MusicplayerViewModel;
import com.lohni.musicplayer.database.viewmodel.PlaylistViewModel;
import com.lohni.musicplayer.interfaces.SongInterface;
import com.lohni.musicplayer.interfaces.PlaylistInterface;
import com.lohni.musicplayer.ui.playlist.PlaylistDetail;
import com.lohni.musicplayer.ui.songlist.SongList;
import com.lohni.musicplayer.ui.views.DashboardListDialog;
import com.lohni.musicplayer.ui.views.XYGraphView;
import com.lohni.musicplayer.utils.converter.DashboardEnumDeserializer;
import com.lohni.musicplayer.utils.enums.ListFilterType;
import com.lohni.musicplayer.utils.enums.DashboardListType;

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
    private ListFilterType firstFilterType = ListFilterType.LAST_PLAYED;
    private ListFilterType secondFilterType = ListFilterType.TIMES_PLAYED;

    private int firstListSize, secondListSize;

    private ArrayList<?> firstAdapterList = new ArrayList<>();
    private ArrayList<?> secondAdapterList = new ArrayList<>();

    private RecyclerView firstList;
    private RecyclerView secondList;
    private TextView first;
    private TextView second;
    private TextView statTitle;
    private XYGraphView stat;

    private SharedPreferences sharedPreferences;
    private SongInterface songInterface;

    private LinearLayoutManager firstListManager;
    private LinearLayoutManager secondListManager;

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
        statTitle = root.findViewById(R.id.dashboard_stat_title);
        View firstEdit = root.findViewById(R.id.dashboard_first_element_edit);
        View secondEdit = root.findViewById(R.id.dashboard_second_element_edit);
        View firstGoto = root.findViewById(R.id.dashboard_first_element_goto);
        View secondGoto = root.findViewById(R.id.dashboard_second_element_goto);
        stat = root.findViewById(R.id.dashboard_statistics);

        firstListType = DashboardEnumDeserializer.getDashboardListType(sharedPreferences.getInt(getString(R.string.preference_dashboard_first_list_type), 1));
        secondlistType = DashboardEnumDeserializer.getDashboardListType(sharedPreferences.getInt(getString(R.string.preference_dashboard_second_list_type), 0));

        firstFilterType = DashboardEnumDeserializer.getListFilterTypeByInt(sharedPreferences.getInt(getString(R.string.preference_dashboard_first_list_filter), 1));
        secondFilterType = DashboardEnumDeserializer.getListFilterTypeByInt(sharedPreferences.getInt(getString(R.string.preference_dashboard_second_list_filter), 1));

        firstListSize = sharedPreferences.getInt(getString(R.string.preference_dashboard_first_list_size), 10);
        secondListSize = sharedPreferences.getInt(getString(R.string.preference_dashboard_second_list_size), 10);

        firstListManager = new LinearLayoutManager(requireContext());
        firstListManager.setOrientation(RecyclerView.HORIZONTAL);
        firstList.setHasFixedSize(true);
        firstList.setLayoutManager(firstListManager);
        firstList.setAdapter(getAdapter(firstListType, firstAdapterList, firstFilterType));

        secondListManager = new LinearLayoutManager(requireContext());
        secondListManager.setOrientation(RecyclerView.HORIZONTAL);
        secondList.setHasFixedSize(true);
        secondList.setLayoutManager(secondListManager);
        secondList.setAdapter(getAdapter(secondlistType, secondAdapterList, secondFilterType));

        setFirstList();
        setSecondList();

        musicplayerViewModel.getAllTrackPlayedInDaySteps().observe(getViewLifecycleOwner(), list -> {
            musicplayerViewModel.getAllTrackPlayedInDaySteps().removeObservers(getViewLifecycleOwner());
            statTitle.setText("Played last week");
            stat.setValues(list, 7);
        });

        firstEdit.setOnClickListener((view) -> {
            DashboardListDialog dialog = new DashboardListDialog(requireContext(), "Configure first list", firstListType, firstFilterType, firstListSize);
            dialog.show();
            dialog.setOnFinishListener((res) -> {
                firstListType = dialog.getSelectedListType();
                firstFilterType = dialog.getSelectedFilterType();
                firstListSize = dialog.getSelectedListSize();
                firstAdapterList.clear();
                firstList.setAdapter(getAdapter(firstListType, firstAdapterList, firstFilterType));
                setFirstList();

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(getString(R.string.preference_dashboard_first_list_type), firstListType.getTypeId());
                editor.putInt(getString(R.string.preference_dashboard_first_list_filter), firstFilterType.getFilterType());
                editor.putInt(getString(R.string.preference_dashboard_first_list_size), firstListSize);
                editor.apply();
            });
        });

        secondEdit.setOnClickListener((view) -> {
            DashboardListDialog dialog = new DashboardListDialog(requireContext(), "Configure second list", secondlistType, secondFilterType, secondListSize);
            dialog.show();
            dialog.setOnFinishListener((res) -> {
                secondlistType = dialog.getSelectedListType();
                secondFilterType = dialog.getSelectedFilterType();
                secondListSize = dialog.getSelectedListSize();
                secondAdapterList.clear();
                secondList.setAdapter(getAdapter(secondlistType, secondAdapterList, secondFilterType));
                setSecondList();

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(getString(R.string.preference_dashboard_second_list_type), secondlistType.getTypeId());
                editor.putInt(getString(R.string.preference_dashboard_second_list_filter), secondFilterType.getFilterType());
                editor.putInt(getString(R.string.preference_dashboard_second_list_size), secondListSize);
                editor.apply();
            });
        });

        firstGoto.setOnClickListener((view) -> {
            if (firstListType.equals(DashboardListType.TRACK)) {
                Bundle bundle = new Bundle();
                bundle.putInt("FILTER", firstFilterType.getFilterType());

                SongList fragment = new SongList();
                fragment.setArguments(bundle);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, fragment)
                        .addToBackStack(null).commit();
            }
        });

        secondGoto.setOnClickListener((view) -> {
            if (secondlistType.equals(DashboardListType.TRACK)) {
                Bundle bundle = new Bundle();
                bundle.putInt("FILTER", secondFilterType.getFilterType());

                SongList fragment = new SongList();
                fragment.setArguments(bundle);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, fragment)
                        .addToBackStack(null).commit();
            }
        });

        return root;
    }

    private void setFirstList() {
        first.setText(DashboardEnumDeserializer.getTitleForFilterType(firstFilterType));
        switch (firstListType) {
            case TRACK:
                useTrackViewModel(firstFilterType, (ArrayList<TrackDTO>) firstAdapterList, firstList.getAdapter(), firstListManager, firstListSize);
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
                useTrackViewModel(secondFilterType, (ArrayList<TrackDTO>) secondAdapterList, secondList.getAdapter(), secondListManager, secondListSize);
                break;
            case PLAYLIST:
                usePlaylistViewModel(secondFilterType, (ArrayList<PlaylistDTO>) secondAdapterList, secondList.getAdapter());
                break;
        }
    }

    private RecyclerView.Adapter getAdapter(DashboardListType listType, ArrayList<?> list, ListFilterType filterType) {
        switch (listType) {
            case ALBUM:
                return null;
            case PLAYLIST:
                return new DashboardPlaylistAdapter(requireContext(), (ArrayList<PlaylistDTO>) list, filterType, this);
            default:
                return new DashboardTrackAdapter(requireContext(), (ArrayList<TrackDTO>) list, songInterface, filterType);
        }
    }

    private void useTrackViewModel(ListFilterType filterType, ArrayList<TrackDTO> listToFill, RecyclerView.Adapter adapter, LinearLayoutManager layoutManager, int listSize) {
        musicplayerViewModel.getTrackListByFilter(filterType).observe(getViewLifecycleOwner(), trackDTOS -> {
            int size = Math.min(trackDTOS.size(), listSize);
            if (listToFill.size() == 0) {
                listToFill.addAll(trackDTOS.subList(0, size));
                adapter.notifyItemRangeInserted(0, listToFill.size());
            } else if (filterType.equals(ListFilterType.TIMES_PLAYED)
                    || filterType.equals(ListFilterType.LAST_PLAYED)) {
                ArrayList<TrackDTO> oldList = new ArrayList<>(listToFill);
                listToFill.clear();
                listToFill.addAll(trackDTOS.subList(0, size));
                int toPos = 0, targetId = -1;
                for (int i = 0; i < oldList.size(); i++) {
                    if (!trackDTOS.get(i).getTrack().getTId().equals(oldList.get(i).getTrack().getTId())) {
                        toPos = i;
                        targetId = trackDTOS.get(i).getTrack().getTId();
                        break;
                    }
                }

                if (targetId >= 0) {
                    int fromPos = 0;
                    for (int i = 0; i < oldList.size(); i++) {
                        if (oldList.get(i).getTrack().getTId().equals(targetId)) {
                            fromPos = i;
                            break;
                        }
                    }

                    adapter.notifyItemMoved(fromPos, toPos);

                    if (toPos == 0) {
                        layoutManager.scrollToPosition(0);
                    }
                }
                adapter.notifyItemRangeChanged(0, listSize, "");
            }
        });
    }

    private synchronized void usePlaylistViewModel(ListFilterType filterType, ArrayList<PlaylistDTO> listToFill, RecyclerView.Adapter adapter) {
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
