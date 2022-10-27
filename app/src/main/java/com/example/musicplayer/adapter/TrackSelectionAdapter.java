package com.example.musicplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.musicplayer.R;
import com.example.musicplayer.database.entity.Track;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class TrackSelectionAdapter extends RecyclerView.Adapter<TrackSelectionAdapter.ViewHolder> implements Filterable {

    private ArrayList<Track> trackList, mDisplayedvalues, selected;
    private ArrayList<Integer> originalIndex;
    private final Context context;
    private OnTrackSelectedListener onTrackSelectedListener;

    public interface OnTrackSelectedListener {
        void onSongSelected(int position);
    }

    public TrackSelectionAdapter(Context context, ArrayList<Track> trackList) {
        this.trackList = trackList;
        this.mDisplayedvalues = trackList;
        this.context = context;
        this.selected = new ArrayList<>();
    }

    @NonNull
    @Override
    public TrackSelectionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_item_add, parent, false);
        return new TrackSelectionAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackSelectionAdapter.ViewHolder viewHolder, int position) {
        Track sel = mDisplayedvalues.get(position);

        viewHolder.title.setText(sel.getTTitle());
        viewHolder.artist.setText(sel.getTArtist());
        viewHolder.checkBox.setChecked(selected.contains(sel));
        viewHolder.itemView.setOnClickListener(view -> {
            if (selected.contains(sel)) {
                selected.remove(sel);
                updateViewHolderNotSelected(viewHolder);
            } else {
                selected.add(sel);
                updateViewHolderSelected(viewHolder);
            }

            viewHolder.checkBox.setChecked(!viewHolder.checkBox.isChecked());
            if (onTrackSelectedListener != null) {
                onTrackSelectedListener.onSongSelected(position);
            }
        });

        if (viewHolder.checkBox.isChecked()) {
            updateViewHolderSelected(viewHolder);
        } else {
            updateViewHolderNotSelected(viewHolder);
        }
    }

    private void updateViewHolderSelected(TrackSelectionAdapter.ViewHolder viewHolder) {
        viewHolder.itemView.setBackgroundResource(R.color.colorSurfaceLevel3);
        viewHolder.title.setTextColor(ContextCompat.getColor(context, R.color.colorOnSurface));
        viewHolder.artist.setTextColor(ContextCompat.getColor(context, R.color.colorOnSurface));
        viewHolder.checkBox.setButtonTintList(ContextCompat.getColorStateList(context, R.color.colorPrimary));
    }

    private void updateViewHolderNotSelected(TrackSelectionAdapter.ViewHolder viewHolder) {
        viewHolder.itemView.setBackgroundResource(R.color.colorSurface);
        viewHolder.title.setTextColor(ContextCompat.getColor(context, R.color.colorOnSurface));
        viewHolder.artist.setTextColor(ContextCompat.getColor(context, R.color.colorOnSurface));
        viewHolder.checkBox.setButtonTintList(ContextCompat.getColorStateList(context, R.color.colorPrimary));
    }

    @Override
    public int getItemCount() {
        return mDisplayedvalues.size();
    }

    public int getSelectedCount() {
        return selected.size();
    }

    public ArrayList<Track> getSelected() {
        return selected;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                ArrayList<Track> filteredList = new ArrayList<>();
                originalIndex = new ArrayList<>();

                if (trackList == null) {
                    trackList = new ArrayList<Track>(mDisplayedvalues);
                }
                if (constraint == null || constraint.length() == 0) {

                    // set the Original result to return
                    filterResults.count = trackList.size();
                    filterResults.values = trackList;
                } else {
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < trackList.size(); i++) {
                        String data = trackList.get(i).getTTitle();
                        if (data.toLowerCase().startsWith(constraint.toString())) {
                            filteredList.add(trackList.get(i));
                            originalIndex.add(i);
                        }
                    }
                    // set the Filtered result to return
                    filterResults.count = filteredList.size();
                    filterResults.values = filteredList;
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mDisplayedvalues = (ArrayList<Track>) filterResults.values; // has the filtered values
                notifyDataSetChanged();  // notifies the data with new filtered values
            }
        };
    }

    public void setOnTrackSelectedListener(OnTrackSelectedListener onTrackSelectedListener) {
        this.onTrackSelectedListener = onTrackSelectedListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, artist;
        CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.playlist_item_title);
            artist = itemView.findViewById(R.id.playlist_item_artist);
            checkBox = itemView.findViewById(R.id.playlist_item_box);
        }
    }
}
