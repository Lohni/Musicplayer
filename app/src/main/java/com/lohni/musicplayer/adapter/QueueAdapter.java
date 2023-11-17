package com.lohni.musicplayer.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.lohni.musicplayer.R;
import com.lohni.musicplayer.database.entity.Track;
import com.lohni.musicplayer.interfaces.QueueControlInterface;
import com.lohni.musicplayer.utils.enums.PlaybackBehaviour;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.ViewHolder> {
    private final ArrayList<Track> queueList;
    private int queuePosition;
    private Context context;
    private PlaybackBehaviour playbackBehaviour;
    private QueueControlInterface songInterface;

    private final Drawable customCoverImage, customCoverBackground;
    private HashMap<Integer, Drawable> drawableHashMap = new HashMap<>();

    public QueueAdapter(Context c, ArrayList<Track> queueList, int queuePosition, PlaybackBehaviour playbackBehaviour, QueueControlInterface songInterface) {
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
        if (position == queuePosition && playbackBehaviour != PlaybackBehaviour.SHUFFLE) {
            return String.valueOf(0);
        } else if (position >= queuePosition && playbackBehaviour == PlaybackBehaviour.REPEAT_LIST) {
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
        if (drawableHashMap.containsKey(trackId) && holder != null) {
            Animation fadeIn = new AlphaAnimation(0, 1);
            fadeIn.setInterpolator(new DecelerateInterpolator());
            fadeIn.setDuration(100);
            holder.coverImage.setAnimation(fadeIn);
            holder.coverImage.setClipToOutline(true);
            holder.coverImage.setForeground(null);
            holder.coverImage.setBackground(drawableHashMap.get(trackId));
        }
    }

    private void setColors(int position, ViewHolder holder) {
        if (position >= queuePosition || !(playbackBehaviour == PlaybackBehaviour.REPEAT_LIST)) {
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

    public void updatePlaybackBehaviourState(PlaybackBehaviour newState) {
        playbackBehaviour = newState;
        notifyItemRangeChanged(0, queueList.size(), "REFRESH_INDEX");
    }


    public void setDrawableHashMap(HashMap<Integer, Drawable> drawableHashMap) {
        this.drawableHashMap = drawableHashMap;
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
