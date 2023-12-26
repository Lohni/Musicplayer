package com.lohni.musicplayer.ui.songlist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lohni.musicplayer.R;
import com.lohni.musicplayer.adapter.SongListAdapter;
import com.lohni.musicplayer.core.ApplicationDataViewModel;
import com.lohni.musicplayer.database.MusicplayerApplication;
import com.lohni.musicplayer.database.dao.MusicplayerDataAccess;
import com.lohni.musicplayer.database.dao.PreferenceDataAccess;
import com.lohni.musicplayer.database.dto.TrackDTO;
import com.lohni.musicplayer.database.entity.Track;
import com.lohni.musicplayer.database.viewmodel.MusicplayerViewModel;
import com.lohni.musicplayer.database.viewmodel.SonglistViewModel;
import com.lohni.musicplayer.interfaces.NavigationControlInterface;
import com.lohni.musicplayer.interfaces.PlaybackControlInterface;
import com.lohni.musicplayer.interfaces.QueueControlInterface;
import com.lohni.musicplayer.interfaces.ServiceTriggerInterface;
import com.lohni.musicplayer.ui.views.DeleteDialog;
import com.lohni.musicplayer.ui.views.SideIndex;
import com.lohni.musicplayer.utils.AdapterUtils;
import com.lohni.musicplayer.utils.GeneralUtils;
import com.lohni.musicplayer.utils.enums.ListFilterType;
import com.lohni.musicplayer.utils.enums.ListType;
import com.lohni.musicplayer.utils.enums.PlaybackBehaviour;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SongList extends Fragment {
    private TextView shuffle_size;
    private View menuLayout;

    private NavigationControlInterface navigationControlInterface;
    private QueueControlInterface songInterface;
    private PlaybackControlInterface playbackControlInterface;
    private ServiceTriggerInterface serviceTriggerInterface;

    private SongListAdapter songListAdapter;
    private SideIndex sideIndex;
    private LinearLayoutManager listViewManager;
    private MusicplayerViewModel musicplayerViewModel;
    private ApplicationDataViewModel applicationDataViewModel;
    private SonglistViewModel songlistViewModel;

    private final ArrayList<TrackDTO> songList = new ArrayList<>();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            navigationControlInterface = (NavigationControlInterface) context;
            songInterface = (QueueControlInterface) context;
            playbackControlInterface = (PlaybackControlInterface) context;
            serviceTriggerInterface = (ServiceTriggerInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + "must implement Interface");
        }
    }

    @Override
    public void onDetach() {
        requireActivity().unregisterReceiver(receiver);
        super.onDetach();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MusicplayerDataAccess mda = ((MusicplayerApplication) requireActivity().getApplication()).getDatabase().musicplayerDao();
        PreferenceDataAccess pda = ((MusicplayerApplication) requireActivity().getApplication()).getDatabase().preferenceDao();
        songlistViewModel = new ViewModelProvider(this, new SonglistViewModel.SonglistViewModelFactory(mda, pda)).get(SonglistViewModel.class);
        musicplayerViewModel = new ViewModelProvider(this, new MusicplayerViewModel.MusicplayerViewModelFactory(mda)).get(MusicplayerViewModel.class);
        applicationDataViewModel = new ViewModelProvider(requireActivity()).get(ApplicationDataViewModel.class);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(getResources().getString(R.string.musicservice_song_prepared));
        intentFilter.addAction(getResources().getString(R.string.playback_control_values));
        requireActivity().registerReceiver(receiver, intentFilter);

        if (getArguments() != null && getArguments().containsKey("FILTER")) {
            songlistViewModel.setListFilterType(ListFilterType.Companion.getListFilterTypeByInt(getArguments().getInt("FILTER")));
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.songlist_menu, menu);
        menu.getItem(0).setIconTintList(ContextCompat.getColorStateList(requireContext(), R.color.colorOnSurface));
        menu.getItem(1).setIconTintList(ContextCompat.getColorStateList(requireContext(), R.color.colorOnSurface));
        menu.getItem(2).setIconTintList(ContextCompat.getColorStateList(requireContext(), R.color.colorOnSurface));

        menu.getItem(2).setIcon(ListFilterType.Companion.getDrawableForFilterType(songlistViewModel.getFilter().getValue()));

        SearchView view = (SearchView) menu.findItem(R.id.action_songlist_search).getActionView();
        view.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                songListAdapter.getFilter().filter(newText);
                return false;
            }
        });

        view.setOnCloseListener(() -> {
            songListAdapter.getFilter().filter("");
            return false;
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_songlist_jumpto) {
            jumpToCurrentPlayingSong();
        } else if (item.getItemId() == R.id.action_songlist_filter) {
            int wrapContent = LinearLayout.LayoutParams.WRAP_CONTENT;
            menuLayout.setAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.popupwindow_show));
            PopupWindow filterMenu = new PopupWindow(menuLayout, wrapContent, wrapContent, true);

            menuLayout.findViewById(R.id.songlist_filter_menu_az).setOnClickListener(view1 -> {
                applyFilterMenu(ListFilterType.ALPHABETICAL, filterMenu, item);
            });

            menuLayout.findViewById(R.id.songlist_filter_menu_lastplayed).setOnClickListener(view1 -> {
                applyFilterMenu(ListFilterType.LAST_PLAYED, filterMenu, item);
            });

            menuLayout.findViewById(R.id.songlist_filter_menu_timeplayed).setOnClickListener(view1 -> {
                applyFilterMenu(ListFilterType.TIME_PLAYED, filterMenu, item);
            });

            menuLayout.findViewById(R.id.songlist_filter_menu_timesplayed).setOnClickListener(view1 -> {
                applyFilterMenu(ListFilterType.TIMES_PLAYED, filterMenu, item);
            });

            menuLayout.findViewById(R.id.songlist_filter_menu_created).setOnClickListener(view1 -> {
                applyFilterMenu(ListFilterType.LAST_CREATED, filterMenu, item);
            });

            filterMenu.setContentView(menuLayout);
            filterMenu.setOutsideTouchable(true);
            filterMenu.showAsDropDown(getView(), 0, -getView().getHeight(), Gravity.END);
            serviceTriggerInterface.triggerCurrentDataBroadcast();
        }

        return super.onOptionsItemSelected(item);
    }

    private void applyFilterMenu(ListFilterType filterType, PopupWindow filterMenu, MenuItem item) {
        item.setIcon(ListFilterType.Companion.getDrawableForFilterType(filterType));
        item.setIconTintList(ContextCompat.getColorStateList(requireContext(), R.color.colorOnSurface));
        songlistViewModel.setListFilterType(filterType);
        filterMenu.dismiss();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        navigationControlInterface.isDrawerEnabledListener(true);
        navigationControlInterface.setHomeAsUpEnabled(false);
        navigationControlInterface.setToolbarTitle("Tracklist");

        View mainView = inflater.inflate(R.layout.fragment_song_list, container, false);
        menuLayout = inflater.inflate(R.layout.songlist_filter_menu, null);
        RecyclerView listView = mainView.findViewById(R.id.songList);
        ConstraintLayout shuffle = mainView.findViewById(R.id.songlist_shuffle);
        shuffle_size = mainView.findViewById(R.id.songlist_shuffle_size);

        songListAdapter = new SongListAdapter(requireContext(), songList);

        songListAdapter.setOnItemClickedListener((pos) -> {
            List<Track> songListAsTracks = songList.stream().map(TrackDTO::getTrack).collect(Collectors.toList());
            playbackControlInterface.onPlaybackBehaviourChangeListener(PlaybackBehaviour.REPEAT_LIST);
            if (songListAdapter.getQueueItemCount() == 0) songInterface.onSongListCreatedListener(songListAsTracks, ListType.TRACK, false);
            songInterface.onSongSelectedListener(songList.get(pos).getTrack());
        });

        songListAdapter.setOnItemOptionClickedListener((view, position, inQueue) -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), view);
            popupMenu.getMenuInflater().inflate(R.menu.songlist_item_more, popupMenu.getMenu());

            if (inQueue) popupMenu.getMenu().getItem(0).setTitle("Remove from queue");

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_songlist_item_add_to_queue) {
                    List<Track> track = new ArrayList<>();
                    track.add(songList.get(position).getTrack());

                    if (!inQueue) {
                        songInterface.onAddSongsToSonglistListener(track, false);
                        playbackControlInterface.onPlaybackBehaviourChangeListener(PlaybackBehaviour.REPEAT_LIST);
                    } else {
                        songInterface.onSongsRemoveListener(track);
                    }
                } else if (item.getItemId() == R.id.action_songlist_item_play_next) {
                    List<Track> track = new ArrayList<>();
                    track.add(songList.get(position).getTrack());
                    songInterface.onAddSongsToSonglistListener(track, true);
                    playbackControlInterface.onPlaybackBehaviourChangeListener(PlaybackBehaviour.REPEAT_LIST);
                } else if (item.getItemId() == R.id.action_songlist_item_new_queue) {
                    List<Track> tracks = new ArrayList<>();
                    tracks.add(songList.get(position).getTrack());
                    playbackControlInterface.onPlaybackBehaviourChangeListener(PlaybackBehaviour.REPEAT_LIST);
                    songInterface.onSongListCreatedListener(tracks, ListType.TRACK, true);
                } else if (item.getItemId() == R.id.action_songlist_item_delete) {
                    DeleteDialog dialog = new DeleteDialog(requireContext());
                    dialog.setOnDeleteListener((v) -> {
                        Track toDelete = songList.get(position).getTrack();
                        toDelete.setTDeleted(1);
                        musicplayerViewModel.updateTrack(toDelete);
                        List<Track> del = new ArrayList<>();
                        del.add(toDelete);
                        songInterface.onSongsRemoveListener(del);
                        songList.remove(position);
                        songListAdapter.notifyItemRemoved(position);
                    });
                    dialog.show();
                }
                return false;
            });
            popupMenu.show();
        });

        listView.setAdapter(songListAdapter);

        songlistViewModel.getFilter().observe(getViewLifecycleOwner(), filter -> {
            songListAdapter.setListFilterType(filter);
            listViewManager.scrollToPosition(0);
            if (filter.equals(ListFilterType.ALPHABETICAL)) sideIndex.setVisible();
            else sideIndex.setVisibilityGone();
        });

        applicationDataViewModel.getTrackImages().observe(getViewLifecycleOwner(), hashmap -> {
            songListAdapter.setDrawableHashMap(hashmap);
            songListAdapter.notifyItemRangeChanged(0, songListAdapter.getItemCount(), "RELOAD_IMAGES");
        });

        songlistViewModel.getSongList().observe(getViewLifecycleOwner(), songList -> {
            ArrayList<TrackDTO> oldList = new ArrayList<>(this.songList);
            this.songList.clear();
            this.songList.addAll(songList);

            int time = songList.stream().map(TrackDTO::getTrack).map(Track::getTDuration).reduce(0, Integer::sum);
            shuffle_size.setText(songList.size() + " songs - " + GeneralUtils.convertTimeWithUnit(time));

            sideIndex.setIndexList(songList).displayIndex();
            if (AdapterUtils.moveItemToNewPositionList(oldList, songList, songListAdapter) == 0)
                listViewManager.scrollToPosition(0);

            int sizeDiff = songList.size() - oldList.size();
            if (sizeDiff > 0) songListAdapter.notifyItemRangeInserted(oldList.size(), sizeDiff);
            else if (sizeDiff < 0)
                songListAdapter.notifyItemRangeRemoved(songList.size(), sizeDiff * -1);

            songListAdapter.notifyItemRangeChanged(0, songList.size());
        });

        LinearLayout linearLayout = mainView.findViewById(R.id.side_index);
        FrameLayout indexZoomHolder = mainView.findViewById(R.id.songlist_indexzoom_holder);
        TextView indexZoom = mainView.findViewById(R.id.songlist_indexzoom);
        listViewManager = new LinearLayoutManager(requireContext());
        sideIndex = new SideIndex(requireActivity(), linearLayout, indexZoomHolder, indexZoom, listViewManager);

        listView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_animation_fall_down));
        listView.setHasFixedSize(true);
        listView.setLayoutManager(listViewManager);

        shuffle.setOnClickListener(view -> {
            playbackControlInterface.onPlaybackBehaviourChangeListener(PlaybackBehaviour.SHUFFLE);
            List<Track> songListAsTracks = songList.stream().map(TrackDTO::getTrack).collect(Collectors.toList());
            songInterface.onSongListCreatedListener(songListAsTracks, ListType.TRACK, true);
        });

        return mainView;
    }

    private void jumpToCurrentPlayingSong() {
        int targetIndex = -1;
        for (int i = 0; i < songList.size(); i++) {
            Track track = songList.get(i).getTrack();
            if (track.getTId().equals(songListAdapter.getCurrentPlayingIndex())) {
                targetIndex = i;
                break;
            }
        }

        if (targetIndex >= 0) {
            listViewManager.scrollToPositionWithOffset(targetIndex, listViewManager.getHeight() / 2);
        } else {
            Toast.makeText(requireContext(), "No playing song found", Toast.LENGTH_SHORT).show();
        }
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(getResources().getString(R.string.musicservice_song_prepared))) {
                Bundle bundle = intent.getExtras();
                songListAdapter.setCurrentPlayingIndex(bundle.getInt("ID", -1));
            } else if (intent.getAction().equals(getResources().getString(R.string.playback_control_values))) {
                Bundle bundle = intent.getExtras();
                songListAdapter.setPlaybackBehaviour(PlaybackBehaviour.Companion.getStateFromInteger(bundle.getInt("BEHAVIOUR_STATE")));
                songListAdapter.setCurrentPlayingIndex(bundle.getInt("ID", -1));
                ArrayList<Track> t = bundle.getParcelableArrayList(getResources().getString(R.string.parcelable_track_list));
                songListAdapter.setQueueList(t);
            }
        }
    };
}
