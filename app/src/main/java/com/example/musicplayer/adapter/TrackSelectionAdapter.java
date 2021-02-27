package com.example.musicplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.musicplayer.R;
import com.example.musicplayer.entities.MusicResolver;

import java.util.ArrayList;

import androidx.core.content.ContextCompat;

public class TrackSelectionAdapter extends BaseAdapter implements Filterable {

    private ArrayList<MusicResolver> songList, mDisplayedValues;
    private ArrayList<Integer> originalIndex;
    private LayoutInflater layoutInflater;
    private Context context;

    public TrackSelectionAdapter(Context c, ArrayList<MusicResolver> songList){
        this.songList=songList;
        this.mDisplayedValues=songList;
        context=c;
        layoutInflater=LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return mDisplayedValues.size();
    }

    @Override
    public MusicResolver getItem(int i) {
        return songList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertview, ViewGroup viewGroup) {
        TrackSelectionAdapter.ViewHolder viewHolder = null;
        View view = convertview;
        if(view==null){
            view=layoutInflater.inflate(R.layout.playlist_item_add,viewGroup,false);
            viewHolder = new TrackSelectionAdapter.ViewHolder();

            viewHolder.title = view.findViewById(R.id.playlist_item_title);
            viewHolder.artist = view.findViewById(R.id.playlist_item_artist);
            viewHolder.checkBox = view.findViewById(R.id.playlist_item_box);
            view.setTag(viewHolder);
        } else viewHolder = (TrackSelectionAdapter.ViewHolder) view.getTag();

        viewHolder.title.setText(mDisplayedValues.get(i).getTitle());
        viewHolder.artist.setText(mDisplayedValues.get(i).getArtist());
        viewHolder.checkBox.setChecked(mDisplayedValues.get(i).isSelected());
        if(mDisplayedValues.get(i).isSelected()){
            view.setBackgroundResource(R.color.colorSecondaryLightTrans);
            viewHolder.title.setTextColor(ContextCompat.getColor(context,R.color.colorPrimaryNight));
            viewHolder.artist.setTextColor(ContextCompat.getColor(context,R.color.colorPrimaryNight));
            viewHolder.checkBox.setButtonTintList(ContextCompat.getColorStateList(context,R.color.colorPrimaryNight));
        }
        else{
            view.setBackgroundResource(R.color.colorTransparent);
            viewHolder.title.setTextColor(ContextCompat.getColor(context,R.color.colorTextLight));
            viewHolder.artist.setTextColor(ContextCompat.getColor(context,R.color.colorTextLight));
            viewHolder.checkBox.setButtonTintList(ContextCompat.getColorStateList(context,R.color.colorPrimary));
        }
        return view;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                ArrayList<MusicResolver> filteredList = new ArrayList<>();
                originalIndex = new ArrayList<>();

                if(songList==null){
                    songList = new ArrayList<MusicResolver>(mDisplayedValues);
                }
                if (constraint == null || constraint.length() == 0) {

                    // set the Original result to return
                    filterResults.count = songList.size();
                    filterResults.values = songList;
                } else {
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < songList.size(); i++) {
                        String data = songList.get(i).getTitle();
                        if (data.toLowerCase().startsWith(constraint.toString())) {
                            filteredList.add(songList.get(i));
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
                mDisplayedValues = (ArrayList<MusicResolver>) filterResults.values; // has the filtered values
                notifyDataSetChanged();  // notifies the data with new filtered values
            }
        };
    }

    public Integer getOriginalPosition(int i){
        return originalIndex.get(i);
    }

    private class ViewHolder{
        TextView title, artist;
        CheckBox checkBox;
    }
}
