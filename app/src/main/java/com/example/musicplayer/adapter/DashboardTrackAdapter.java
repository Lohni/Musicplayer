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
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.musicplayer.R;
import com.example.musicplayer.database.dto.TrackDTO;
import com.example.musicplayer.database.entity.Track;
import com.example.musicplayer.interfaces.SongInterface;
import com.example.musicplayer.utils.GeneralUtils;
import com.example.musicplayer.utils.enums.DashboardListType;
import com.example.musicplayer.utils.enums.ListFilterType;
import com.example.musicplayer.utils.images.ImageTransformUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

public class DashboardTrackAdapter extends RecyclerView.Adapter<DashboardTrackAdapter.ViewHolder> {
    private final List<TrackDTO> trackList;
    private final Drawable customCoverDrawable, backgroundDrawable;
    private final Context context;
    private SongInterface songInterface;
    private ListFilterType listFilterType;
    private int imagesLoading = 0;

    public DashboardTrackAdapter(Context context, List<TrackDTO> trackList, SongInterface songInterface, ListFilterType listFilterType) {
        this.trackList = trackList;
        this.context = context;
        this.songInterface = songInterface;
        this.listFilterType = listFilterType;
        customCoverDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_baseline_music_note_24, null);
        backgroundDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.background_button_secondary, null);
    }

    @NonNull
    @Override
    public DashboardTrackAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DashboardTrackAdapter.ViewHolder holder, int position) {
        int pos = holder.getAbsoluteAdapterPosition();
        holder.position = pos;
        TrackDTO trackDTO = trackList.get(position);
        Track track = trackDTO.getTrack();
        holder.title.setText(track.getTTitle());
        holder.subTitle.setText(track.getTArtist());
        holder.description.setText(getDescription(trackList.get(position)));

        if (!holder.isRefresh) {
            holder.imageView.setBackground(backgroundDrawable);
            holder.imageView.setForeground(customCoverDrawable);
            holder.imageView.setForegroundTintList(ContextCompat.getColorStateList(context, R.color.colorOnSecondaryContainer));
        }

        holder.description.postDelayed(() -> notifyItemChanged(holder.position, ""), 10000);

        if (!holder.isRefresh) {
            imagesLoading++;
            holder.imageView.postDelayed(() -> {
                if (holder.position == position) {
                    Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, trackList.get(pos).getTrack().getTId());
                    byte[] thumbnail = null;
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    try {
                        mmr.setDataSource(context, trackUri);
                        thumbnail = mmr.getEmbeddedPicture();
                    } catch (IllegalArgumentException e) {
                        System.out.println("MediaMetadataRetriever IllegalArgument");
                    } finally {
                        try {
                            mmr.release();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (thumbnail != null) {
                            Bitmap cover = BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length);
                            holder.imageView.setClipToOutline(true);
                            holder.imageView.setBackground(ImageTransformUtil.roundCorners(cover, context.getResources()));
                            holder.imageView.setForeground(null);
                            Animation fadeIn = new AlphaAnimation(0, 1);
                            fadeIn.setInterpolator(new DecelerateInterpolator());
                            fadeIn.setDuration(350);
                            holder.imageView.setAnimation(fadeIn);
                        }
                    }
                }
                imagesLoading--;
            }, 300 + (20L * imagesLoading));
        }

        holder.root.setOnClickListener((view -> {
            int absPos = holder.getBindingAdapterPosition();
            Track track1 = trackList.get(absPos).getTrack();
            ArrayList<Track> tracks = new ArrayList<>();
            tracks.add(track1);
            songInterface.onSongListCreatedListener(tracks, DashboardListType.TRACK);
            songInterface.onSongSelectedListener(track);
        }));

        if (holder.isRefresh) holder.isRefresh = false;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty() && (payloads.get(0)).equals("")) {
            holder.isRefresh = true;
            //Todo: Overlapping postDelayed/Scrolling -> try to find out if currently scrolling
        } else {
            holder.isRefresh = false;
        }
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public int getItemCount() {
        return trackList.size();
    }

    private String getDescription(TrackDTO trackDTO) {
        if (trackDTO.getSize() != null || listFilterType.equals(ListFilterType.LAST_CREATED)) {
            if (listFilterType.equals(ListFilterType.LAST_PLAYED)) {
                LocalDateTime ldt = LocalDateTime.parse(trackDTO.getSize(), GeneralUtils.DB_TIMESTAMP);
                return GeneralUtils.getTimeDiffAsText(ldt);
            } else if (listFilterType.equals(ListFilterType.LAST_CREATED) && trackDTO.getTrack().getTCreated() != null) {
                LocalDateTime dbTime = LocalDateTime.parse(trackDTO.getTrack().getTCreated(), GeneralUtils.DB_TIMESTAMP);
                return GeneralUtils.getTimeDiffAsText(dbTime);
            } else if (listFilterType.equals(ListFilterType.TIMES_PLAYED)) {
                return trackDTO.getSize();
            } else if (listFilterType.equals(ListFilterType.TIME_PLAYED)) {
                try {
                    return GeneralUtils.convertTimeWithUnit(Integer.parseInt(trackDTO.getSize()));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return "";
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public FrameLayout imageView;
        public ConstraintLayout root;
        public TextView title;
        public TextView subTitle;
        public TextView description;
        int position = -1;
        public boolean isRefresh = false;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.dashboard_list_item_image);
            title = itemView.findViewById(R.id.dashboard_list_item_title);
            subTitle = itemView.findViewById(R.id.dashboard_list_item_subtitle);
            root = itemView.findViewById(R.id.dashboard_list_item_holder);
            description = itemView.findViewById(R.id.dashboard_list_item_desc);
        }
    }
}
