package com.lohni.musicplayer.ui.album;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Fade;

import com.lohni.musicplayer.R;
import com.lohni.musicplayer.adapter.AlbumAdapter;
import com.lohni.musicplayer.database.MusicplayerApplication;
import com.lohni.musicplayer.database.dao.MusicplayerDataAccess;
import com.lohni.musicplayer.database.dto.AlbumTrackDTO;
import com.lohni.musicplayer.database.viewmodel.MusicplayerViewModel;
import com.lohni.musicplayer.interfaces.NavigationControlInterface;
import com.lohni.musicplayer.interfaces.PlaybackControlInterface;
import com.lohni.musicplayer.interfaces.SongInterface;
import com.lohni.musicplayer.transition.AlbumDetailTransition;
import com.lohni.musicplayer.ui.views.SideIndex;
import com.lohni.musicplayer.utils.enums.DashboardListType;
import com.lohni.musicplayer.utils.enums.PlaybackBehaviour;

import java.util.ArrayList;

public class AlbumFragment extends Fragment {
    private RecyclerView albumList;
    private ArrayList<AlbumTrackDTO> albumItems = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private SideIndex sideIndex;

    private MusicplayerViewModel musicplayerViewModel;
    private NavigationControlInterface navigationControlInterface;
    private SongInterface songInterface;
    private PlaybackControlInterface playbackControlInterface;

    private boolean scrolling = false;

    public AlbumFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MusicplayerDataAccess mda = ((MusicplayerApplication) requireActivity().getApplication()).getDatabase().musicplayerDao();
        musicplayerViewModel = new ViewModelProvider(this, new MusicplayerViewModel.MusicplayerViewModelFactory(mda)).get(MusicplayerViewModel.class);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            navigationControlInterface = (NavigationControlInterface) context;
            songInterface = (SongInterface) context;
            playbackControlInterface = (PlaybackControlInterface) context;
        } catch (ClassCastException ignored) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album, container, false);
        albumList = view.findViewById(R.id.album_albumList);
        LinearLayout linearLayout = view.findViewById(R.id.album_side_index);
        FrameLayout indexZoomHolder = view.findViewById(R.id.album_indexzoom_holder);
        TextView indexZoom = view.findViewById(R.id.album_indexzoom);

        navigationControlInterface.setHomeAsUpEnabled(false);
        navigationControlInterface.isDrawerEnabledListener(true);
        navigationControlInterface.setToolbarTitle("Album");

        layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        albumList.setHasFixedSize(true);
        albumList.setNestedScrollingEnabled(false);

        sideIndex = new SideIndex(requireActivity(), linearLayout, indexZoomHolder, indexZoom, layoutManager);

        AlbumAdapter adapter = new AlbumAdapter(requireContext(), albumItems);
        adapter.setHasStableIds(true);
        adapter.setOnItemClickedListener((viewHolder, position) -> {
            Fade fade = new Fade();
            fade.setDuration(100);
            setExitTransition(fade);

            AlbumDetailFragment albumDetailFragment = new AlbumDetailFragment();
            albumDetailFragment.setSharedElementEnterTransition(new AlbumDetailTransition());
            albumDetailFragment.setExitTransition(fade);

            Integer aId = albumItems.get(position).album.getAId();
            Bundle bundle = new Bundle();
            bundle.putInt("ALBUM_ID", aId);
            adapter.getBitmapForAlbum(aId).ifPresent(bmp -> bundle.putParcelable("COVER", bmp));
            albumDetailFragment.setArguments(bundle);

            if (scrolling) {
                scrolling = false;
                navigationControlInterface.setToolbarBackground(false);
            }

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .addSharedElement(viewHolder.albumCover, getResources().getString(R.string.transition_album_cover))
                    .addSharedElement(viewHolder.albumName, getResources().getString(R.string.transition_album_name))
                    .addSharedElement(viewHolder.albumSize, getResources().getString(R.string.transition_album_size))
                    .addSharedElement(viewHolder.albumArtist, getResources().getString(R.string.transition_album_artist))
                    .addSharedElement(viewHolder.constraintLayout, getResources().getString(R.string.transition_album_layout))
                    .replace(R.id.nav_host_fragment, albumDetailFragment)
                    .addToBackStack("ALBUM_FRAGMENT")
                    .commit();
        });
        adapter.setOnItemOptionClickedListener((optionsView, position) -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), optionsView);
            popupMenu.getMenuInflater().inflate(R.menu.album_item_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_album_play) {
                    songInterface.onRemoveAllSongsListener();
                    songInterface.onSongListCreatedListener(albumItems.get(position).trackList, DashboardListType.ALBUM);
                    playbackControlInterface.onPlaybackBehaviourChangeListener(PlaybackBehaviour.PlaybackBehaviourState.REPEAT_LIST);
                    songInterface.onSongSelectedListener(albumItems.get(position).trackList.get(0));
                } else if (item.getItemId() == R.id.menu_album_queue) {
                    songInterface.onAddSongsToSonglistListener(albumItems.get(position).trackList, false);
                } else if (item.getItemId() == R.id.menu_album_shuffle) {
                    songInterface.onRemoveAllSongsListener();
                    songInterface.onSongListCreatedListener(albumItems.get(position).trackList, DashboardListType.ALBUM);
                    playbackControlInterface.onPlaybackBehaviourChangeListener(PlaybackBehaviour.PlaybackBehaviourState.SHUFFLE);
                    playbackControlInterface.onNextClickListener();
                }
                return false;
            });
            popupMenu.show();
        });

        albumList.setAdapter(adapter);
        albumList.setLayoutManager(layoutManager);

        musicplayerViewModel.getAllAlbumsWithTracks().observe(getViewLifecycleOwner(), albums -> {
            musicplayerViewModel.getAllAlbumsWithTracks().removeObservers(getViewLifecycleOwner());
            if (albumItems.size() == 0) {
                albumItems.addAll(albums);
                adapter.getAllBackgroundImages(albums, albumList);
                adapter.notifyItemRangeInserted(0, albumItems.size());
            }
            sideIndex.setIndexList(albumItems).displayIndex();
        });

        albumList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (!scrolling && layoutManager.findFirstCompletelyVisibleItemPosition() > 0) {
                    scrolling = true;
                    navigationControlInterface.setToolbarBackground(true);
                } else if (scrolling && layoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                    scrolling = false;
                    navigationControlInterface.setToolbarBackground(false);
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        albumList.setAdapter(null);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        if (albumList.getAdapter() != null) {
            ((AlbumAdapter) albumList.getAdapter()).getAllBackgroundImages(albumItems, albumList);
        }
        super.onResume();
    }
}