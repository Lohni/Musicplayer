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
import android.widget.ImageView;
import android.widget.TextView;

import com.example.musicplayer.R;
import com.example.musicplayer.database.entity.Track;
import com.example.musicplayer.ui.songlist.SongListInterface;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.ViewHolder> {

    private ArrayList<Track> songList;
    private SongListInterface songListInterface;
    private Drawable customCoverImage;
    private Context context;
    private int imagesLoading = 0;

    public SongListAdapter(Context c, ArrayList<Track> songList, SongListInterface songListInterface) {
        this.songList = songList;
        this.songListInterface = songListInterface;
        this.context = c;
        this.customCoverImage = ResourcesCompat.getDrawable(c.getResources(), R.drawable.ic_baseline_music_note_24, null);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tracklist_item, parent, false);
        return new SongListAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Track track = songList.get(position);
        holder.position = holder.getAbsoluteAdapterPosition();
        holder.artist.setText(track.getTArtist());
        holder.title.setText(track.getTTitle());
        holder.coverImage.setImageDrawable(customCoverImage);
        holder.coverImage.setImageTintList(ContextCompat.getColorStateList(context, R.color.NewcolorSurfaceVariant));
        holder.itemView.setOnClickListener(view -> songListInterface.OnSongSelectedListener(position));

        int pos = holder.getAbsoluteAdapterPosition();
        imagesLoading++;
        holder.coverImage.postDelayed(() -> {
            if (holder.position == pos) {
                Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, track.getTId());
                byte[] thumbnail = null;
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                try {
                    mmr.setDataSource(context, trackUri);
                    thumbnail = mmr.getEmbeddedPicture();
                } catch (IllegalArgumentException e) {
                    System.out.println("MediaMetadataRetriever IllegalArgument");
                } finally {
                    mmr.release();
                    if (thumbnail != null) {
                        Bitmap cover = BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length);
                        holder.coverImage.setClipToOutline(true);
                        holder.coverImage.setImageBitmap(cover);
                        holder.coverImage.setImageTintList(null);
                        Animation fadeIn = new AlphaAnimation(0, 1);
                        fadeIn.setInterpolator(new DecelerateInterpolator());
                        fadeIn.setDuration(350);
                        holder.coverImage.setAnimation(fadeIn);
                    }
                }
            }
            imagesLoading--;
        }, 300 + (30L * imagesLoading));
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

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, artist;
        ImageView coverImage;
        int position;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tracklist_title);
            artist = itemView.findViewById(R.id.tracklist_artist);
            coverImage = itemView.findViewById(R.id.tracklist_cover);
        }
    }
}
