package com.example.musicplayer.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.musicplayer.R;
import com.example.musicplayer.database.entity.Playlist;
import com.example.musicplayer.database.dto.PlaylistDTO;
import com.example.musicplayer.ui.playlist.PlaylistInterface;
import com.example.musicplayer.utils.enums.DashboardFilterType;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

public class DashboardPlaylistAdapter extends RecyclerView.Adapter<DashboardPlaylistAdapter.ViewHolder> {
    private final List<PlaylistDTO> playlists;
    private final Drawable customCoverDrawable;
    private final DashboardFilterType type;
    private PlaylistInterface playlistInterface;

    public DashboardPlaylistAdapter(Context context, List<PlaylistDTO> playlists, DashboardFilterType type, PlaylistInterface playlistInterface) {
        this.playlists = playlists;
        this.type = type;
        this.playlistInterface = playlistInterface;
        customCoverDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_playlist_play_black_24dp, null);
    }

    @NonNull
    @Override
    public DashboardPlaylistAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_list_item, parent, false);
        DashboardPlaylistAdapter.ViewHolder holder = new DashboardPlaylistAdapter.ViewHolder(v);
        holder.imageHolder.setBackgroundTintList(ContextCompat.getColorStateList(parent.getContext(), R.color.NewcolorSecondaryContainer));
        holder.imageView.setImageDrawable(customCoverDrawable);
        holder.imageView.setImageTintList(ContextCompat.getColorStateList(parent.getContext(), R.color.NewcolorOnSecondaryContainer));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull DashboardPlaylistAdapter.ViewHolder holder, int position) {
        Playlist playlist = playlists.get(position).getPlaylist();
        holder.title.setText(playlist.getPName());
        holder.subTitle.setText(getSubtitleMessage(playlists.get(position).getSize()));
        holder.root.setOnClickListener((view) -> {
            playlistInterface.OnClickListener(playlists.get(position).getPlaylist().getPId(), view);
        });
    }

    private String getSubtitleMessage(Integer amount) {
        return (type.equals(DashboardFilterType.TIMES_PLAYED))
                ? amount + " times played"
                : amount + " songs";
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public ConstraintLayout imageHolder, root;
        public TextView title;
        public TextView subTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.dashboard_list_item_image);
            title = itemView.findViewById(R.id.dashboard_list_item_title);
            subTitle = itemView.findViewById(R.id.dashboard_list_item_subtitle);
            imageHolder = itemView.findViewById(R.id.dashboard_list_item_image_holder);
            root = itemView.findViewById(R.id.dashboard_list_item_holder);
        }
    }
}
