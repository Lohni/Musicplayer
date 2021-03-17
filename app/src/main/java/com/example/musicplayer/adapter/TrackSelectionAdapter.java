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
import com.example.musicplayer.entities.MusicResolver;
import com.example.musicplayer.ui.playlistdetail.OnTrackSelectedListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class TrackSelectionAdapter extends RecyclerView.Adapter<TrackSelectionAdapter.ViewHolder> implements Filterable {

    private ArrayList<MusicResolver> trackList, mDisplayedvalues;
    private ArrayList<Integer> originalIndex;
    private Context context;
    private OnTrackSelectedListener onTrackSelectedListener;

    public TrackSelectionAdapter(Context c, ArrayList<MusicResolver> trackList, OnTrackSelectedListener onTrackSelectedListener){
        //this.songList=songList;
        this.trackList =trackList;
        this.mDisplayedvalues = trackList;
        context=c;
        this.onTrackSelectedListener = onTrackSelectedListener;
    }

    public int getFilterCount() {
        return trackList.size();
    }

    public MusicResolver getItem(int i) {
        return trackList.get(i);
    }

    @NonNull
    @Override
    public TrackSelectionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_item_add,parent,false);
        return new TrackSelectionAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackSelectionAdapter.ViewHolder viewHolder, int position) {
        viewHolder.title.setText(mDisplayedvalues.get(position).getTitle());
        viewHolder.artist.setText(mDisplayedvalues.get(position).getArtist());
        viewHolder.checkBox.setChecked(mDisplayedvalues.get(position).isSelected());
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onTrackSelectedListener.onSongSelected(position);
            }
        });

        if(mDisplayedvalues.get(position).isSelected()){
            viewHolder.itemView.setBackgroundResource(R.color.colorSecondaryLightTrans);
            viewHolder.title.setTextColor(ContextCompat.getColor(context,R.color.colorPrimaryNight));
            viewHolder.artist.setTextColor(ContextCompat.getColor(context,R.color.colorPrimaryNight));
            viewHolder.checkBox.setButtonTintList(ContextCompat.getColorStateList(context,R.color.colorPrimaryNight));
        } else{
            viewHolder.itemView.setBackgroundResource(R.color.colorTransparent);
            viewHolder.title.setTextColor(ContextCompat.getColor(context,R.color.colorTextLight));
            viewHolder.artist.setTextColor(ContextCompat.getColor(context,R.color.colorTextLight));
            viewHolder.checkBox.setButtonTintList(ContextCompat.getColorStateList(context,R.color.colorPrimary));
        }
    }

    @Override
    public int getItemCount() {
        return mDisplayedvalues.size();
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                ArrayList<MusicResolver> filteredList = new ArrayList<>();
                originalIndex = new ArrayList<>();

                if(trackList==null){
                    trackList = new ArrayList<MusicResolver>(mDisplayedvalues);
                }
                if (constraint == null || constraint.length() == 0) {

                    // set the Original result to return
                    filterResults.count = trackList.size();
                    filterResults.values = trackList;
                } else {
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < trackList.size(); i++) {
                        String data = trackList.get(i).getTitle();
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
                mDisplayedvalues = (ArrayList<MusicResolver>) filterResults.values; // has the filtered values
                notifyDataSetChanged();  // notifies the data with new filtered values
            }
        };
    }


    public Integer getOriginalPosition(int i){
        return originalIndex.get(i);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
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
