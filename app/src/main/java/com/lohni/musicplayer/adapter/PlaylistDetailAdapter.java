package com.lohni.musicplayer.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lohni.musicplayer.R;
import com.lohni.musicplayer.database.dto.PlaylistItemDTO;
import com.lohni.musicplayer.database.entity.Track;
import com.lohni.musicplayer.interfaces.OnStartDragListener;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

public class PlaylistDetailAdapter extends RecyclerView.Adapter<PlaylistDetailAdapter.ViewHolder> {
    private final PlaylistClickListener playlistClickListener;
    private final OnStartDragListener onStartDragListener;
    private final Drawable customCover;
    private final ArrayList<PlaylistItemDTO> itemList;
    private HashMap<Integer, Drawable> drawableHashMap = new HashMap<>();

    public interface PlaylistClickListener {
        void onAdapterItemClickListener(int position);
    }

    public PlaylistDetailAdapter(Context c, ArrayList<PlaylistItemDTO> playlistItemList, PlaylistClickListener playlistClickListener, OnStartDragListener onStartDragListener) {
        this.itemList = playlistItemList;
        this.playlistClickListener = playlistClickListener;
        this.onStartDragListener = onStartDragListener;
        this.customCover = ResourcesCompat.getDrawable(c.getResources(), R.drawable.ic_baseline_music_note_24, null);
    }

    @NonNull
    @Override
    public PlaylistDetailAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_detail_item, parent, false);
        return new PlaylistDetailAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistDetailAdapter.ViewHolder holder, int position) {
        Track track = itemList.get(position).getTrack();
        holder.title.setText(track.getTTitle());
        holder.artist.setText(track.getTArtist());

        holder.itemView.setOnClickListener(view -> {
            playlistClickListener.onAdapterItemClickListener(position);
        });

        holder.itemView.setOnLongClickListener(view -> {
            onStartDragListener.onStartDrag(holder);
            return false;
        });

        holder.cover.setBackground(drawableHashMap.getOrDefault(track.getTId(), customCover));
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void setDrawableHashMap(HashMap<Integer, Drawable> drawableHashMap) {
        this.drawableHashMap = drawableHashMap;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, artist;
        View cover;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.playlist_detail_title);
            artist = itemView.findViewById(R.id.playlist_detail_artist);
            cover = itemView.findViewById(R.id.playlist_detail_cover);
        }
    }
}
