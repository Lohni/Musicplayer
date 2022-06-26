package com.example.musicplayer.ui.album;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.transition.Fade;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.musicplayer.R;
import com.example.musicplayer.adapter.AlbumAdapter;
import com.example.musicplayer.adapter.MediaOptionsAdapter;
import com.example.musicplayer.entities.AlbumResolver;
import com.example.musicplayer.entities.MusicResolver;
import com.example.musicplayer.transition.AlbumDetailTransition;
import com.example.musicplayer.utils.AlbumOptions;
import com.example.musicplayer.utils.Permissions;

import java.util.ArrayList;

public class AlbumFragment extends Fragment implements AlbumAdapter.AlbumAdapterCallback, MediaOptionsAdapter.MediaOptionsAdapterListener {

    private RecyclerView albumList;
    private ArrayList<AlbumResolver> albumItems = new ArrayList<>();
    private RecyclerView.LayoutManager layoutManager;
    private AlbumViewModel albumViewModel;
    private AlbumListener albumListener;

    private int sharedElementsViewPosition = -1;

    private int[] queueDest;
    public AlbumFragment() {
        // Required empty public constructor
    }

    public interface AlbumListener {
        void onPlayAlbumListener(int position ,ArrayList<MusicResolver> albumTrackList, boolean shuffle);
        void onQueueAlbumListener(ArrayList<MusicResolver> albumTrackList);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postponeEnterTransition();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            albumListener = (AlbumListener) context;
        } catch (ClassCastException ignored){

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_album, container, false);

        albumList = view.findViewById(R.id.album_albumList);

        layoutManager  = new GridLayoutManager(requireContext(), 2);
        layoutManager.setAutoMeasureEnabled(false);
        albumList.setHasFixedSize(true);
        albumList.setNestedScrollingEnabled(false);
        albumViewModel = new ViewModelProvider(requireActivity()).get(AlbumViewModel.class);
        AlbumAdapter adapter = new AlbumAdapter(requireContext(), albumItems, this,this , sharedElementsViewPosition);
        adapter.setHasStableIds(true);
        albumList.setAdapter(adapter);
        albumList.setLayoutManager(layoutManager);

        if (Permissions.permission(requireActivity(), this, Manifest.permission.READ_EXTERNAL_STORAGE)){
            albumViewModel.getAllAlbums().observe(getViewLifecycleOwner(), albumResolvers -> {
                albumItems.clear();
                albumItems.addAll(albumResolvers);
                adapter.notifyItemRangeInserted(0, albumItems.size());
            });
        }

        return  view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //startPostponedEnterTransition();
    }

    @Override
    public void onLayoutClickListener(AlbumAdapter.ViewHolder holder, AlbumResolver albumResolver, int position) {
        sharedElementsViewPosition = position;
        AlbumDetailFragment albumDetailFragment = new AlbumDetailFragment();
        albumDetailFragment.setSharedElementEnterTransition(new AlbumDetailTransition());
        albumDetailFragment.setSharedElementReturnTransition(new AlbumDetailTransition());

        Bitmap coverImage = null;

        try {
            coverImage = ((BitmapDrawable)holder.albumCover.getDrawable()).getBitmap();
        } catch (ClassCastException ignored){

        }
        albumDetailFragment.setViewTransitionValues(coverImage,
                holder.albumName.getText().toString(), holder.albumSize.getText().toString());
        albumDetailFragment.setAlbumResolver(albumResolver);
        albumDetailFragment.setQueueDest(queueDest);
        albumDetailFragment.setEnterTransition(new Fade());
        setExitTransition(new Fade());
        requireActivity().getSupportFragmentManager().beginTransaction()
                .addSharedElement(holder.albumCover, getResources().getString(R.string.transition_album_cover))
                .addSharedElement(holder.albumName, getResources().getString(R.string.transition_album_name))
                .addSharedElement(holder.albumSize, getResources().getString(R.string.transition_album_size))
                .replace(R.id.nav_host_fragment, albumDetailFragment)
                .addToBackStack(null).commit();
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
    public void onItemClickListener(int action, int albumPosition) {
        AlbumAdapter adapter = (AlbumAdapter) albumList.getAdapter();
        adapter.closePopupWindow((AlbumAdapter.ViewHolder) albumList.findViewHolderForAdapterPosition(albumPosition));
        AlbumResolver resolver = adapter.getItem(albumPosition);
        albumViewModel.getAllAlbumSongs(resolver.getAlbumId()).observe(getViewLifecycleOwner(), albumTrackList -> {
            switch (action){
                case AlbumOptions.OPTION_PLAY:{
                    albumListener.onPlayAlbumListener(0, albumTrackList, false);
                    break;
                }
                case AlbumOptions.OPTION_SHUFFLE:{
                    albumListener.onPlayAlbumListener(0, albumTrackList, true);
                    break;
                }
                case AlbumOptions.OPTION_QUEUE:{
                    albumListener.onQueueAlbumListener(albumTrackList);
                    break;
                }
            }
        });
    }
}