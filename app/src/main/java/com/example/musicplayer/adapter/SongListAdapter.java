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
import android.widget.ImageView;
import android.widget.TextView;

import com.example.musicplayer.R;
import com.example.musicplayer.database.dto.TrackDTO;
import com.example.musicplayer.database.entity.Track;
import com.example.musicplayer.ui.songlist.SongListInterface;
import com.example.musicplayer.utils.GeneralUtils;
import com.example.musicplayer.utils.enums.ListFilterType;

import java.time.LocalDateTime;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.ViewHolder> implements Filterable {

    private ArrayList<TrackDTO> songList, mDisplayedValues;
    private SongListInterface songListInterface;
    private Drawable customCoverImage;
    private Context context;
    private ListFilterType listFilterType;
    private int currPlayingSongIndex = -1, imagesLoading = 0;
    MediaMetadataRetriever mmr = null;
    boolean isScrolling = false;

    public SongListAdapter(Context c, ArrayList<TrackDTO> songList, SongListInterface songListInterface, ListFilterType listFilterType) {
        this.songList = songList;
        this.mDisplayedValues = songList;
        this.songListInterface = songListInterface;
        this.context = c;
        this.listFilterType = listFilterType;
        this.customCoverImage = ResourcesCompat.getDrawable(c.getResources(), R.drawable.ic_baseline_music_note_24, null);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tracklist_item, parent, false);
        return new SongListAdapter.ViewHolder(v);
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        holder.imageTrackID = -1;
        super.onViewRecycled(holder);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TrackDTO dto = mDisplayedValues.get(position);
        Track track = dto.getTrack();
        holder.trackDTO = dto;
        holder.artist.setText(track.getTArtist());
        holder.title.setText(track.getTTitle());
        holder.info.setText(getInfoText(songList.get(position)));
        holder.itemView.setOnClickListener(view -> songListInterface.OnSongSelectedListener(position));

        if (holder.imageTrackID != track.getTId()) {
            holder.coverImage.setImageDrawable(customCoverImage);
            holder.coverImage.setImageTintList(ContextCompat.getColorStateList(context, R.color.NewcolorSurfaceVariant));
        }

        if (!isScrolling && holder.imageTrackID != track.getTId()) {
            holder.coverImage.post(() -> {
                Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, holder.trackDTO.getTrack().getTId());

                if (mmr == null) {
                    mmr = new MediaMetadataRetriever();
                }

                byte[] thumbnail = null;
                try {
                    mmr.setDataSource(context, trackUri);
                    thumbnail = mmr.getEmbeddedPicture();
                } catch (IllegalArgumentException e) {
                    System.out.println("MediaMetadataRetriever IllegalArgument");
                } finally {
                    if (thumbnail != null) {
                        imagesLoading++;
                        Animation fadeIn = new AlphaAnimation(0, 1);
                        fadeIn.setInterpolator(new DecelerateInterpolator());

                        int addTime = (holder.getAbsoluteAdapterPosition() < 15 || holder.getAbsoluteAdapterPosition() > (songList.size() - 15)) ? 4 : 1;

                        fadeIn.setDuration(350 + (120L * imagesLoading * addTime));
                        holder.coverImage.setAnimation(fadeIn);
                        Bitmap cover = BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length);
                        holder.coverImage.setClipToOutline(true);
                        holder.coverImage.setImageBitmap(cover);
                        holder.coverImage.setImageTintList(null);
                        imagesLoading--;
                    }
                    holder.imageTrackID = holder.trackDTO.getTrack().getTId();
                }
            });
        }

        if (!isScrolling) {
            holder.info.postDelayed(() -> {
                if (position == holder.getAbsoluteAdapterPosition()) {
                    notifyItemChanged(holder.getAbsoluteAdapterPosition(), "");
                }
            }, 10000);
        }

        if (currPlayingSongIndex >= 0 && track.getTId().equals(currPlayingSongIndex)) {
            holder.itemView.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.NewcolorTeritaryContainer));
            holder.title.setTextColor(ContextCompat.getColorStateList(context, R.color.NewcolorOnTeritaryContainer));
            holder.artist.setTextColor(ContextCompat.getColorStateList(context, R.color.NewcolorOnTeritaryContainer));
            holder.more.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.NewcolorOnTeritaryContainer));
        } else {
            holder.itemView.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.NewcolorSurfaceVariant));
            holder.title.setTextColor(ContextCompat.getColorStateList(context, R.color.NewcolorText));
            holder.artist.setTextColor(ContextCompat.getColorStateList(context, R.color.NewcolorText));
            holder.more.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.NewcolorText));
        }
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
                try {
                    return GeneralUtils.convertTimeWithUnit(Integer.parseInt(trackDTO.getSize()));
                } catch (NumberFormatException ignored) {
                }
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
        int oldIndex = this.currPlayingSongIndex;
        this.currPlayingSongIndex = currentPlayingIndex;

        for (int i = 0; i < songList.size(); i++) {
            if (songList.get(i).getTrack().getTId().equals(currentPlayingIndex)) {
                notifyItemChanged(i, "");
            } else if (songList.get(i).getTrack().getTId().equals(oldIndex)) {
                notifyItemChanged(i, "");
            }
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, artist, info;
        ImageView coverImage;
        ImageButton more;
        TrackDTO trackDTO;
        int imageTrackID = -1;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tracklist_title);
            artist = itemView.findViewById(R.id.tracklist_artist);
            coverImage = itemView.findViewById(R.id.tracklist_cover);
            more = itemView.findViewById(R.id.tracklist_more);
            info = itemView.findViewById(R.id.tracklist_info);
        }
    }
}
