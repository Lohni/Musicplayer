package com.lohni.musicplayer.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.lohni.musicplayer.R;
import com.lohni.musicplayer.database.dto.ItemPlayedDTO;
import com.lohni.musicplayer.utils.GeneralUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

public class DatabaseViewerAdapter extends RecyclerView.Adapter<DatabaseViewerAdapter.ViewHolder> implements Filterable {

    private final ArrayList<ItemPlayedDTO> itemPlayedList;
    private ArrayList<ItemPlayedDTO> mDisplayedValues;
    private final DateTimeFormatter dtf;
    private final Context c;
    public DatabaseViewerAdapter(Context c, ArrayList<ItemPlayedDTO> itemPlayedList) {
        this.itemPlayedList = itemPlayedList;
        this.mDisplayedValues = itemPlayedList;
        this.c = c;
        dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.database_viewer_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemPlayedDTO itemPlayedDTO = mDisplayedValues.get(position);
        holder.id.setText(String.valueOf(itemPlayedDTO.getId()));
        holder.title.setText(itemPlayedDTO.getTitle());
        holder.subtitle.setText(itemPlayedDTO.getSubTitle());
        holder.credat.setText(LocalDateTime.parse(itemPlayedDTO.getCredat(), GeneralUtils.DB_TIMESTAMP).format(dtf));
        holder.timeplayed.setText(GeneralUtils.convertTimeWithUnit((int) itemPlayedDTO.getTimePlayed()));

        holder.type.setBackground(getType(itemPlayedDTO.getType()));

        if (itemPlayedDTO.getRefType() != 0) {
            holder.reftype.setBackground(getType(itemPlayedDTO.getRefType()));
            holder.refid.setText(String.valueOf(itemPlayedDTO.getRefId()));
        } else {
            holder.reftype.setBackground(null);
            holder.refid.setText("");
        }
    }

    private Drawable getType(int id) {
        if (id == 0) return ResourcesCompat.getDrawable(c.getResources(), R.drawable.ic_baseline_music_note_24, null);
        else if (id == 2) return ResourcesCompat.getDrawable(c.getResources(), R.drawable.ic_album_black_24dp, null);
        else return ResourcesCompat.getDrawable(c.getResources(), R.drawable.ic_playlist_play_black_24dp, null);
    }

    @Override
    public int getItemCount() {
        return mDisplayedValues.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                PlayedFilter playedFilter = PlayedFilter.getPlayedFilterByName(constraint.toString());
                List<ItemPlayedDTO> filteredList;
                if (playedFilter == PlayedFilter.TRACK) {
                    filteredList = itemPlayedList.stream().filter(ip -> ip.getType() == 0).collect(Collectors.toList());
                } else if (playedFilter == PlayedFilter.PLAYLIST) {
                    filteredList = itemPlayedList.stream().filter(ip -> ip.getType() == 1).collect(Collectors.toList());
                } else if (playedFilter == PlayedFilter.ALBUM) {
                    filteredList = itemPlayedList.stream().filter(ip -> ip.getType() == 2).collect(Collectors.toList());
                } else {
                    filteredList = new ArrayList<>(itemPlayedList);
                }
                filterResults.count = filteredList.size();
                filterResults.values = filteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mDisplayedValues = (ArrayList<ItemPlayedDTO>) results.values;
                notifyDataSetChanged();
            }
        };
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView id, title, subtitle, timeplayed, credat, refid;
        View type, reftype;

        public ViewHolder(View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.database_viewer_id);
            title = itemView.findViewById(R.id.database_viewer_title);
            subtitle = itemView.findViewById(R.id.database_viewer_subtitle);
            timeplayed = itemView.findViewById(R.id.database_viewer_timeplayed);
            credat = itemView.findViewById(R.id.database_viewer_credat);
            refid = itemView.findViewById(R.id.database_viewer_refid);
            type = itemView.findViewById(R.id.database_viewer_type);
            reftype = itemView.findViewById(R.id.database_viewer_reftype);
        }
    }

    public enum PlayedFilter {
        ALL, TRACK, PLAYLIST, ALBUM;

        public static String getTitleFromFilter(PlayedFilter filter) {
            if (filter == TRACK) return "Track";
            else if (filter == PLAYLIST) return "Playlist";
            else if (filter == ALBUM) return "Album";
            else return "All";
        }

        public static PlayedFilter getPlayedFilterByOrdinal(int ord) {
            if (ord == 0) return ALL;
            else if (ord == 1) return TRACK;
            else if (ord == 2) return PLAYLIST;
            else if (ord == 3) return ALBUM;
            else return ALL;
        }

        public static PlayedFilter getPlayedFilterByName(String name) {
            if (name.equals("Track")) return TRACK;
            else if (name.equals("Playlist")) return PLAYLIST;
            else if (name.equals("Album")) return ALBUM;
            else return ALL;
        }

        @NonNull
        @Override
        public String toString() {
            return getTitleFromFilter(this);
        }
    }
}
