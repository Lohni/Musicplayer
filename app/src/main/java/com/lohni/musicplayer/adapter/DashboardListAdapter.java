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
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.lohni.musicplayer.R;
import com.lohni.musicplayer.database.dto.AlbumDTO;
import com.lohni.musicplayer.database.dto.DashboardDTO;
import com.lohni.musicplayer.database.dto.PlaylistDTO;
import com.lohni.musicplayer.database.dto.TrackDTO;
import com.lohni.musicplayer.database.entity.Track;
import com.lohni.musicplayer.utils.AdapterUtils;
import com.lohni.musicplayer.utils.enums.ListFilterType;

import java.util.HashMap;
import java.util.List;

public class DashboardListAdapter<T extends DashboardDTO> extends RecyclerView.Adapter<DashboardListAdapter<T>.ViewHolder> {
    private final List<T> list;
    private final Drawable customCoverDrawable, backgroundDrawable;
    private final ListFilterType type;
    private final Context context;
    private final HashMap<Integer, Drawable> drawableHashMap = new HashMap<>();

    private final Handler handler = new Handler();
    private Runnable refreshRunnable;

    private OnItemClickListener<T> onItemClickListener;

    public interface OnItemClickListener<T extends DashboardDTO> {
        void onItemClick(T item);
    }

    public DashboardListAdapter(Context context, List<T> list, ListFilterType type) {
        this.context = context;
        this.list = list;
        this.type = type;
        
        customCoverDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_baseline_music_note_24, null);
        backgroundDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.background_button_secondary, null);

        refreshRunnable = () -> {
            notifyItemRangeChanged(0, list.size(), "REFRESH");
            handler.postDelayed(refreshRunnable, 1000);
        };
        refreshRunnable.run();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_list_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        setDefaultBackground(vh);
        return vh;
    }

    private void setDefaultBackground(ViewHolder holder) {
        holder.imageView.setBackground(backgroundDrawable);
        holder.imageView.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorSurfaceLevel4));
        holder.imageView.setForeground(customCoverDrawable);
        holder.imageView.setForegroundTintList(ContextCompat.getColorStateList(context, R.color.colorOnSurface));
    }

    @Override
    public void onBindViewHolder(@NonNull DashboardListAdapter.ViewHolder holder, int position) {
        T object = list.get(position);

        if (object instanceof PlaylistDTO) {
            holder.title.setText(((PlaylistDTO) object).getPlaylist().getPName());
            holder.subTitle.setText(getSubtitleMessage(((PlaylistDTO) object).getSize()));
        } else if (object instanceof TrackDTO) {
            TrackDTO trackDTO = (TrackDTO) object;
            Track track = trackDTO.getTrack();
            holder.title.setText(track.getTTitle());
            holder.subTitle.setText(track.getTArtist());

            String description = AdapterUtils.getDescription(trackDTO, type);
            holder.description.setText(description);
            holder.descriptionContainer.setVisibility((!description.isBlank()) ? View.VISIBLE : View.GONE);
            setViewHolderBackground(holder, track.getTId());

        } else if (object instanceof AlbumDTO) {
            AlbumDTO albumDTO = (AlbumDTO) object;
            holder.title.setText(albumDTO.getAlbum().album.getAName());
            holder.subTitle.setText(albumDTO.getAlbum().trackList.size() + " songs");

            String description = AdapterUtils.getDescription(albumDTO, type);
            holder.description.setText(description);
            holder.descriptionContainer.setVisibility((!description.isBlank()) ? View.VISIBLE : View.GONE);
            setViewHolderBackground(holder, albumDTO.getAlbum().album.getAId());
        }

        holder.root.setOnClickListener((view) ->{
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(list.get(position));
            }
        });
    }

    private void setViewHolderBackground(DashboardListAdapter.ViewHolder holder, Integer positionEntityId) {
        if (drawableHashMap.containsKey(positionEntityId)) {
            Animation fadeIn = new AlphaAnimation(0, 1);
            fadeIn.setInterpolator(new DecelerateInterpolator());
            fadeIn.setDuration(350);
            holder.imageView.setAnimation(fadeIn);
            holder.imageView.setBackground(drawableHashMap.get(positionEntityId));
            holder.imageView.setForeground(null);
        } else {
            setDefaultBackground(holder);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty() && payloads.get(0).equals("REFRESH")) {
            if (list.get(position) instanceof TrackDTO) {
                TrackDTO trackDTO = (TrackDTO) list.get(position);
                String description = AdapterUtils.getDescription(trackDTO, type);
                holder.description.setText(description);
                holder.descriptionContainer.setVisibility((!description.isBlank()) ? View.VISIBLE : View.GONE);
            }
        } else {
            onBindViewHolder(holder, position);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private String getSubtitleMessage(String amount) {
        return (type.equals(ListFilterType.TIMES_PLAYED))
                ? amount + " times played"
                : amount + " songs";
    }

    public void setBackgroundImages(HashMap<Integer, Drawable> images) {
        drawableHashMap.clear();
        drawableHashMap.putAll(images);
        notifyItemRangeChanged(0, list.size());
    }

    public void setOnItemClickListener(OnItemClickListener<T> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public FrameLayout imageView;
        public ConstraintLayout root;
        public TextView title;
        public TextView subTitle;
        public TextView description;
        public FrameLayout descriptionContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.dashboard_list_item_image);
            title = itemView.findViewById(R.id.dashboard_list_item_title);
            subTitle = itemView.findViewById(R.id.dashboard_list_item_subtitle);
            root = itemView.findViewById(R.id.dashboard_list_item_holder);
            description = itemView.findViewById(R.id.dashboard_list_item_desc);
            descriptionContainer = itemView.findViewById(R.id.dashboard_list_item_description_container);
        }
    }
}
