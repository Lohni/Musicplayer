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
import android.widget.TextView;

import com.example.musicplayer.R;
import com.example.musicplayer.database.entity.Track;
import com.example.musicplayer.interfaces.SongInterface;
import com.example.musicplayer.utils.enums.PlaybackBehaviour;
import com.example.musicplayer.utils.images.ImageUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.ViewHolder> {
    private ArrayList<Track> queueList;
    private int queuePosition;
    private Context context;
    private PlaybackBehaviour.PlaybackBehaviourState playbackBehaviour;
    private SongInterface songInterface;

    private final Drawable customCoverImage, customCoverBackground;
    private HashMap<Integer, Drawable> loadedCovers = new HashMap<>();

    public QueueAdapter(Context c, ArrayList<Track> queueList, int queuePosition, PlaybackBehaviour.PlaybackBehaviourState playbackBehaviour, SongInterface songInterface) {
        this.queueList = queueList;
        this.queuePosition = queuePosition;
        this.context = c;
        this.playbackBehaviour = playbackBehaviour;
        this.customCoverImage = ResourcesCompat.getDrawable(c.getResources(), R.drawable.ic_baseline_music_note_24, null);
        this.customCoverBackground = ResourcesCompat.getDrawable(c.getResources(), R.drawable.background_button_secondary, null);
        this.songInterface = songInterface;
    }

    @NonNull
    @Override
    public QueueAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.queue_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull QueueAdapter.ViewHolder holder, int position) {
        Track track = queueList.get(position);
        holder.title.setText(track.getTTitle());
        holder.artist.setText(track.getTArtist());
        holder.queueIndex.setText(getIndexTextForPosition(position));
        holder.itemView.setOnClickListener((view) -> songInterface.onSongSelectedListener(queueList.get(position)));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        if (payloads.isEmpty() || !payloads.get(0).equals("NO_COLOR")) {
            setColors(position, holder);
            if (payloads.isEmpty() || !payloads.get(0).equals("REFRESH_INDEX")) {
                Track track = queueList.get(position);
                loadCover(holder, track);
            }
        }
    }

    private String getIndexTextForPosition(int position) {
        if (position == queuePosition && playbackBehaviour != PlaybackBehaviour.PlaybackBehaviourState.SHUFFLE) {
            return String.valueOf(0);
        } else if (position >= queuePosition && playbackBehaviour == PlaybackBehaviour.PlaybackBehaviourState.REPEAT_LIST) {
            return String.valueOf(position - queuePosition);
        } else {
            return "~";
        }
    }

    @Override
    public int getItemCount() {
        return queueList.size();
    }

    private void loadCover(QueueAdapter.ViewHolder holder, Track track) {
        if (holder != null) {
            holder.coverImage.setBackground(customCoverBackground);
            holder.coverImage.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorOnSecondaryContainer));
            holder.coverImage.setForeground(customCoverImage);
            holder.coverImage.setForegroundTintList(ContextCompat.getColorStateList(context, R.color.colorOnSecondary));
        }

        Integer trackId = track.getTId();
        if (loadedCovers.containsKey(trackId) && holder != null) {
            Animation fadeIn = new AlphaAnimation(0, 1);
            fadeIn.setInterpolator(new DecelerateInterpolator());
            fadeIn.setDuration(100);
            holder.coverImage.setAnimation(fadeIn);
            holder.coverImage.setClipToOutline(true);
            holder.coverImage.setForeground(null);
            holder.coverImage.setBackground(loadedCovers.get(trackId));
        }
    }

    private void setColors(int position, ViewHolder holder) {
        if (position >= queuePosition || !(playbackBehaviour == PlaybackBehaviour.PlaybackBehaviourState.REPEAT_LIST)) {
            if (position == queuePosition) {
                holder.itemView.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorTertiaryContainer));
                holder.title.setTextColor(ContextCompat.getColorStateList(context, R.color.colorOnTertiaryContainer));
                holder.artist.setTextColor(ContextCompat.getColorStateList(context, R.color.colorOnTertiaryContainer));
                holder.queueIndex.setTextColor(ContextCompat.getColorStateList(context, R.color.colorOnTertiaryContainer));
                holder.dragHandle.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorOnTertiaryContainer));
            } else {
                holder.itemView.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorBackground));
                holder.title.setTextColor(ContextCompat.getColorStateList(context, R.color.colorOnBackground));
                holder.artist.setTextColor(ContextCompat.getColorStateList(context, R.color.colorOnBackground));
                holder.queueIndex.setTextColor(ContextCompat.getColorStateList(context, R.color.colorOnBackground));
                holder.dragHandle.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorOnBackground));
            }
        } else {
            holder.itemView.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorBackground));
            holder.title.setTextColor(ContextCompat.getColorStateList(context, R.color.colorSurfaceVariant));
            holder.artist.setTextColor(ContextCompat.getColorStateList(context, R.color.colorSurfaceVariant));
            holder.queueIndex.setTextColor(ContextCompat.getColorStateList(context, R.color.colorSurfaceVariant));
            holder.dragHandle.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorSurfaceVariant));
        }
    }

    public void setNewQueuePosition(int newQueuePosition) {
        this.queuePosition = newQueuePosition;
        notifyItemRangeChanged(0, queueList.size(), "REFRESH_INDEX");
    }

    public void updatePlaybackBehaviourState(PlaybackBehaviour.PlaybackBehaviourState newState) {
        playbackBehaviour = newState;
        notifyItemRangeChanged(0, queueList.size(), "REFRESH_INDEX");
    }

    public void getAllBackgroundImages(List<Track> newList) {
        new Thread(() -> {
            List<Track> t = new ArrayList<>();
            t.addAll(newList);
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            for (int i = 0; i < t.size(); i++) {
                Track track = t.get(i);
                Integer trackId = track.getTId();
                Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, trackId);
                try {
                    if (!loadedCovers.containsKey(trackId)) {
                        mmr.setDataSource(context, trackUri);
                        byte[] thumbnail = mmr.getEmbeddedPicture();
                        if (thumbnail != null) {
                            Bitmap cover = BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length);
                            Drawable drawable = ImageUtil.roundCorners(cover, context.getResources());
                            loadedCovers.put(trackId, drawable);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("MediaMetadataRetriever IllegalArgument");
                }
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            notifyItemRangeChanged(0, queueList.size());
        }).start();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView queueIndex, title, artist;
        View coverImage, dragHandle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            queueIndex = itemView.findViewById(R.id.queue_item_position);
            title = itemView.findViewById(R.id.queue_item_title);
            artist = itemView.findViewById(R.id.queue_item_artist);
            coverImage = itemView.findViewById(R.id.queue_item_cover);
            dragHandle = itemView.findViewById(R.id.queue_item_handle);
        }
    }
}
