package com.example.musicplayer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.musicplayer.R;
import com.example.musicplayer.entities.MusicResolver;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

public class AlbumDetailAdapter extends RecyclerView.Adapter<AlbumDetailAdapter.ViewHolder> {
    private ArrayList<MusicResolver> albumSongList;
    private Context context;
    private Bitmap albumCover;
    private Drawable customCoverDrawable;
    private AlbumDetailAdapterListener albumDetailAdapterListener;
    public interface AlbumDetailAdapterListener{
        void onItemClickListener(int position);
    }

    public AlbumDetailAdapter(Context context, ArrayList<MusicResolver> albumSongList, Bitmap albumCover, AlbumDetailAdapterListener albumDetailAdapterListener){
        this.context = context;
        this.albumSongList = albumSongList;
        this.albumCover = albumCover;
        this.albumDetailAdapterListener = albumDetailAdapterListener;
        customCoverDrawable = ResourcesCompat.getDrawable(context.getResources(),R.drawable.ic_album_black_24dp, null);
    }

    @NonNull
    @Override
    public AlbumDetailAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_detail_item, parent, false);
        return new AlbumDetailAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumDetailAdapter.ViewHolder holder, int position) {
        MusicResolver musicResolver = albumSongList.get(position);
        holder.trackName.setText(musicResolver.getTitle());
        holder.trackArtist.setText(musicResolver.getArtist());
        if (musicResolver.getTrackNr() > 0){
            holder.trackNr.setText(String.valueOf(musicResolver.getTrackNr()));
        } else {
            holder.trackNr.setText("-");
        }

        if (albumCover != null){
            holder.albumCover.setImageBitmap(albumCover);
        } else {
            holder.albumCover.setImageDrawable(customCoverDrawable);
        }
        holder.constraintLayout.setOnClickListener((view -> {
            albumDetailAdapterListener.onItemClickListener(position);
        }));
    }

    @Override
    public int getItemCount() {
        return albumSongList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView trackNr, trackName, trackArtist;
        public ImageView albumCover;
        private ConstraintLayout constraintLayout;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            this.trackNr = itemView.findViewById(R.id.album_detail_item_tracknr);
            this.trackName = itemView.findViewById(R.id.album_detail_item_title);
            this.albumCover = itemView.findViewById(R.id.album_detail_item_cover);
            this.trackArtist = itemView.findViewById(R.id.album_detail_item_artist);
            this.constraintLayout = itemView.findViewById(R.id.album_detail_item_layout);
        }

    }

}
