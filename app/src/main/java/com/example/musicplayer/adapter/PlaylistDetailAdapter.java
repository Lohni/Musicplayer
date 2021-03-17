package com.example.musicplayer.adapter;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
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
import com.example.musicplayer.entities.MusicResolver;
import com.example.musicplayer.ui.playlist.PlaylistInterface;
import com.example.musicplayer.ui.playlistdetail.OnStartDragListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class PlaylistDetailAdapter extends RecyclerView.Adapter<PlaylistDetailAdapter.ViewHolder> {

    private ArrayList<MusicResolver> trackList;
    private PlaylistInterface playlistInterface;
    private OnStartDragListener onStartDragListener;
    private Drawable customCover;
    private Context context;


    public PlaylistDetailAdapter(Context c, ArrayList<MusicResolver> trackList, PlaylistInterface playlistInterface, OnStartDragListener onStartDragListener){
        this.trackList =trackList;
        this.playlistInterface=playlistInterface;
        this.onStartDragListener = onStartDragListener;
        this.context = c;
        this.customCover = ResourcesCompat.getDrawable(c.getResources(),R.drawable.ic_baseline_music_note_24,null);
    }

    @NonNull
    @Override
    public PlaylistDetailAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_detail_item,parent,false);
        return new PlaylistDetailAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistDetailAdapter.ViewHolder holder, int position) {
        holder.title.setText(trackList.get(position).getTitle());
        holder.artist.setText(trackList.get(position).getArtist());
        holder.cover.setImageDrawable(customCover);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //playlistInterface.OnClickListener(holder.title.getText().toString());
                playlistInterface.OnPlaylistItemSelectedListener(position);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                onStartDragListener.onStartDrag(holder);
                return false;
            }
        });
        holder.cover.postDelayed(new Runnable() {
            @Override
            public void run() {
                Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,trackList.get(position).getId());
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(context,trackUri);
                byte [] thumbnail = mmr.getEmbeddedPicture();
                mmr.release();
                if (thumbnail != null){
                    holder.cover.setImageBitmap(BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length));
                }
            }
        },500);

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
        ImageView cover;

        public ViewHolder(View itemView) {
            super(itemView);
            title=itemView.findViewById(R.id.playlist_detail_title);
            artist=itemView.findViewById(R.id.playlist_detail_artist);
            cover = itemView.findViewById(R.id.playlist_detail_cover);
        }
    }
}
