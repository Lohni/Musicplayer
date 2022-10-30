package com.example.musicplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.musicplayer.R;
import com.example.musicplayer.utils.enums.AlbumOptions;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MediaOptionsAdapter extends RecyclerView.Adapter<MediaOptionsAdapter.ViewHolder> {
    private ArrayList<String> optionsList;
    private final MediaOptionsAdapterListener mediaOptionsAdapterListener;
    private final int albumPosition;

    public interface MediaOptionsAdapterListener{
        void onItemClickListener(int action, int albumPosition);
    }

    public MediaOptionsAdapter(Context context, MediaOptionsAdapterListener mediaOptionsAdapterListener, int albumPosition){
        optionsList = AlbumOptions.getAlbumOptions(context);
        this.mediaOptionsAdapterListener = mediaOptionsAdapterListener;
        this.albumPosition = albumPosition;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_listitem, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mediaOption.setText(optionsList.get(position));
        holder.mediaOption.setOnClickListener((view -> {
            mediaOptionsAdapterListener.onItemClickListener(position, albumPosition);
        }));
    }

    @Override
    public int getItemCount() {
        return optionsList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView mediaOption;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mediaOption = itemView.findViewById(R.id.simple_listitem);
        }
    }

}
