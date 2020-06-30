package com.example.musicplayer.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.musicplayer.R;
import com.example.musicplayer.ui.playlist.PlaylistInterface;

import java.util.ArrayList;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {
    private ArrayList<String> playlist,playlistsize;
    private PlaylistInterface playlistInterface;

    public PlaylistAdapter(ArrayList<String> tracklist, ArrayList<String> size, PlaylistInterface playlistInterface){
        playlist =tracklist;
        playlistsize=size;
        this.playlistInterface=playlistInterface;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_item,parent,false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name.setText(playlist.get(position));
        holder.size.setText(playlistsize.get(position) + " songs");
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setTransitionName("playlist_detail");
                playlistInterface.OnClickListener(holder.name.getText().toString(),view);
            }
        });
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return playlist.size();
    }

    public int getHolderPosition(String title){
        return playlist.indexOf(title);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView name, size;

        public ViewHolder(View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.playlist_title);
            size=itemView.findViewById(R.id.playlist_size);
        }
    }
}
