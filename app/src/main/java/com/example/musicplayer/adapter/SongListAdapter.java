package com.example.musicplayer.adapter;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.musicplayer.R;
import com.example.musicplayer.entities.MusicResolver;
import com.example.musicplayer.ui.songlist.SongListInterface;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.ViewHolder>{

    private ArrayList<MusicResolver> songList;
    private SongListInterface songListInterface;
    private Drawable customCoverImage;
    private Context context;

    public SongListAdapter(Context c, ArrayList<MusicResolver> songList, SongListInterface songListInterface){
        this.songList=songList;
        this.songListInterface = songListInterface;
        this.context = c;
        this.customCoverImage = ResourcesCompat.getDrawable(c.getResources(),R.drawable.ic_baseline_music_note_24,null);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tageditor_item, parent, false);
        return new SongListAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MusicResolver track = songList.get(position);
        holder.artist.setText(track.getArtist());
        holder.title.setText(track.getTitle());
        holder.coverImage.setImageDrawable(customCoverImage);
        holder.itemView.setOnClickListener(view -> {
            songListInterface.OnSongListCreatedListener(songList);
            songListInterface.OnSongSelectedListener(position);
        });

        holder.coverImage.postDelayed(() -> {
            Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,track.getId());
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(context,trackUri);
            byte [] thumbnail = mmr.getEmbeddedPicture();
            mmr.release();
            if (thumbnail != null){
                holder.coverImage.setImageBitmap(BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length));
            }
        },500);
    }

    /*
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

     */

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView title, artist;
        ImageView coverImage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tagEditorList_title);
            artist = itemView.findViewById(R.id.tagEditorList_artist);
            coverImage = itemView.findViewById(R.id.tagEditorList_cover);
        }
    }
}
