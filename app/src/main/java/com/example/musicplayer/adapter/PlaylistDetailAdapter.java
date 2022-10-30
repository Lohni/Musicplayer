package com.example.musicplayer.adapter;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.musicplayer.R;
import com.example.musicplayer.database.entity.Track;
import com.example.musicplayer.interfaces.OnStartDragListener;

import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

public class PlaylistDetailAdapter extends RecyclerView.Adapter<PlaylistDetailAdapter.ViewHolder> {

    private final PlaylistClickListener playlistClickListener;
    private final OnStartDragListener onStartDragListener;
    private final Drawable customCover;
    private final Context context;
    private final ArrayList<Track> itemList;

    public interface PlaylistClickListener {
        void onAdapterItemClickListener(int position);
    }

    public PlaylistDetailAdapter(Context c, ArrayList<Track> tracks, PlaylistClickListener playlistClickListener, OnStartDragListener onStartDragListener) {
        this.itemList = tracks;
        this.playlistClickListener = playlistClickListener;
        this.onStartDragListener = onStartDragListener;
        this.context = c;
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
        holder.title.setText(itemList.get(position).getTTitle());
        holder.artist.setText(itemList.get(position).getTArtist());
        holder.cover.setImageDrawable(customCover);
        holder.itemView.setOnClickListener(view -> {
            playlistClickListener.onAdapterItemClickListener(position);
        });

        holder.itemView.setOnLongClickListener(view -> {
            onStartDragListener.onStartDrag(holder);
            return false;
        });

        holder.cover.postDelayed(() -> {
            Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, itemList.get(position).getTId());
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(context, trackUri);
            byte[] thumbnail = mmr.getEmbeddedPicture();
            try {
                mmr.release();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (thumbnail != null) {
                holder.cover.setImageBitmap(BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length));
            }
        }, 500);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, artist;
        ImageView cover;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.playlist_detail_title);
            artist = itemView.findViewById(R.id.playlist_detail_artist);
            cover = itemView.findViewById(R.id.playlist_detail_cover);
        }
    }
}
