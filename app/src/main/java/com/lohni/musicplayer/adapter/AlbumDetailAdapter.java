package com.lohni.musicplayer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.lohni.musicplayer.R;
import com.lohni.musicplayer.database.entity.Track;
import com.lohni.musicplayer.utils.AdapterUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AlbumDetailAdapter extends RecyclerView.Adapter<AlbumDetailAdapter.ViewHolder> {
    private final ArrayList<Track> albumSongList;
    private final Drawable customCoverDrawable;
    private final AlbumDetailAdapterListener albumDetailAdapterListener;
    private HashMap<Integer, Drawable> drawableHashMap = new HashMap<>();

    public interface AlbumDetailAdapterListener {
        void onItemClickListener(int position);
    }

    public AlbumDetailAdapter(Context context, ArrayList<Track> albumSongList, AlbumDetailAdapterListener albumDetailAdapterListener) {
        this.albumSongList = albumSongList;
        this.albumDetailAdapterListener = albumDetailAdapterListener;
        customCoverDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_album_black_24dp, null);
    }

    @NonNull
    @Override
    public AlbumDetailAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_detail_item, parent, false);
        return new AlbumDetailAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumDetailAdapter.ViewHolder holder, int position) {
        Track track = albumSongList.get(position);
        holder.trackName.setText(track.getTTitle());
        holder.trackArtist.setText(track.getTArtist());
        holder.trackNr.setText(String.valueOf(track.getTTrackNr()));
        holder.trackCover.setForeground(drawableHashMap.getOrDefault(track.getTId(), customCoverDrawable));
        holder.constraintLayout.setOnClickListener(view -> albumDetailAdapterListener.onItemClickListener(position));
    }

    @Override
    public int getItemCount() {
        return albumSongList.size();
    }

    public void setDrawableHashMap(HashMap<Integer, Drawable> drawableHashMap) {
        this.drawableHashMap = drawableHashMap;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView trackNr, trackName, trackArtist;
        public View trackCover;
        private final ConstraintLayout constraintLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.trackNr = itemView.findViewById(R.id.album_detail_item_tracknr);
            this.trackName = itemView.findViewById(R.id.album_detail_item_title);
            this.trackCover = itemView.findViewById(R.id.album_detail_item_cover);
            this.trackArtist = itemView.findViewById(R.id.album_detail_item_artist);
            this.constraintLayout = itemView.findViewById(R.id.album_detail_item_layout);
        }

    }

}
