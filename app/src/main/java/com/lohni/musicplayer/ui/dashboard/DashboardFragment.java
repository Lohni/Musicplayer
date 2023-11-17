package com.lohni.musicplayer.ui.dashboard;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lohni.musicplayer.R;
import com.lohni.musicplayer.adapter.DashboardListAdapter;
import com.lohni.musicplayer.core.ApplicationDataViewModel;
import com.lohni.musicplayer.database.MusicplayerApplication;
import com.lohni.musicplayer.database.dao.MusicplayerDataAccess;
import com.lohni.musicplayer.database.dao.PlaylistDataAccess;
import com.lohni.musicplayer.database.dao.PreferenceDataAccess;
import com.lohni.musicplayer.database.dto.AlbumDTO;
import com.lohni.musicplayer.database.dto.DashboardDTO;
import com.lohni.musicplayer.database.dto.PlaylistDTO;
import com.lohni.musicplayer.database.dto.TrackDTO;
import com.lohni.musicplayer.database.entity.Album;
import com.lohni.musicplayer.database.entity.DashboardListConfiguration;
import com.lohni.musicplayer.database.entity.Track;
import com.lohni.musicplayer.database.viewmodel.MusicplayerViewModel;
import com.lohni.musicplayer.database.viewmodel.PlaylistViewModel;
import com.lohni.musicplayer.interfaces.NavigationControlInterface;
import com.lohni.musicplayer.ui.album.AlbumDetailFragment;
import com.lohni.musicplayer.ui.album.AlbumFragment;
import com.lohni.musicplayer.ui.playlist.PlaylistDetail;
import com.lohni.musicplayer.ui.playlist.PlaylistFragment;
import com.lohni.musicplayer.ui.songlist.SongList;
import com.lohni.musicplayer.ui.views.DashboardListDialog;
import com.lohni.musicplayer.ui.views.XYGraphView;
import com.lohni.musicplayer.utils.enums.ListFilterType;
import com.lohni.musicplayer.utils.enums.ListType;
import com.lohni.musicplayer.utils.images.ImageUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DashboardFragment extends Fragment implements DashboardListAdapter.OnItemClickListener<DashboardDTO> {
    private MusicplayerViewModel musicplayerViewModel;
    private PlaylistViewModel playlistViewModel;
    private ApplicationDataViewModel applicationDataViewModel;
    private DashboardViewModel dashboardViewModel;
    private NavigationControlInterface navigationControlInterface;
    private TextView statTitle;
    private XYGraphView stat;

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
        PreferenceDataAccess prefda = ((MusicplayerApplication) requireActivity().getApplication()).getDatabase().preferenceDao();
        dashboardViewModel = new ViewModelProvider(this, new DashboardViewModel.DashboardViewModelFactory(prefda)).get(DashboardViewModel.class);
        applicationDataViewModel = new ViewModelProvider(requireActivity()).get(ApplicationDataViewModel.class);
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

        dashboardViewModel.getFirstListConfiguration().observe(getViewLifecycleOwner(), listConfiguration -> {
            if (firstListConfiguration == null) {
                firstListConfiguration = new ListConfiguration(requireContext(), listConfiguration, firstList, firstTitle, firstTypeImage, this);
            } else {
                firstListConfiguration.updateListConfiguration(listConfiguration);
            }
        });

        dashboardViewModel.getSecondListConfiguration().observe(getViewLifecycleOwner(), listConfiguration -> {
            if (secondListConfiguration == null) {
                secondListConfiguration = new ListConfiguration(requireContext(), listConfiguration, secondList, secondTitle, secondTypeImage, this);
            } else {
                secondListConfiguration.updateListConfiguration(listConfiguration);
            }
        });

        musicplayerViewModel.getAllTrackPlayedInDaySteps().observe(getViewLifecycleOwner(), list -> {
            musicplayerViewModel.getAllTrackPlayedInDaySteps().removeObservers(getViewLifecycleOwner());
            statTitle.setText("Played last week");
            stat.setValues(list, 7);
        });

        firstEdit.setOnClickListener((view) -> openEditDialog(firstListConfiguration));

        secondEdit.setOnClickListener((view) -> openEditDialog(secondListConfiguration));

        firstGoto.setOnClickListener((view) -> openFragmentByFilterType(firstListConfiguration));

        secondGoto.setOnClickListener((view) -> openFragmentByFilterType(secondListConfiguration));

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

    private void openEditDialog(ListConfiguration listConfiguration) {
        String title = listConfiguration.listConfiguration.getId() == 0 ? "Configure first list" : "Configure second list";
        DashboardListDialog dialog = new DashboardListDialog(requireContext(), title, listConfiguration.listConfiguration);
        dialog.show();
        dialog.setOnFinishListener((res) -> dashboardViewModel.updateListConfiguration(dialog.getListConfiguration()));
    }

    private void openFragmentByFilterType(ListConfiguration listConfiguration) {
        Fragment fragment;
        DashboardListConfiguration listConf = listConfiguration.listConfiguration;
        if (listConf.getListType().equals(ListType.ALBUM)) {
            fragment = new AlbumFragment();
        } else if (listConf.getListType().equals(ListType.PLAYLIST)) {
            fragment = new PlaylistFragment();
        } else {
            fragment = new SongList();
        }

        Bundle bundle = new Bundle();
        bundle.putInt("FILTER", ListFilterType.Companion.getFilterTypeAsInt(listConf.getListFilterType()));
        fragment.setArguments(bundle);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null).commit();
    }

    private void useAlbumViewModel(ListFilterType filterType, Observer<List<? extends DashboardDTO>> observer) {
        musicplayerViewModel.getAlbumListByFilter(filterType).observe(getViewLifecycleOwner(), observer);
    }

    private void useTrackViewModel(ListFilterType filterType, Observer<List<? extends DashboardDTO>> observer) {
        musicplayerViewModel.getTrackListByFilter(filterType).observe(getViewLifecycleOwner(), observer);
    }

    private void usePlaylistViewModel(ListFilterType filterType, Observer<List<? extends DashboardDTO>> observer) {
        playlistViewModel.getPlaylistByFilter(filterType).observe(getViewLifecycleOwner(), observer);
    }

    private void removeObserver(Observer<List<? extends DashboardDTO>> observer, ListFilterType filterType) {
        if (observer != null) {
            musicplayerViewModel.getTrackListByFilter(filterType).removeObserver(observer);
            musicplayerViewModel.getAlbumListByFilter(filterType).removeObserver(observer);
            playlistViewModel.getPlaylistByFilter(filterType).removeObserver(observer);
        }
    }

    private <T extends DashboardDTO> void updateAdapterList(ListConfiguration listConfiguration, DashboardListAdapter<T> adapter, List<? extends DashboardDTO> newList) {
        DashboardListConfiguration dashboardListConfiguration = listConfiguration.listConfiguration;
        List<T> oldList = new ArrayList<T>(listConfiguration.itemList);
        listConfiguration.itemList.clear();
        listConfiguration.itemList.addAll(newList.subList(0, Math.min(newList.size(), dashboardListConfiguration.getListSize())));

        if (oldList.size() > newList.size())
            adapter.notifyItemRangeRemoved(newList.size(), oldList.size());
        else if (oldList.size() < newList.size())
            adapter.notifyItemRangeInserted(oldList.size(), newList.size());

        adapter.notifyItemRangeChanged(0, listConfiguration.itemList.size());
    }

    @Override
    public void onItemClick(DashboardDTO item) {
        if (item instanceof TrackDTO) {
            Track[] list = {((TrackDTO) item).getTrack()};
            requireActivity().sendBroadcast(new Intent(getString(R.string.musicservice_play_list)).putExtra("LIST", list).putExtra("INDEX", 0));
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

    private class ListConfiguration<T extends DashboardDTO> {
        private DashboardListConfiguration listConfiguration;
        private LinearLayoutManager layoutManager;
        private final List<T> itemList = new ArrayList<>();
        private final TextView title;
        private final View typeImage;
        private final DashboardListAdapter<T> adapter;
        private Observer<List<? extends DashboardDTO>> currentObserver;

        public ListConfiguration(Context context, DashboardListConfiguration listConfiguration, RecyclerView recyclerView, TextView title, View typeImage, DashboardListAdapter.OnItemClickListener<T> onItemClickListener) {
            adapter = new DashboardListAdapter<>(requireContext(), itemList, listConfiguration.getListFilterType());
            adapter.setOnItemClickListener(onItemClickListener);
            this.listConfiguration = listConfiguration;
            this.title = title;
            this.typeImage = typeImage;
            init(context, recyclerView);
        }

        private void init(Context context, RecyclerView recyclerView) {
            layoutManager = new LinearLayoutManager(context);
            layoutManager.setOrientation(RecyclerView.HORIZONTAL);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);

            updateList();
        }

        private void updateList() {
            title.setText(ListFilterType.Companion.getTitleForFilterType(listConfiguration.getListFilterType()));
            typeImage.setBackground(ContextCompat.getDrawable(requireContext(), ListType.Companion.getDrawableIdForListType(listConfiguration.getListType())));
            adapter.updateFilterType(listConfiguration.getListFilterType());
            updateAdapter();
        }

        private void updateAdapter() {
            currentObserver = (newList) -> updateAdapterList(this, adapter, newList);
            switch (listConfiguration.getListType()) {
                case TRACK:
                    useTrackViewModel(listConfiguration.getListFilterType(), currentObserver);
                    break;
                case PLAYLIST:
                    usePlaylistViewModel(listConfiguration.getListFilterType(), currentObserver);
                    break;
                case ALBUM:
                    useAlbumViewModel(listConfiguration.getListFilterType(), currentObserver);
                    break;
            }
        }

        public void updateCoverImages(HashMap<Integer, Drawable> hashMap, ListType type) {
            if (type.equals(listConfiguration.getListType())) {
                adapter.setBackgroundImages(hashMap);
                adapter.notifyItemRangeChanged(0, listConfiguration.getListSize());
            }
        }

        public void updateListConfiguration(DashboardListConfiguration listConfiguration) {
            removeObserver(currentObserver, this.listConfiguration.getListFilterType());
            this.listConfiguration = listConfiguration;
            updateList();
        }
    }
}
