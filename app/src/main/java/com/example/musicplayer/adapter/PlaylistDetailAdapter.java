package com.example.musicplayer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.musicplayer.R;
import com.example.musicplayer.entities.MusicResolver;
import com.example.musicplayer.ui.playlist.PlaylistInterface;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PlaylistDetailAdapter extends RecyclerView.Adapter<PlaylistDetailAdapter.ViewHolder> {

    private ArrayList<MusicResolver> trackList;
    private PlaylistInterface playlistInterface;

    public PlaylistDetailAdapter(ArrayList<MusicResolver> trackList, PlaylistInterface playlistInterface){
        this.trackList =trackList;
        this.playlistInterface=playlistInterface;
    }

    @NonNull
    @Override
    public PlaylistDetailAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.songlist_item,parent,false);
        PlaylistDetailAdapter.ViewHolder vh = new PlaylistDetailAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistDetailAdapter.ViewHolder holder, int position) {
        holder.title.setText(trackList.get(position).getTitle());
        holder.artist.setText(trackList.get(position).getArtist());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //playlistInterface.OnClickListener(holder.title.getText().toString());
                playlistInterface.OnPlaylistItemSelectedListener(position);
            }
        });
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return trackList.size();
    }

    public int getHolderPosition(MusicResolver title){
        return trackList.indexOf(title);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView title, artist;

        public ViewHolder(View itemView) {
            super(itemView);
            title=itemView.findViewById(R.id.song_title);
            artist=itemView.findViewById(R.id.song_artist);
        }
    }
}
