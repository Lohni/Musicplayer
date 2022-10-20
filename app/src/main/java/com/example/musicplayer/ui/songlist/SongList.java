package com.example.musicplayer.ui.songlist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.musicplayer.R;
import com.example.musicplayer.adapter.SongListAdapter;
import com.example.musicplayer.database.MusicplayerApplication;
import com.example.musicplayer.database.dao.MusicplayerDataAccess;
import com.example.musicplayer.database.dto.TrackDTO;
import com.example.musicplayer.database.entity.Track;
import com.example.musicplayer.database.viewmodel.MusicplayerViewModel;
import com.example.musicplayer.interfaces.PlaybackControlInterface;
import com.example.musicplayer.interfaces.ServiceTriggerInterface;
import com.example.musicplayer.interfaces.SongInterface;
import com.example.musicplayer.ui.views.SideIndex;
import com.example.musicplayer.utils.GeneralUtils;
import com.example.musicplayer.utils.NavigationControlInterface;
import com.example.musicplayer.utils.enums.DashboardEnumDeserializer;
import com.example.musicplayer.utils.enums.DashboardListType;
import com.example.musicplayer.utils.enums.ListFilterType;
import com.example.musicplayer.utils.enums.PlaybackBehaviour;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SongList extends Fragment implements SongListInterface {
    private RecyclerView listView;
    private View view;
    private TextView shuffle_size,filterTitle;
    private ConstraintLayout shuffle, filter;
    private TextInputEditText search;
    private TextInputLayout search_layout;

    private NavigationControlInterface navigationControlInterface;
    private SongInterface songInterface;
    private PlaybackControlInterface playbackControlInterface;
    private SongListInterface songListInterface;
    private ServiceTriggerInterface serviceTriggerInterface;

    private SongListAdapter songListAdapter;
    private SideIndex sideIndex;
    private ListFilterType listFilterType;
    private LinearLayoutManager listViewManager;

    private SharedPreferences sharedPreferences;
    private MusicplayerViewModel musicplayerViewModel;

    private final ArrayList<TrackDTO> songList = new ArrayList<>();
    private boolean isSonglistSet = false, isSearchModeActive = false, firstLoad = true, listChange = false;
    private int currentPlayingSongId = -1, currentScrollingSpeed = 0;

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
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            currentPlayingSongId = getArguments().getInt("ID", -1);
            int filter = getArguments().getInt("FILTER", -1);
            if (filter >= 0) {
                listFilterType = DashboardEnumDeserializer.getListFilterTypeByInt(filter);
            }
        }

        MusicplayerDataAccess mda = ((MusicplayerApplication) requireActivity().getApplication()).getDatabase().musicplayerDao();
        musicplayerViewModel = new ViewModelProvider(this, new MusicplayerViewModel.MusicplayerViewModelFactory(mda)).get(MusicplayerViewModel.class);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(getResources().getString(R.string.musicservice_song_prepared));
        intentFilter.addAction(getResources().getString(R.string.playback_control_values));
        requireActivity().registerReceiver(receiver, intentFilter);

        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.songlist_menu, menu);
        menu.getItem(0).setIconTintList(ContextCompat.getColorStateList(requireContext(), R.color.colorOnSurface));
        menu.getItem(1).setIconTintList(ContextCompat.getColorStateList(requireContext(), R.color.colorOnSurface));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_songlist_search) {
            if (!isSearchModeActive) {
                item.setIcon(R.drawable.ic_shuffle_black_24dp);
                item.setIconTintList(ContextCompat.getColorStateList(requireContext(), R.color.colorOnSurface));

                Animation anim = AnimationUtils.loadAnimation(requireContext(), R.anim.view_animation_to_bottom);
                shuffle.setVisibility(View.GONE);
                shuffle.startAnimation(anim);

                Animation animFallDown = AnimationUtils.loadAnimation(requireContext(), R.anim.view_animation_from_top);
                search_layout.setVisibility(View.VISIBLE);
                search_layout.setAnimation(animFallDown);
                isSearchModeActive = true;
            } else {
                if (!search.getText().toString().equals("")) {
                    songListAdapter.getFilter().filter("");
                }
                search.setText("");
                item.setIcon(R.drawable.ic_search_black_24dp);
                item.setIconTintList(ContextCompat.getColorStateList(requireContext(), R.color.colorOnSurface));

                Animation anim = AnimationUtils.loadAnimation(requireContext(), R.anim.view_animation_to_bottom);
                search_layout.setVisibility(View.GONE);
                search_layout.startAnimation(anim);

                Animation animFallDown = AnimationUtils.loadAnimation(requireContext(), R.anim.view_animation_from_top);
                shuffle.setVisibility(View.VISIBLE);
                shuffle.setAnimation(animFallDown);

                isSearchModeActive = false;

                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
            }
        } else if (item.getItemId() == R.id.action_songlist_jumpto)  {
            jumpToCurrentPlayingSong();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        songListAdapter.getAllBackgroundImages(songList);
        int time = songList.stream().map(TrackDTO::getTrack).map(Track::getTDuration).reduce(0, Integer::sum);
        shuffle_size.setText(songList.size() + " songs - " + GeneralUtils.convertTimeWithUnit(time));
        serviceTriggerInterface.triggerCurrentDataBroadcast();
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
        search = view.findViewById(R.id.songlist_search);
        search_layout = view.findViewById(R.id.songlist_search_layout);
        shuffle_size = view.findViewById(R.id.songlist_shuffle_size);
        filter = view.findViewById(R.id.songlist_filter_holder);
        filterTitle = view.findViewById(R.id.songlist_filter_title);

        if (listFilterType == null) {
            listFilterType = DashboardEnumDeserializer.getListFilterTypeByInt(sharedPreferences.getInt(getString(R.string.preference_songlist_filter_type), 3));
        }

        filterTitle.setText(listFilterType.getId());

        LinearLayout linearLayout = view.findViewById(R.id.side_index);
        FrameLayout indexZoomHolder = view.findViewById(R.id.songlist_indexzoom_holder);
        TextView indexZoom = view.findViewById(R.id.songlist_indexzoom);
        listViewManager = new LinearLayoutManager(requireContext());
        sideIndex = new SideIndex(requireActivity(), linearLayout, indexZoomHolder, indexZoom, listViewManager);

        listView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_animation_fall_down));
        listView.setHasFixedSize(true);
        listView.setLayoutManager(listViewManager);

        listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == 0) {
                    songListAdapter.isScrolling(false);
                    if (currentScrollingSpeed > 8) {
                        songListAdapter.notifyItemRangeChanged(0, songList.size(), "SCROLL");
                    }
                    currentScrollingSpeed = 0;
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                int dyAbs = Math.abs(dy);
                songListAdapter.isScrolling(dyAbs > 50);

                if (!(currentScrollingSpeed > 25 && dy < 5) && !((currentScrollingSpeed - dyAbs) > 60 && dyAbs < 20)) {
                    currentScrollingSpeed = dyAbs;
                }

                System.out.println(dy);
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        songListAdapter = new SongListAdapter(requireContext(), songList, listFilterType);
        songListAdapter.setCurrentPlayingIndex(currentPlayingSongId);
        songListAdapter.setOnItemClickedListener((position -> songListInterface.OnSongSelectedListener(position)));
        songListAdapter.setOnItemOptionClickedListener((view, position, inQueue) -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), view);
            popupMenu.getMenuInflater().inflate(R.menu.songlist_item_more, popupMenu.getMenu());

            if (inQueue) {
                popupMenu.getMenu().getItem(0).setTitle("Remove from queue");
            }

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_songlist_item_add_to_queue) {
                    List<Track> track = new ArrayList<>();
                    track.add(songList.get(position).getTrack());

                    if (!inQueue) {
                        songInterface.onAddSongsToSonglistListener(track, false);
                        playbackControlInterface.onPlaybackBehaviourChangeListener(PlaybackBehaviour.PlaybackBehaviourState.REPEAT_LIST);
                    } else {
                        songInterface.onSongsRemoveListener(track);
                    }
                } else if (item.getItemId() == R.id.action_songlist_item_play_next) {
                    List<Track> track = new ArrayList<>();
                    track.add(songList.get(position).getTrack());
                    songInterface.onAddSongsToSonglistListener(track, true);
                    playbackControlInterface.onPlaybackBehaviourChangeListener(PlaybackBehaviour.PlaybackBehaviourState.REPEAT_LIST);
                } else if (item.getItemId() == R.id.action_songlist_item_new_queue) {
                    List<Track> track = new ArrayList<>();
                    track.add(songList.get(position).getTrack());
                    songInterface.onRemoveAllSongsListener();
                    songInterface.onAddSongsToSonglistListener(track, true);
                    playbackControlInterface.onPlaybackBehaviourChangeListener(PlaybackBehaviour.PlaybackBehaviourState.REPEAT_LIST);
                }
                return false;
            });

            popupMenu.show();
        });

        listView.setAdapter(songListAdapter);

        shuffle.setOnClickListener(view -> {
            if (!isSonglistSet) {
                List<Track> songListAsTracks = songList.stream().map(TrackDTO::getTrack).collect(Collectors.toList());
                songInterface.onSongListCreatedListener(songListAsTracks, DashboardListType.TRACK);
                isSonglistSet = true;
            }

            playbackControlInterface.onPlaybackBehaviourChangeListener(PlaybackBehaviour.PlaybackBehaviourState.SHUFFLE);
            playbackControlInterface.onNextClickListener();
        });

        filter.setOnClickListener(view -> {
            View menuLayout = inflater.inflate(R.layout.songlist_filter_menu, null);
            int wrapContent = LinearLayout.LayoutParams.WRAP_CONTENT;
            menuLayout.setAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.popupwindow_show));
            PopupWindow filterMenu = new PopupWindow(menuLayout, wrapContent, wrapContent, true);

            menuLayout.findViewById(R.id.songlist_filter_menu_az).setOnClickListener(view1 -> {
                musicplayerViewModel.getTrackListByFilter(listFilterType).removeObservers(getViewLifecycleOwner());
                listFilterType = ListFilterType.ALPHABETICAL;
                onMenuClick(filterMenu);});

            menuLayout.findViewById(R.id.songlist_filter_menu_lastplayed).setOnClickListener(view1 -> {
                musicplayerViewModel.getTrackListByFilter(listFilterType).removeObservers(getViewLifecycleOwner());
                listFilterType = ListFilterType.LAST_PLAYED;
                onMenuClick(filterMenu);});

            menuLayout.findViewById(R.id.songlist_filter_menu_timeplayed).setOnClickListener(view1 -> {
                musicplayerViewModel.getTrackListByFilter(listFilterType).removeObservers(getViewLifecycleOwner());
                listFilterType = ListFilterType.TIME_PLAYED;
                onMenuClick(filterMenu);});

            menuLayout.findViewById(R.id.songlist_filter_menu_timesplayed).setOnClickListener(view1 -> {
                musicplayerViewModel.getTrackListByFilter(listFilterType).removeObservers(getViewLifecycleOwner());
                listFilterType = ListFilterType.TIMES_PLAYED;
                onMenuClick(filterMenu);});

            menuLayout.findViewById(R.id.songlist_filter_menu_created).setOnClickListener(view1 -> {
                musicplayerViewModel.getTrackListByFilter(listFilterType).removeObservers(getViewLifecycleOwner());
                listFilterType = ListFilterType.LAST_CREATED;
                onMenuClick(filterMenu);});

            filterMenu.setContentView(menuLayout);
            filterMenu.setOutsideTouchable(true);
            filterMenu.showAsDropDown(filter);
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                songListAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        loadSonglist();
        return view;
    }

    private void onMenuClick(PopupWindow filterMenu) {
        filterTitle.setText(listFilterType.getId());
        listChange = true;
        loadSonglist();
        filterMenu.dismiss();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getResources().getString(R.string.preference_songlist_filter_type), listFilterType.getFilterType());
        editor.apply();
    }

    private void loadSonglist() {
        musicplayerViewModel.getTrackListByFilter(listFilterType).observe(getViewLifecycleOwner(), tracks -> {
            ArrayList<TrackDTO> oldList = new ArrayList<>(this.songList);
            this.songList.clear();
            this.songList.addAll(tracks);

            if (firstLoad) {
                songListAdapter.notifyItemRangeInserted(0, tracks.size());
                int time = tracks.stream().map(TrackDTO::getTrack).map(Track::getTDuration).reduce(0, Integer::sum);
                shuffle_size.setText(tracks.size() + " songs - " + GeneralUtils.convertTimeWithUnit(time));
                firstLoad = false;
                songListAdapter.getAllBackgroundImages(tracks);
            }

            if (listFilterType.equals(ListFilterType.ALPHABETICAL)) {
                sideIndex.setVisible();
                sideIndex.setIndexListDTO(songList).displayIndex();
            } else if (listFilterType.equals(ListFilterType.LAST_PLAYED)
                    || listFilterType.equals(ListFilterType.TIMES_PLAYED)
                    || listFilterType.equals(ListFilterType.TIME_PLAYED)) {
                sideIndex.setVisibilityGone();
                if (oldList.size() == tracks.size()) {
                    int toPos = 0, targetId = -1;
                    for (int i = 0; i < oldList.size(); i++) {
                        if (!tracks.get(i).getTrack().getTId().equals(oldList.get(i).getTrack().getTId())) {
                            toPos = i;
                            targetId = tracks.get(i).getTrack().getTId();
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

                        songListAdapter.notifyItemMoved(fromPos, toPos);

                        if (toPos == 0) {
                            listViewManager.scrollToPosition(0);
                        }
                    }
                }
            }

            songListAdapter.setListFilterType(listFilterType);
            if (listChange) {
                listViewManager.scrollToPosition(0);
                songListAdapter.notifyDataSetChanged();
                listChange = false;
            } else {
                songListAdapter.notifyItemRangeChanged(0, tracks.size() - 1, "");
            }
        });
    }

    @Override
    public void OnSongSelectedListener(int index) {
        if (!isSonglistSet) {
            List<Track> songListAsTracks = songList.stream().map(TrackDTO::getTrack).collect(Collectors.toList());
            songInterface.onSongListCreatedListener(songListAsTracks, DashboardListType.TRACK);
            isSonglistSet = true;
        }

        songInterface.onSongSelectedListener(songList.get(index).getTrack());
    }

    private void jumpToCurrentPlayingSong() {
        int targetIndex = -1;
        for (int i = 0; i < songList.size(); i++) {
            Track track = songList.get(i).getTrack();
            if (track.getTId().equals(currentPlayingSongId)) {
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
                currentPlayingSongId = bundle.getInt("ID", -1);
                songListAdapter.setCurrentPlayingIndex(currentPlayingSongId);
            } else if (intent.getAction().equals(getResources().getString(R.string.playback_control_values))) {
                Bundle bundle = intent.getExtras();
                currentPlayingSongId = (int) bundle.getLong("ID", -1);
                songListAdapter.setCurrentPlayingIndex(currentPlayingSongId);
                ArrayList<Track> t = bundle.getParcelableArrayList(getResources().getString(R.string.parcelable_track_list));
                songListAdapter.setPlaybackBehaviour(PlaybackBehaviour.getStateFromInteger(bundle.getInt("BEHAVIOUR_STATE")));
                songListAdapter.setQueueList(t);
            }
        }
    };
}
