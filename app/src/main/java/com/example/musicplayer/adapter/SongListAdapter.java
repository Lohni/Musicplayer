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
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.musicplayer.R;
import com.example.musicplayer.database.dto.TrackDTO;
import com.example.musicplayer.database.entity.Track;
import com.example.musicplayer.utils.GeneralUtils;
import com.example.musicplayer.utils.enums.ListFilterType;
import com.example.musicplayer.utils.enums.PlaybackBehaviour;
import com.example.musicplayer.utils.images.ImageTransformUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.ViewHolder> implements Filterable {

    private ArrayList<TrackDTO> songList, mDisplayedValues;
    private ArrayList<Track> queue = new ArrayList<>();
    private HashMap<Integer, Drawable> drawableHashMap = new HashMap<>();
    private final Drawable customCoverImage, customCoverBackground;
    private Context context;
    private ListFilterType listFilterType;
    private PlaybackBehaviour.PlaybackBehaviourState playbackBehaviour = PlaybackBehaviour.PlaybackBehaviourState.REPEAT_LIST;
    private int currPlayingSongIndex = -1;
    private boolean isScrolling = false;

    private OnItemClickedListener onItemClickedListener;
    private OnItemOptionClickedListener onItemOptionClickedListener;

    public interface OnItemClickedListener {
        void onItemClicked(int position);
    }

    public interface OnItemOptionClickedListener {
        void onItemOptionClicked(View view, int position, boolean inQueue);
    }

    public SongListAdapter(Context c, ArrayList<TrackDTO> songList, ListFilterType listFilterType) {
        this.songList = songList;
        this.mDisplayedValues = songList;
        this.context = c;
        this.listFilterType = listFilterType;
        this.customCoverImage = ResourcesCompat.getDrawable(c.getResources(), R.drawable.ic_baseline_music_note_24, null);
        this.customCoverBackground = ResourcesCompat.getDrawable(context.getResources(), R.drawable.background_button_secondary, null);
    }

    public void getAllBackgroundImages(List<TrackDTO> newList, RecyclerView recyclerView) {
        new Thread(() -> {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            List<TrackDTO> t = new ArrayList<>(newList);
            for (TrackDTO trackDTO : t) {
                Integer trackId = trackDTO.getTrack().getTId();
                Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, trackId);
                try {
                    if (!drawableHashMap.containsKey(trackId)) {
                        mmr.setDataSource(context, trackUri);
                        byte[] thumbnail = mmr.getEmbeddedPicture();
                        if (thumbnail != null) {
                            Bitmap cover = BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length);
                            Drawable drawable = ImageTransformUtil.roundCorners(cover, context.getResources());
                            drawableHashMap.put(trackId, drawable);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("MediaMetadataRetriever IllegalArgument");
                }
            }

            while (recyclerView.isComputingLayout()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            notifyItemRangeChanged(0, songList.size(), "RELOAD_IMAGES");
        }).start();
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
        holder.imageTrackID = -1;
        setDefaultBackground(holder);
        super.onViewRecycled(holder);
    }

    private void setDefaultBackground(ViewHolder holder) {
        holder.coverImage.setBackground(customCoverBackground);
        holder.coverImage.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorOnSecondaryContainer));
        holder.coverImage.setForeground(customCoverImage);
        holder.coverImage.setForegroundTintList(ContextCompat.getColorStateList(context, R.color.colorOnSecondary));
        holder.more.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorOnSurface));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TrackDTO dto = mDisplayedValues.get(position);
        Track track = dto.getTrack();
        Optional<Track> curr = songList.stream().map(TrackDTO::getTrack).filter(t -> t.getTId().equals(currPlayingSongIndex)).findFirst();
        Track currPlaying = curr.orElse(null);

        holder.trackDTO = dto;
        holder.artist.setText(track.getTArtist());
        holder.info.setText(getInfoText(songList.get(position)));

        holder.itemView.setOnClickListener(view -> {
            if (onItemClickedListener != null) onItemClickedListener.onItemClicked(position);
        });
        holder.more.setOnClickListener((view) -> {
            if (onItemOptionClickedListener != null)
                onItemOptionClickedListener.onItemOptionClicked(view, position, queue.contains(track));
        });

        if (holder.imageTrackID < 0) {
            Integer trackId = holder.trackDTO.getTrack().getTId();
            Drawable drawable = drawableHashMap.getOrDefault(trackId, null);

            if (drawable != null) {
                Animation fadeIn = new AlphaAnimation(0, 1);
                fadeIn.setInterpolator(new DecelerateInterpolator());
                fadeIn.setDuration(350);
                holder.coverImage.setAnimation(fadeIn);
                holder.coverImage.setClipToOutline(true);
                holder.coverImage.setForeground(null);
                holder.coverImage.setBackground(drawable);
            }
            holder.imageTrackID = holder.trackDTO.getTrack().getTId();
        }

        if (!isScrolling) {
            holder.info.postDelayed(() -> {
                if (position == holder.getAbsoluteAdapterPosition()) {
                    notifyItemChanged(holder.getAbsoluteAdapterPosition(), "");
                }
            }, 10000);
        }

        if (currPlayingSongIndex >= 0 && track.equals(currPlaying)) {
            holder.itemView.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorSurfaceLevel4));
        } else {
            holder.itemView.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorBackground));
        }

        int color = (queue.contains(track)) ? R.color.colorPrimary : R.color.colorOnSurface;
        holder.more.setBackgroundTintList(ContextCompat.getColorStateList(context, color));

        int currQueueIndex = queue.indexOf(currPlaying);
        if (playbackBehaviour == PlaybackBehaviour.PlaybackBehaviourState.REPEAT_LIST
                && currPlaying != null
                && currQueueIndex >= 0 && currQueueIndex + 1 < queue.size()
                && track.equals(queue.get(currQueueIndex + 1))) {
            holder.isNext.setVisibility(View.VISIBLE);
        } else {
            holder.isNext.setVisibility(View.GONE);
        }

        holder.title.setText(track.getTTitle());
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty() && payloads.get(0).equals("RELOAD_IMAGES"))
            holder.imageTrackID = -1;
        super.onBindViewHolder(holder, position, payloads);
    }

    private String getInfoText(TrackDTO trackDTO) {
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
                return GeneralUtils.convertTimeWithUnit(Integer.parseInt(trackDTO.getSize()));
            }
        }
        return "";
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

    @Override
    public int getItemCount() {
        return mDisplayedValues.size();
    }

    public void setListFilterType(ListFilterType listFilterType) {
        this.listFilterType = listFilterType;
    }

    public void isScrolling(boolean newIsScrolling) {
        this.isScrolling = newIsScrolling;
    }

    public void setCurrentPlayingIndex(int currentPlayingIndex) {
        this.currPlayingSongIndex = currentPlayingIndex;
        notifyItemRangeChanged(0, getItemCount(), "");
    }

    public void setOnItemClickedListener(OnItemClickedListener onItemClickedListener) {
        this.onItemClickedListener = onItemClickedListener;
    }

    public void setOnItemOptionClickedListener(OnItemOptionClickedListener onItemOptionClickedListener) {
        this.onItemOptionClickedListener = onItemOptionClickedListener;
    }

    public void setQueueList(ArrayList<Track> queue) {
        this.queue.clear();
        this.queue.addAll(queue);
        notifyItemRangeChanged(0, getItemCount(), "");
    }

    public void setPlaybackBehaviour(PlaybackBehaviour.PlaybackBehaviourState newState) {
        this.playbackBehaviour = newState;
        if (currPlayingSongIndex >= 0) {
            notifyItemChanged(currPlayingSongIndex + 1);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, artist, info;
        View coverImage, isNext;
        ImageButton more;
        TrackDTO trackDTO;
        int imageTrackID;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tracklist_title);
            artist = itemView.findViewById(R.id.tracklist_artist);
            coverImage = itemView.findViewById(R.id.tracklist_cover);
            more = itemView.findViewById(R.id.tracklist_more);
            info = itemView.findViewById(R.id.tracklist_info);
            isNext = itemView.findViewById(R.id.tracklist_next);
            imageTrackID = -1;
        }
    }
}
