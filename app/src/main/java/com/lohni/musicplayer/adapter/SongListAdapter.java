package com.lohni.musicplayer.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.lohni.musicplayer.R;
import com.lohni.musicplayer.database.dto.TrackDTO;
import com.lohni.musicplayer.database.entity.Track;
import com.lohni.musicplayer.utils.AdapterUtils;
import com.lohni.musicplayer.utils.enums.ListFilterType;
import com.lohni.musicplayer.utils.enums.PlaybackBehaviour;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.ViewHolder> implements Filterable {

    private final ArrayList<TrackDTO> songList;
    private ArrayList<TrackDTO> mDisplayedValues;
    private final ArrayList<Track> queue = new ArrayList<>();
    private HashMap<Integer, Drawable> drawableHashMap = new HashMap<>();
    private final Drawable customCoverImage;
    private final Context context;
    private ListFilterType listFilterType;
    private PlaybackBehaviour playbackBehaviour = PlaybackBehaviour.REPEAT_LIST;
    private int currPlayingSongIndex = -1;
    private final Handler refreshInfotextHandler;
    private Runnable refreshInfotextRunnable;

    private OnItemClickedListener onItemClickedListener;
    private OnItemOptionClickedListener onItemOptionClickedListener;


    public interface OnItemClickedListener {
        void onItemClicked(int position);
    }

    public interface OnItemOptionClickedListener {
        void onItemOptionClicked(View view, int position, boolean inQueue);
    }

    public SongListAdapter(Context c, ArrayList<TrackDTO> songList) {
        this.songList = songList;
        this.mDisplayedValues = songList;
        this.context = c;
        this.customCoverImage = ResourcesCompat.getDrawable(c.getResources(), R.drawable.ic_baseline_music_note_24, null);

        this.refreshInfotextHandler = new Handler();
        this.refreshInfotextRunnable = () -> {
            if (ListFilterType.LAST_PLAYED.equals(listFilterType))
                notifyItemRangeChanged(0, getItemCount(), "");
            refreshInfotextHandler.postDelayed(refreshInfotextRunnable, 10000);
        };
        this.refreshInfotextHandler.postDelayed(refreshInfotextRunnable, 1000);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tracklist_item, parent, false);
        ViewHolder viewHolder = new SongListAdapter.ViewHolder(v);
        setDefaultBackground(viewHolder);
        return viewHolder;
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        setDefaultBackground(holder);
        super.onViewRecycled(holder);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TrackDTO dto = mDisplayedValues.get(position);
        Track track = dto.getTrack();
        Optional<Track> curr = songList.stream().map(TrackDTO::getTrack).filter(t -> t.getTId().equals(currPlayingSongIndex)).findFirst();
        Track currPlaying = curr.orElse(null);

        holder.artist.setText(track.getTArtist());
        holder.info.setText(songList.get(position).getAsInfoText(listFilterType));

        holder.itemView.setOnClickListener(view -> {
            if (onItemClickedListener != null) onItemClickedListener.onItemClicked(position);
        });
        holder.more.setOnClickListener((view) -> {
            if (onItemOptionClickedListener != null)
                onItemOptionClickedListener.onItemOptionClicked(view, position, queue.contains(track));
        });

        int colorRes = (currPlayingSongIndex >= 0 && track.equals(currPlaying))
                ? R.color.colorSurfaceLevel4
                : R.color.colorBackground;
        holder.itemView.setBackgroundTintList(ContextCompat.getColorStateList(context, colorRes));

        int colorMore = (queue.contains(track)) ? R.color.colorPrimary : R.color.colorOnSurface;
        holder.more.setBackgroundTintList(ContextCompat.getColorStateList(context, colorMore));

        int currPlayingQueueIndex = queue.indexOf(currPlaying);
        if (isNextPlaying(track, currPlayingQueueIndex)) holder.isNext.setVisibility(View.VISIBLE);
        else holder.isNext.setVisibility(View.GONE);

        holder.title.setText(track.getTTitle());
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty() || payloads.get(0).equals("RELOAD_IMAGES")) {
            setCoverImage(holder, position);
        }

        if (payloads.isEmpty() || !payloads.get(0).equals("RELOAD_IMAGES")) {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public int getItemCount() {
        return mDisplayedValues.size();
    }

    public int getQueueItemCount() {
        return queue.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                ArrayList<TrackDTO> filteredList = new ArrayList<>();
                if (constraint == null || constraint.length() == 0 || constraint.equals("")) {
                    filterResults.count = songList.size();
                    filterResults.values = songList;
                } else {
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < songList.size(); i++) {
                        String data = songList.get(i).getTrack().getTTitle();
                        if (data.toLowerCase().contains(constraint.toString())) {
                            filteredList.add(songList.get(i));
                        }
                    }
                    filterResults.count = filteredList.size();
                    filterResults.values = filteredList;
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mDisplayedValues = (ArrayList<TrackDTO>) filterResults.values; // has the filtered values
                notifyDataSetChanged();  // notifies the data with new filtered values
            }
        };
    }

    public void setListFilterType(ListFilterType listFilterType) {
        this.listFilterType = listFilterType;
        notifyItemRangeChanged(0, getItemCount(), "RELOAD_IMAGES");
    }

    public void setCurrentPlayingIndex(int currentPlayingIndex) {
        updateCurrAndNextItem(this.currPlayingSongIndex);

        this.currPlayingSongIndex = currentPlayingIndex;
        updateCurrAndNextItem(currentPlayingIndex);
    }

    public int getCurrentPlayingIndex() {
        return currPlayingSongIndex;
    }

    public void setOnItemClickedListener(OnItemClickedListener onItemClickedListener) {
        this.onItemClickedListener = onItemClickedListener;
    }

    public void setOnItemOptionClickedListener(OnItemOptionClickedListener onItemOptionClickedListener) {
        this.onItemOptionClickedListener = onItemOptionClickedListener;
    }

    public void setQueueList(ArrayList<Track> queue) {
        if (!AdapterUtils.compareLists(this.queue, queue)) {
            this.queue.clear();
            this.queue.addAll(queue);
            notifyItemRangeChanged(0, getItemCount(), "");
        }
    }

    public void setPlaybackBehaviour(PlaybackBehaviour newState) {
        this.playbackBehaviour = newState;
        if (currPlayingSongIndex >= 0) {
            updateCurrAndNextItem(currPlayingSongIndex);
        }
    }

    public void setDrawableHashMap(HashMap<Integer, Drawable> drawableHashMap) {
        this.drawableHashMap = drawableHashMap;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, artist, info;
        View coverImage, isNext, more;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tracklist_title);
            artist = itemView.findViewById(R.id.tracklist_artist);
            coverImage = itemView.findViewById(R.id.tracklist_cover);
            more = itemView.findViewById(R.id.tracklist_more);
            info = itemView.findViewById(R.id.tracklist_info);
            isNext = itemView.findViewById(R.id.tracklist_next);

            Animation fadeIn = new AlphaAnimation(0, 1);
            fadeIn.setInterpolator(new DecelerateInterpolator());
            fadeIn.setDuration(350);
            coverImage.setAnimation(fadeIn);
            coverImage.setClipToOutline(true);
        }
    }


    private void setDefaultBackground(ViewHolder holder) {
        holder.coverImage.setBackground(customCoverImage);
        holder.coverImage.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorSecondary));
        holder.more.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorOnSurface));
    }

    private void setCoverImage(ViewHolder holder, int position) {
        Integer trackId = mDisplayedValues.get(position).getTrack().getTId();
        Drawable drawable = drawableHashMap.getOrDefault(trackId, null);

        if (drawable != null) holder.coverImage.setBackground(drawable);
    }

    private boolean isNextPlaying(Track track, int currPlayingQueueIndex) {
        return playbackBehaviour == PlaybackBehaviour.REPEAT_LIST
                && currPlayingQueueIndex >= 0 && currPlayingQueueIndex + 1 < queue.size()
                && track.equals(queue.get(currPlayingQueueIndex + 1));
    }

    private void updateCurrAndNextItem(int currPlayingSongIndex) {
        for (int x = 0; x < songList.size(); x++) {
            if (songList.get(x).getTrack().getTId().equals(currPlayingSongIndex)) {
                Track track = songList.get(x).getTrack();
                int queueIndex = queue.indexOf(track);
                if (queueIndex + 1 < queue.size()) {
                    Track next = queue.get(queueIndex + 1);
                    for (int i = 0; i < songList.size(); i++) {
                        if (songList.get(i).getTrack().equals(next)) notifyItemChanged(i);
                    }
                }
                notifyItemChanged(x);
            }
        }
    }
}
