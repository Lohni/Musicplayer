package com.example.musicplayer.ui.album;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.musicplayer.R;
import com.example.musicplayer.adapter.AlbumDetailAdapter;
import com.example.musicplayer.entities.AlbumResolver;
import com.example.musicplayer.entities.MusicResolver;
import com.example.musicplayer.utils.ListToQueueAnimation;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.widget.LinearLayout.VERTICAL;

public class AlbumDetailFragment extends Fragment implements AlbumDetailAdapter.AlbumDetailAdapterListener{

    private TextView albumName, albumSize, albumArtist;
    private ImageView albumCover;
    private RecyclerView albumDetailList;
    private ImageButton albumDetailPlay, albumDetailShuffle;

    private Bitmap albumCoverBitmap;
    private String albumNameString, albumSizeString;

    private AlbumResolver albumResolver;
    private AlbumViewModel albumViewModel;
    private ArrayList<MusicResolver> albumSongs = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;

    private int[] queueDest;
    private AlbumFragment.AlbumListener albumListener;

    public interface AlbumDetailListener{
        void onAlbumTrackClickedListener(int position, ArrayList<MusicResolver> albumTracks, boolean shuffle);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        albumListener = (AlbumFragment.AlbumListener) context;
    }

    public AlbumDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postponeEnterTransition();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_album_detail, container, false);

        albumName = view.findViewById(R.id.album_detail_name);
        albumSize = view.findViewById(R.id.album_detail_size);
        albumCover = view.findViewById(R.id.album_detail_cover);
        albumArtist = view.findViewById(R.id.album_detail_artist);
        albumDetailList = view.findViewById(R.id.album_detail_list);
        albumDetailPlay = view.findViewById(R.id.album_detail_play);
        albumDetailShuffle = view.findViewById(R.id.album_detail_shuffle);

        if (albumCoverBitmap != null){
            albumCover.setImageBitmap(albumCoverBitmap);
        } else {
            albumCover.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_album_black_24dp, null));
        }

        albumName.setText(albumNameString);
        albumSize.setText(albumSizeString);
        albumArtist.setText(albumResolver.getArtistName());

        albumDetailList.setLayoutManager(linearLayoutManager = new LinearLayoutManager(requireContext()));
        albumDetailList.setHasFixedSize(true);

        albumViewModel = new ViewModelProvider(requireActivity()).get(AlbumViewModel.class);
        albumDetailList.setAdapter(new AlbumDetailAdapter(requireContext(), this.albumSongs, albumCoverBitmap, this));
        albumViewModel.getAllAlbumSongs(albumResolver.getAlbumId()).observe(getViewLifecycleOwner(), albumSongs -> {
            this.albumSongs.addAll(albumSongs);
            albumDetailList.getAdapter().notifyItemRangeInserted(0, albumSongs.size());

        });

        albumDetailPlay.setOnClickListener((button -> {
            animateListContent();
            albumListener.onPlayAlbumListener(0, albumSongs, false);
        }));
        albumDetailShuffle.setOnClickListener((button) -> {
            animateListContent();
            albumListener.onPlayAlbumListener(0, albumSongs, true);
        });

        return view;
    }

    private void animateListContent(){
        int firstIndex = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
        int lastIndex = linearLayoutManager.findLastVisibleItemPosition();
        ArrayList<View> animatedViews = new ArrayList<>();
        for (int index = firstIndex; index <= lastIndex; index++){
            AlbumDetailAdapter.ViewHolder holder = (AlbumDetailAdapter.ViewHolder) albumDetailList.findViewHolderForLayoutPosition(index);
            animatedViews.add(holder.albumCover);
        }

        new ListToQueueAnimation().attachActivity(requireActivity()).setTargetView(animatedViews).setDestCoord(queueDest).startAnimation();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startPostponedEnterTransition();
    }

    public void setViewTransitionValues(Bitmap albumCoverBitmap, String albumNameString, String albumSizeString){
        this.albumCoverBitmap = albumCoverBitmap;
        this.albumNameString = albumNameString;
        this.albumSizeString = albumSizeString;
    }

    public void setQueueDest(int[] dest){
        queueDest = dest;
    }

    public void setAlbumResolver(AlbumResolver albumResolver){
        this.albumResolver = albumResolver;
    }

    @Override
    public void onItemClickListener(int position) {
        albumListener.onPlayAlbumListener(position, albumSongs, false);
    }
}