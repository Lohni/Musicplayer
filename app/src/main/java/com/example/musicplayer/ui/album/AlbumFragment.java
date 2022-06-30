package com.example.musicplayer.ui.album;

import android.content.Context;
import android.os.Bundle;
import android.transition.Fade;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.musicplayer.R;
import com.example.musicplayer.adapter.AlbumAdapter;
import com.example.musicplayer.adapter.MediaOptionsAdapter;
import com.example.musicplayer.database.MusicplayerApplication;
import com.example.musicplayer.database.dao.MusicplayerDataAccess;
import com.example.musicplayer.database.entity.Album;
import com.example.musicplayer.database.viewmodel.MusicplayerViewModel;
import com.example.musicplayer.entities.MusicResolver;
import com.example.musicplayer.transition.AlbumDetailTransition;
import com.example.musicplayer.utils.NavigationControlInterface;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.ActivityNavigatorDestinationBuilder;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AlbumFragment extends Fragment implements AlbumAdapter.AlbumAdapterCallback, MediaOptionsAdapter.MediaOptionsAdapterListener {

    private RecyclerView albumList;
    private ArrayList<Album> albumItems = new ArrayList<>();
    private RecyclerView.LayoutManager layoutManager;
    private MusicplayerViewModel musicplayerViewModel;
    private NavigationControlInterface navigationControlInterface;

    private int sharedElementsViewPosition = -1;

    public AlbumFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postponeEnterTransition();

        MusicplayerDataAccess mda = ((MusicplayerApplication) requireActivity().getApplication()).getDatabase().musicplayerDao();
        musicplayerViewModel = new ViewModelProvider(this, new MusicplayerViewModel.MusicplayerViewModelFactory(mda)).get(MusicplayerViewModel.class);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            navigationControlInterface = (NavigationControlInterface) context;
        } catch (ClassCastException ignored) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_album, container, false);

        albumList = view.findViewById(R.id.album_albumList);

        navigationControlInterface.setHomeAsUpEnabled(false);
        navigationControlInterface.isDrawerEnabledListener(true);
        navigationControlInterface.setToolbarTitle("Album");

        layoutManager = new GridLayoutManager(requireContext(), 2);
        layoutManager.setAutoMeasureEnabled(false);
        albumList.setHasFixedSize(true);
        albumList.setNestedScrollingEnabled(false);

        AlbumAdapter adapter = new AlbumAdapter(requireContext(), albumItems, this, this, sharedElementsViewPosition);
        adapter.setHasStableIds(true);
        albumList.setAdapter(adapter);
        albumList.setLayoutManager(layoutManager);

        musicplayerViewModel.getAllAlbums().observe(getViewLifecycleOwner(), albums -> {
            albumItems.clear();
            albumItems.addAll(albums);
            adapter.notifyItemRangeInserted(0, albumItems.size());
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startPostponedEnterTransition();
    }

    @Override
    public void onLayoutClickListener(AlbumAdapter.ViewHolder holder, Album albumResolver, int position) {
        sharedElementsViewPosition = position;
        AlbumDetailFragment albumDetailFragment = new AlbumDetailFragment();
        albumDetailFragment.setSharedElementEnterTransition(new AlbumDetailTransition());
        albumDetailFragment.setSharedElementReturnTransition(new AlbumDetailTransition());

        Bundle bundle = new Bundle();
        bundle.putInt("ALBUM_ID", albumResolver.getAId());
        albumDetailFragment.setArguments(bundle);

        albumDetailFragment.setEnterTransition(new Fade());
        setExitTransition(new Fade());
        requireActivity().getSupportFragmentManager().beginTransaction()
                .addSharedElement(holder.albumCover, getResources().getString(R.string.transition_album_cover))
                .addSharedElement(holder.albumName, getResources().getString(R.string.transition_album_name))
                .addSharedElement(holder.albumSize, getResources().getString(R.string.transition_album_size))
                .replace(R.id.nav_host_fragment, albumDetailFragment)
                .addToBackStack("ALBUM_FRAGMENT").commit();
    }

    @Override
    public void onSharedElementsViewCreated() {
        startPostponedEnterTransition();
    }

    @Override
    public void onDestroy() {
        albumList.setAdapter(null);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        postponeEnterTransition();
    }

    @Override
    public void onItemClickListener(int action, int albumPosition) {
        AlbumAdapter adapter = (AlbumAdapter) albumList.getAdapter();
        adapter.closePopupWindow((AlbumAdapter.ViewHolder) albumList.findViewHolderForAdapterPosition(albumPosition));
        Album resolver = adapter.getItem(albumPosition);
        //albumViewModel.getAllAlbumSongs(resolver.getAlbumId()).observe(getViewLifecycleOwner(), albumTrackList -> {
        //    switch (action){
        //        case AlbumOptions.OPTION_PLAY:{
        //            albumListener.onPlayAlbumListener(0, albumTrackList, false);
        //            break;
        //        }
        //        case AlbumOptions.OPTION_SHUFFLE:{
        //            albumListener.onPlayAlbumListener(0, albumTrackList, true);
        //            break;
        //        }
        //        case AlbumOptions.OPTION_QUEUE:{
        //            albumListener.onQueueAlbumListener(albumTrackList);
        //            break;
        //        }
        //    }
        //});
    }
}