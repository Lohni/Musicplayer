package com.lohni.musicplayer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.lohni.musicplayer.R;
import com.lohni.musicplayer.database.dto.AlbumTrackDTO;
import com.lohni.musicplayer.database.entity.Album;
import com.lohni.musicplayer.utils.images.ImageUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<AlbumTrackDTO> albumList;
    private final Drawable customCoverImage, customCoverBackground;
    private final HashMap<Integer, Drawable> drawableHashMap = new HashMap<>();

    private OnItemClickedListener onItemClickedListener;
    private OnItemOptionClickedListener onItemOptionClickedListener;

    public interface OnItemClickedListener {
        void onItemClicked(ViewHolder viewHolder, int position);
    }

    public interface OnItemOptionClickedListener {
        void onItemOptionClicked(View view, int position);
    }

    public AlbumAdapter(Context c, ArrayList<AlbumTrackDTO> albumList) {
        this.context = c;
        this.albumList = albumList;
        this.customCoverImage = ResourcesCompat.getDrawable(c.getResources(), R.drawable.ic_album_black_24dp, null);
        this.customCoverBackground = ResourcesCompat.getDrawable(context.getResources(), R.drawable.background_button_secondary, null);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_item, parent, false);
        ViewHolder holder = new ViewHolder(v);
        holder.albumCover.setBackground(customCoverBackground);
        holder.albumCover.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorSurfaceLevel4));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Album album = albumList.get(position).album;
        holder.albumName.setText(album.getAName());
        holder.albumArtist.setText(album.getAArtistName());
        holder.albumSize.setText(album.getANumSongs() + " songs");

        holder.albumOptions.setOnClickListener(view -> {
            if (onItemOptionClickedListener != null) {
                onItemOptionClickedListener.onItemOptionClicked(view, position);
            }
        });

        holder.constraintLayout.setOnClickListener((view -> {
            if (onItemClickedListener != null) {
                holder.albumCover.setTransitionName(context.getResources().getString(R.string.transition_album_cover));
                holder.albumName.setTransitionName(context.getResources().getString(R.string.transition_album_name));
                holder.albumSize.setTransitionName(context.getResources().getString(R.string.transition_album_size));
                holder.albumArtist.setTransitionName(context.getResources().getString(R.string.transition_album_artist));
                holder.constraintLayout.setTransitionName(context.getResources().getString(R.string.transition_album_layout));
                onItemClickedListener.onItemClicked(holder, position);
            }
        }));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        Album album = albumList.get(position).album;
        if (drawableHashMap.containsKey(album.getAId())) {
            holder.albumCover.setForeground(drawableHashMap.get(album.getAId()));
            holder.albumCover.setForegroundTintList(null);
        } else {
            holder.albumCover.setForeground(customCoverImage);
            holder.albumCover.setForegroundTintList(ContextCompat.getColorStateList(context, R.color.colorOnSurfaceVariant));
        }

        if (payloads.isEmpty() || !payloads.get(0).equals("RELOAD_IMAGES")) {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void setOnItemClickedListener(OnItemClickedListener onItemClickedListener) {
        this.onItemClickedListener = onItemClickedListener;
    }

    public void setOnItemOptionClickedListener(OnItemOptionClickedListener onItemOptionClickedListener) {
        this.onItemOptionClickedListener = onItemOptionClickedListener;
    }

    public void setDrawableHashMap(HashMap<Integer, Drawable> albumCovers) {
        this.drawableHashMap.clear();
        this.drawableHashMap.putAll(albumCovers);
    }

    public Optional<Bitmap> getBitmapForAlbum(Integer aId){
        if (drawableHashMap.containsKey(aId)) {
            Drawable d = drawableHashMap.get(aId);
            return Optional.of(ImageUtil.getBitmapFromDrawable(context, d));
        }
        return Optional.empty();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public MaterialTextView albumName, albumSize, albumArtist;
        private final ImageButton albumOptions;
        public View albumCover;
        public LinearLayout constraintLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            albumName = itemView.findViewById(R.id.album_item_title);
            albumSize = itemView.findViewById(R.id.album_item_size);
            albumArtist = itemView.findViewById(R.id.album_item_artist);
            albumCover = itemView.findViewById(R.id.album_item_cover);
            albumOptions = itemView.findViewById(R.id.album_item_more);
            constraintLayout = itemView.findViewById(R.id.album_motionlayout);
        }
    }
}
