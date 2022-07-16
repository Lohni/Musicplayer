package com.example.musicplayer.adapter;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
import com.example.musicplayer.database.dto.TrackDTO;
import com.example.musicplayer.database.entity.Track;
import com.example.musicplayer.interfaces.SongInterface;
import com.example.musicplayer.utils.images.ImageTransformUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.RecyclerView;

public class DashboardTrackAdapter extends RecyclerView.Adapter<DashboardTrackAdapter.ViewHolder> {
    private final List<TrackDTO> trackList;
    private final Drawable customCoverDrawable;
    private final Context context;
    private SongInterface songInterface;
    private int imagesLoading = 0;

    public DashboardTrackAdapter(Context context, List<TrackDTO> trackList,SongInterface songInterface) {
        this.trackList = trackList;
        this.context = context;
        this.songInterface = songInterface;
        customCoverDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_baseline_music_note_24, null);
    }

    @NonNull
    @Override
    public DashboardTrackAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_list_item, parent, false);
        ViewHolder holder = new DashboardTrackAdapter.ViewHolder(v);
        holder.imageHolder.setBackgroundTintList(ContextCompat.getColorStateList(parent.getContext(), R.color.NewcolorTeritaryContainer));
        holder.imageView.setImageDrawable(customCoverDrawable);
        holder.imageView.setImageTintList(ContextCompat.getColorStateList(parent.getContext(), R.color.NewcolorOnTeritaryContainer));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull DashboardTrackAdapter.ViewHolder holder, int position) {
        Track track = trackList.get(position).getTrack();
        holder.title.setText(track.getTTitle());
        holder.subTitle.setText(track.getTArtist());
        holder.position = holder.getAbsoluteAdapterPosition();

        int pos = holder.getAbsoluteAdapterPosition();
        imagesLoading++;
        holder.imageView.postDelayed(() -> {
            if (holder.position == pos) {
                Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, track.getTId());
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                try {
                    mmr.setDataSource(context, trackUri);
                    byte[] thumbnail = mmr.getEmbeddedPicture();
                    if (thumbnail != null) {
                        Bitmap cover = BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length);
                        holder.imageHolder.setBackground(ImageTransformUtil.roundCorners(cover, context.getResources()));
                        holder.imageHolder.setBackgroundTintList(null);
                        holder.imageView.setClipToOutline(true);
                        holder.imageView.setImageDrawable(null);
                        Animation fadeIn = new AlphaAnimation(0, 1);
                        fadeIn.setInterpolator(new DecelerateInterpolator());
                        fadeIn.setDuration(350);
                        holder.imageView.setAnimation(fadeIn);
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("MediaMetadataRetriever IllegalArgument");
                } finally {
                    mmr.release();
                }
            }
            imagesLoading--;
        }, 300 + (30L * imagesLoading));

        holder.root.setOnClickListener((view -> {
            Track track1 =  trackList.get(position).getTrack();
            ArrayList<Track> tracks = new ArrayList<>();
            tracks.add(track1);
            songInterface.onAddSongsToSonglistListener(tracks);
            songInterface.onSongSelectedListener(track);
        }));
    }

    @Override
    public int getItemCount() {
        return trackList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public ConstraintLayout imageHolder, root;
        public TextView title;
        public TextView subTitle;
        int position;

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
