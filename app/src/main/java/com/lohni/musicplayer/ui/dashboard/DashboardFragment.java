package com.lohni.musicplayer.ui.dashboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lohni.musicplayer.R;
import com.lohni.musicplayer.adapter.DashboardListAdapter;
import com.lohni.musicplayer.core.ApplicationDataViewModel;
import com.lohni.musicplayer.database.MusicplayerApplication;
import com.lohni.musicplayer.database.dao.MusicplayerDataAccess;
import com.lohni.musicplayer.database.dao.PlaylistDataAccess;
import com.lohni.musicplayer.database.dto.AlbumDTO;
import com.lohni.musicplayer.database.dto.DashboardDTO;
import com.lohni.musicplayer.database.dto.PlaylistDTO;
import com.lohni.musicplayer.database.dto.TrackDTO;
import com.lohni.musicplayer.database.entity.Album;
import com.lohni.musicplayer.database.entity.Track;
import com.lohni.musicplayer.database.viewmodel.MusicplayerViewModel;
import com.lohni.musicplayer.database.viewmodel.PlaylistViewModel;
import com.lohni.musicplayer.interfaces.NavigationControlInterface;
import com.lohni.musicplayer.interfaces.PlaybackControlInterface;
import com.lohni.musicplayer.interfaces.QueueControlInterface;
import com.lohni.musicplayer.ui.album.AlbumDetailFragment;
import com.lohni.musicplayer.ui.album.AlbumFragment;
import com.lohni.musicplayer.ui.playlist.PlaylistDetail;
import com.lohni.musicplayer.ui.playlist.PlaylistFragment;
import com.lohni.musicplayer.ui.songlist.SongList;
import com.lohni.musicplayer.ui.views.DashboardListDialog;
import com.lohni.musicplayer.ui.views.XYGraphView;
import com.lohni.musicplayer.utils.converter.DashboardEnumDeserializer;
import com.lohni.musicplayer.utils.enums.ListFilterType;
import com.lohni.musicplayer.utils.enums.ListType;
import com.lohni.musicplayer.utils.images.ImageUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DashboardFragment extends Fragment implements DashboardListAdapter.OnItemClickListener<DashboardDTO> {
    private MusicplayerViewModel musicplayerViewModel;
    private PlaylistViewModel playlistViewModel;
    private ApplicationDataViewModel applicationDataViewModel;
    private NavigationControlInterface navigationControlInterface;
    private TextView statTitle;
    private XYGraphView stat;

    private SharedPreferences sharedPreferences;

    private ListConfiguration firstListConfiguration, secondListConfiguration;
    private final HashMap<Integer, Drawable> albumCovers = new HashMap<>();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            navigationControlInterface = (NavigationControlInterface) context;
        } catch (ClassCastException ignored) {
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MusicplayerDataAccess mda = ((MusicplayerApplication) requireActivity().getApplication()).getDatabase().musicplayerDao();
        musicplayerViewModel = new ViewModelProvider(this, new MusicplayerViewModel.MusicplayerViewModelFactory(mda)).get(MusicplayerViewModel.class);
        PlaylistDataAccess pda = ((MusicplayerApplication) requireActivity().getApplication()).getDatabase().playlistDao();
        playlistViewModel = new ViewModelProvider(this, new PlaylistViewModel.PlaylistViewModelFactory(pda)).get(PlaylistViewModel.class);
        applicationDataViewModel = new ViewModelProvider(requireActivity()).get(ApplicationDataViewModel.class);
        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dashboard, container, false);
        RecyclerView firstList = root.findViewById(R.id.dashboard_first_list);
        RecyclerView secondList = root.findViewById(R.id.dashboard_second_list);
        TextView firstTitle = root.findViewById(R.id.dashboard_first_element);
        TextView secondTitle = root.findViewById(R.id.dashboard_second_element);
        View firstEdit = root.findViewById(R.id.dashboard_first_element_edit);
        View secondEdit = root.findViewById(R.id.dashboard_second_element_edit);
        View firstGoto = root.findViewById(R.id.dashboard_first_element_goto);
        View secondGoto = root.findViewById(R.id.dashboard_second_element_goto);
        View firstTypeImage = root.findViewById(R.id.dashboard_first_element_type);
        View secondTypeImage = root.findViewById(R.id.dashboard_second_element_type);

        statTitle = root.findViewById(R.id.dashboard_stat_title);
        stat = root.findViewById(R.id.dashboard_statistics);

        navigationControlInterface.setHomeAsUpEnabled(false);
        navigationControlInterface.isDrawerEnabledListener(true);
        navigationControlInterface.setToolbarTitle(requireContext().getString(R.string.app_name));

        ListType firstListType = DashboardEnumDeserializer.getDashboardListType(sharedPreferences.getInt(getString(R.string.preference_dashboard_first_list_type), 1));
        ListType secondlistType = DashboardEnumDeserializer.getDashboardListType(sharedPreferences.getInt(getString(R.string.preference_dashboard_second_list_type), 0));

        ListFilterType firstFilterType = DashboardEnumDeserializer.getListFilterTypeByInt(sharedPreferences.getInt(getString(R.string.preference_dashboard_first_list_filter), 1));
        ListFilterType secondFilterType = DashboardEnumDeserializer.getListFilterTypeByInt(sharedPreferences.getInt(getString(R.string.preference_dashboard_second_list_filter), 1));

        int firstListSize = sharedPreferences.getInt(getString(R.string.preference_dashboard_first_list_size), 10);
        int secondListSize = sharedPreferences.getInt(getString(R.string.preference_dashboard_second_list_size), 10);

        firstListConfiguration = new ListConfiguration(requireContext(), firstList, firstListSize, firstFilterType, firstListType, firstTitle, firstTypeImage, this);
        secondListConfiguration = new ListConfiguration(requireContext(), secondList, secondListSize, secondFilterType, secondlistType, secondTitle, secondTypeImage, this);

        musicplayerViewModel.getAllTrackPlayedInDaySteps().observe(getViewLifecycleOwner(), list -> {
            musicplayerViewModel.getAllTrackPlayedInDaySteps().removeObservers(getViewLifecycleOwner());
            statTitle.setText("Played last week");
            stat.setValues(list, 7);
        });

        firstEdit.setOnClickListener((view) -> {
            openEditDialog(firstListConfiguration, 0);
        });

        secondEdit.setOnClickListener((view) -> {
            openEditDialog(secondListConfiguration, 1);
        });

        firstGoto.setOnClickListener((view) -> {
            openFragmentByFilterType(firstListConfiguration);
        });

        secondGoto.setOnClickListener((view) -> {
            openFragmentByFilterType(secondListConfiguration);
        });

        applicationDataViewModel.getTrackImages().observe(getViewLifecycleOwner(), trackImages -> {
            if (!trackImages.isEmpty()) {
                firstListConfiguration.updateCoverImages(trackImages, ListType.TRACK);
                secondListConfiguration.updateCoverImages(trackImages, ListType.TRACK);
            }
        });

        applicationDataViewModel.getAlbumCovers().observe(getViewLifecycleOwner(), albumCovers -> {
            if (!albumCovers.isEmpty()) {
                this.albumCovers.putAll(albumCovers);
                firstListConfiguration.updateCoverImages(albumCovers, ListType.ALBUM);
                secondListConfiguration.updateCoverImages(albumCovers, ListType.ALBUM);
            }
        });

        return root;
    }

    private void openEditDialog(ListConfiguration listConfiguration, int list) {
        String title = (list == 0) ? "Configure first list" : "Configure second list";
        DashboardListDialog dialog = new DashboardListDialog(requireContext(), title, listConfiguration.listType, listConfiguration.filterType, listConfiguration.listSize);
        dialog.show();
        dialog.setOnFinishListener((res) -> {
            listConfiguration.setListType(dialog.getSelectedListType());
            listConfiguration.setFilterType(dialog.getSelectedFilterType());
            listConfiguration.setListSize(dialog.getSelectedListSize());
            listConfiguration.updateAdapter();

            String listType = (list == 0) ? getString(R.string.preference_dashboard_first_list_type) : getString(R.string.preference_dashboard_second_list_type);
            String filterType = (list == 0) ? getString(R.string.preference_dashboard_first_list_filter) : getString(R.string.preference_dashboard_second_list_filter);
            String listSize = (list == 0) ? getString(R.string.preference_dashboard_first_list_size) : getString(R.string.preference_dashboard_second_list_size);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(listType, dialog.getSelectedListType().getTypeId());
            editor.putInt(filterType, dialog.getSelectedFilterType().getFilterType());
            editor.putInt(listSize, dialog.getSelectedListSize());
            editor.apply();
        });
    }

    private void openFragmentByFilterType(ListConfiguration listConfiguration) {
        Fragment fragment;

        if (listConfiguration.listType.equals(ListType.ALBUM)) {
            fragment = new AlbumFragment();
        } else if (listConfiguration.listType.equals(ListType.PLAYLIST)) {
            fragment = new PlaylistFragment();
        } else {
            fragment = new SongList();
        }

        Bundle bundle = new Bundle();
        bundle.putInt("FILTER", listConfiguration.filterType.getFilterType());

        fragment.setArguments(bundle);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null).commit();
    }

    private void useAlbumViewModel(ListConfiguration listConfiguration, DashboardListAdapter adapter) {
        listConfiguration.currentObserver = (newList) -> updateAdapterList(listConfiguration, adapter, (List<DashboardDTO>) newList);
        musicplayerViewModel.getAlbumListByFilter(listConfiguration.filterType).observe(getViewLifecycleOwner(), listConfiguration.currentObserver);
    }

    private void useTrackViewModel(ListConfiguration listConfiguration, DashboardListAdapter adapter) {
        listConfiguration.currentObserver = (trackDTOS) -> updateAdapterList(listConfiguration, adapter, (List<DashboardDTO>) trackDTOS);
        musicplayerViewModel.getTrackListByFilter(listConfiguration.filterType).observe(getViewLifecycleOwner(), listConfiguration.currentObserver);
    }

    private void usePlaylistViewModel(ListConfiguration listConfiguration, DashboardListAdapter adapter) {
        listConfiguration.currentObserver = (newList) -> updateAdapterList(listConfiguration, adapter, (List<DashboardDTO>) newList);
        playlistViewModel.getPlaylistByFilter(listConfiguration.filterType).observe(getViewLifecycleOwner(), listConfiguration.currentObserver);
    }

    private void removeObserver(Observer observer, ListFilterType filterType) {
        if (observer != null) {
            musicplayerViewModel.getTrackListByFilter(filterType).removeObserver(observer);
            musicplayerViewModel.getAlbumListByFilter(filterType).removeObserver(observer);
            playlistViewModel.getPlaylistByFilter(filterType).removeObserver(observer);
        }
    }

    private <T extends DashboardDTO> void updateAdapterList(ListConfiguration listConfiguration, DashboardListAdapter adapter, List<T> newList) {
        if (listConfiguration.itemList.isEmpty()) {
            listConfiguration.itemList.addAll(newList.subList(0, Math.min(listConfiguration.listSize, newList.size())));
            adapter.notifyItemRangeInserted(0, listConfiguration.itemList.size());
        } else {
            ArrayList<DashboardDTO> oldList = new ArrayList<>(listConfiguration.itemList);
            listConfiguration.itemList.clear();
            listConfiguration.itemList.addAll(newList.subList(0, Math.min(newList.size(), listConfiguration.listSize)));
            if (!newList.isEmpty()) {
                int toPos = 0, targetId = -1;
                for (int i = 0; i < oldList.size(); i++) {
                    if (newList.get(i).getId().equals(oldList.get(i).getId())) {
                        toPos = i;
                        targetId = newList.get(i).getId();
                        break;
                    }
                }

                if (targetId >= 0) {
                    int fromPos = 0;
                    for (int i = 0; i < oldList.size(); i++) {
                        if (oldList.get(i).getId().equals(targetId)) {
                            fromPos = i;
                            break;
                        }
                    }

                    adapter.notifyItemMoved(fromPos, toPos);

                    if (toPos == 0) {
                        listConfiguration.layoutManager.scrollToPosition(0);
                    }
                }
            }

            adapter.notifyItemRangeChanged(0, listConfiguration.listSize);
        }
    }

    @Override
    public void onItemClick(DashboardDTO item) {
        if (item instanceof TrackDTO) {
            Track[] list = {((TrackDTO) item).getTrack()};
            requireActivity().sendBroadcast(new Intent(getString(R.string.musicservice_play_list)).putExtra("LIST", list));
        } else if (item instanceof AlbumDTO) {
            Album album = ((AlbumDTO) item).getAlbum().album;

            Bundle bundle = new Bundle();
            bundle.putInt("ALBUM_ID", album.getAId());
            
            if (albumCovers.containsKey(album.getAId())) {
                bundle.putParcelable("COVER", ImageUtil.getBitmapFromDrawable(requireContext(), albumCovers.get(album.getAId())));
            }

            AlbumDetailFragment albumDetailFragment = new AlbumDetailFragment();
            albumDetailFragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, albumDetailFragment)
                    .addToBackStack(null)
                    .commit();
        } else if (item instanceof PlaylistDTO) {
            PlaylistDetail playlistDetailFragment = new PlaylistDetail();

            Bundle bundle = new Bundle();
            bundle.putInt("PLAYLIST_ID", ((PlaylistDTO) item).getPlaylist().getPId());
            playlistDetailFragment.setArguments(bundle);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, playlistDetailFragment, getString(R.string.fragment_playlist_detail))
                    .addToBackStack(null)
                    .commit();
        }
    }

    private class ListConfiguration {
        private ListType listType;
        private ListFilterType filterType;
        private LinearLayoutManager layoutManager;
        private final List<DashboardDTO> itemList = new ArrayList<>();
        private final RecyclerView recyclerView;
        private final TextView title;
        private final View typeImage;
        private int listSize;
        private final DashboardListAdapter.OnItemClickListener<DashboardDTO> onItemClickListener;

        private Observer currentObserver;

        public ListConfiguration(Context context, RecyclerView recyclerView, int listSize, ListFilterType filterType, ListType listType, TextView title, View typeImage, DashboardListAdapter.OnItemClickListener<DashboardDTO> onItemClickListener) {
            this.recyclerView = recyclerView;
            this.listSize = listSize;
            this.title = title;
            this.filterType = filterType;
            this.listType = listType;
            this.onItemClickListener = onItemClickListener;
            this.typeImage = typeImage;
            init(context);
        }

        private void init(Context context) {
            layoutManager = new LinearLayoutManager(context);
            layoutManager.setOrientation(RecyclerView.HORIZONTAL);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(layoutManager);
            title.setText(DashboardEnumDeserializer.getTitleForFilterType(filterType));
            typeImage.setBackground(ContextCompat.getDrawable(requireContext(), DashboardEnumDeserializer.getDrawableIdForListType(listType)));
            updateAdapter();
        }

        public void updateAdapter() {
            DashboardListAdapter adapter = new DashboardListAdapter(requireContext(), itemList, filterType);
            adapter.setOnItemClickListener(onItemClickListener);
            recyclerView.setAdapter(adapter);
            switch (listType) {
                case TRACK:
                    useTrackViewModel(this, adapter);
                    break;
                case PLAYLIST:
                    usePlaylistViewModel(this, adapter);
                    break;
                case ALBUM:
                    useAlbumViewModel(this, adapter);
                    break;
            }
        }

        public void updateCoverImages(HashMap<Integer, Drawable> hashMap, ListType type) {
            DashboardListAdapter adapter = (DashboardListAdapter) recyclerView.getAdapter();
            if (type.equals(listType)) {
                adapter.setBackgroundImages(hashMap);
                adapter.notifyItemRangeChanged(0, listSize);
            }
        }

        public void setListType(ListType listType) {
            removeObserver(currentObserver, filterType);
            this.listType = listType;
            typeImage.setBackground(ContextCompat.getDrawable(requireContext(), DashboardEnumDeserializer.getDrawableIdForListType(listType)));
        }

        public void setFilterType(ListFilterType filterType) {
            removeObserver(currentObserver, filterType);
            this.filterType = filterType;
            title.setText(DashboardEnumDeserializer.getTitleForFilterType(filterType));
        }

        public void setListSize(int listSize) {
            this.listSize = listSize;
        }
    }
}
