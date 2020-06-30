package com.example.musicplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.musicplayer.R;
import com.example.musicplayer.entities.MusicResolver;

import java.util.ArrayList;

public class SongListAdapter extends BaseAdapter implements Filterable {

    private ArrayList<MusicResolver> songList, mDisplayedValues;
    private LayoutInflater layoutInflater;

    public SongListAdapter(Context c, ArrayList<MusicResolver> songList){
        this.songList=songList;
        this.mDisplayedValues=songList;
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
        ViewHolder viewHolder = null;
        View view = convertview;
        if(view==null){
            view=layoutInflater.inflate(R.layout.songlist_item,viewGroup,false);
            viewHolder = new ViewHolder();

            viewHolder.title = view.findViewById(R.id.song_title);
            viewHolder.artist = view.findViewById(R.id.song_artist);
            view.setTag(viewHolder);
        } else viewHolder = (ViewHolder) view.getTag();

        viewHolder.title.setText(mDisplayedValues.get(i).getTitle());
        viewHolder.artist.setText(mDisplayedValues.get(i).getArtist());
        return view;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @SuppressWarnings("unchecked")
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                ArrayList<MusicResolver> filteredList = new ArrayList<>();

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
        return filter;
    }

    private class ViewHolder{
        TextView title, artist;
    }
}
