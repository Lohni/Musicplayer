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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.musicplayer.R;
import com.example.musicplayer.database.entity.Track;
import com.example.musicplayer.ui.tagEditor.TagEditorInterface;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

public class TagEditorAdapter extends RecyclerView.Adapter<TagEditorAdapter.ViewHolder> {

    private ArrayList<Track> trackList;
    private Context context;
    private Drawable customDrawable, coverDrawable;
    private TagEditorInterface tagEditorInterface;

    private int imagesLoading = 0;

    public TagEditorAdapter(ArrayList<Track> trackList, Context context, TagEditorInterface tagEditorInterface) {
        this.trackList = trackList;
        this.context = context;
        customDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_baseline_music_note_24, null);
        this.tagEditorInterface = tagEditorInterface;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tageditor_item, parent, false);
        return new TagEditorAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Track track = trackList.get(position);
        holder.position = holder.getAbsoluteAdapterPosition();
        holder.artist.setText(track.getTArtist());
        holder.title.setText(track.getTTitle());
        holder.itemView.setOnClickListener(view -> tagEditorInterface.onTrackSelectedListener(track));
        holder.cover.setImageDrawable(customDrawable);

        int pos = holder.getAbsoluteAdapterPosition();
        imagesLoading++;
        holder.cover.postDelayed(() -> {
            if (holder.position == pos) {
                Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, track.getTId());
                byte[] thumbnail = null;
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                try {
                    mmr.setDataSource(context, trackUri);
                    thumbnail = mmr.getEmbeddedPicture();
                } catch (IllegalArgumentException e) {
                    System.out.println("MediaMetadataRetriever IllegalArgument");
                } finally {
                    mmr.release();
                    if (thumbnail != null) {
                        Bitmap cover = BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length);
                        //ImageTransformUtil.getRoundedCornerBitmap(cover, context.getResources())
                        holder.cover.setClipToOutline(true);
                        holder.cover.setImageBitmap(cover);
                        Animation fadeIn = new AlphaAnimation(0, 1);
                        fadeIn.setInterpolator(new DecelerateInterpolator());
                        fadeIn.setDuration(350);
                        holder.cover.setAnimation(fadeIn);
                    }
                }
            }
            imagesLoading--;
        }, 300 + (30L * imagesLoading));
    }

    @Override
    public int getItemCount() {
        return trackList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, artist;
        ImageView cover;
        int position;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tagEditorList_title);
            artist = itemView.findViewById(R.id.tagEditorList_artist);
            cover = itemView.findViewById(R.id.tagEditorList_cover);
        }
    }
}
