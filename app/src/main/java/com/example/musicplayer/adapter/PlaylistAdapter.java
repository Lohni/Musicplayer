package com.example.musicplayer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.musicplayer.R;
import com.example.musicplayer.database.entity.Playlist;
import com.example.musicplayer.ui.playlist.PlaylistDTO;
import com.example.musicplayer.ui.playlist.PlaylistInterface;

import java.util.ArrayList;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {
    private ArrayList<PlaylistDTO> playlistWithSize;
    private PlaylistInterface playlistInterface;
    private boolean onItemClickEnabled = true;

    public PlaylistAdapter(ArrayList<PlaylistDTO> playlistWithSize, PlaylistInterface playlistInterface) {
        this.playlistWithSize = playlistWithSize;
        this.playlistInterface = playlistInterface;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name.setText(playlistWithSize.get(position).getPlaylist().getPName());
        holder.size.setText(playlistWithSize.get(position).getSize().toString() + " songs");
        holder.itemView.setOnClickListener(view -> {
            if (onItemClickEnabled) {
                view.setTransitionName("playlist_detail");
                playlistInterface.OnClickListener(playlistWithSize.get(position).getPlaylist(), view);
            }
        });
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return playlistWithSize.size();
    }

    public void setOnItemClickEnabled(boolean onItemClickEnabled) {
        this.onItemClickEnabled = onItemClickEnabled;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, size;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.playlist_title);
            size = itemView.findViewById(R.id.playlist_size);
        }
    }
}
