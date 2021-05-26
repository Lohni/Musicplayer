package com.example.musicplayer.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.musicplayer.R;
import com.example.musicplayer.entities.AlbumResolver;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<AlbumResolver> albumList;
    private final Drawable customCoverImage;
    AlbumAdapterCallback albumAdapterCallback;
    private MediaOptionsAdapter.MediaOptionsAdapterListener mediaOptionsAdapterListener;
    private final LayoutInflater layoutInflater;
    private final PopupWindow popupWindow;
    private final RequestOptions requestOptions;

    private int sharedElementsPosition = 0;

    public interface AlbumAdapterCallback{
        void onLayoutClickListener(ViewHolder viewHolder, AlbumResolver album, int position);
        void onSharedElementsViewCreated();
    }

    @SuppressLint("ServiceCast")
    public AlbumAdapter(Context c, ArrayList<AlbumResolver> albumList, AlbumAdapterCallback albumAdapterCallback, MediaOptionsAdapter.MediaOptionsAdapterListener mediaOptionsAdapterListener, int sharedElementsPosition){
        this.context = c;
        this.albumList = albumList;
        this.albumAdapterCallback = albumAdapterCallback;
        customCoverImage = ResourcesCompat.getDrawable(c.getResources(),R.drawable.ic_album_black_24dp, null);
        popupWindow = new PopupWindow();
        this.sharedElementsPosition = sharedElementsPosition;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        requestOptions = new RequestOptions().error(R.drawable.ic_album_black_24dp)
                .format(DecodeFormat.PREFER_RGB_565);
        this.mediaOptionsAdapterListener = mediaOptionsAdapterListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.albumitem, parent, false);
        ViewHolder holder =  new AlbumAdapter.ViewHolder(v);
        holder.albumCover.setImageDrawable(customCoverImage);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AlbumResolver albumResolver = albumList.get(position);
        holder.albumName.setText(albumResolver.getAlbumName());
        holder.albumSize.setText(albumResolver.getNumSongs() + " songs");

        Glide.with(context)
                .load(albumResolver.getAlbumArtUri())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .apply(requestOptions)
                .override(holder.albumCover.getWidth(), holder.albumCover.getHeight())
                .into(holder.albumCover);

        holder.albumOptions.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View view) {
                if (!holder.isTransitionInEndState()){
                    holder.albumOptions.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.anim_more_vert_to_up));
                    AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) holder.albumOptions.getDrawable();
                    animatedVectorDrawable.start();

                    View linearLayout = layoutInflater.inflate(R.layout.popup_layout, null);
                    RecyclerView menu = linearLayout.findViewById(R.id.album_menu);
                    menu.setLayoutManager(new LinearLayoutManager(context));
                    menu.setHasFixedSize(true);
                    menu.setAdapter(new MediaOptionsAdapter(context, mediaOptionsAdapterListener, position));

                    linearLayout.setAnimation(AnimationUtils.loadAnimation(context, R.anim.popupwindow_show));
                    popupWindow.setContentView(linearLayout);
                    popupWindow.setOutsideTouchable(true);
                    popupWindow.setFocusable(true);
                    popupWindow.setTouchInterceptor(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            if (motionEvent.getX() < 0 || motionEvent.getX() > popupWindow.getWidth()){
                                closePopupWindow(holder);
                                return true;
                            }
                            if (motionEvent.getY() < 0 || motionEvent.getY() > popupWindow.getHeight()){
                                closePopupWindow(holder);
                                return true;
                            }
                            return false;
                        }
                    });
                    popupWindow.showAtLocation(holder.albumCover, Gravity.LEFT | Gravity.TOP, 0 ,0);
                    popupWindow.update(holder.albumCover,0,holder.albumCover.getHeight(),holder.albumCover.getWidth(), holder.albumCover.getHeight());

                }
                holder.setTransitionInEndState(!holder.isTransitionInEndState());
            }
        });

        holder.constraintLayout.setOnClickListener((view -> {
            holder.albumCover.setTransitionName(context.getResources().getString(R.string.transition_album_cover));
            holder.albumName.setTransitionName(context.getResources().getString(R.string.transition_album_name));
            holder.albumSize.setTransitionName(context.getResources().getString(R.string.transition_album_size));
            albumAdapterCallback.onLayoutClickListener(holder, albumList.get(position), position);
        }));
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (holder.getBindingAdapterPosition() == sharedElementsPosition){
            holder.albumCover.setTransitionName(context.getResources().getString(R.string.transition_album_cover));
            holder.albumName.setTransitionName(context.getResources().getString(R.string.transition_album_name));
            holder.albumSize.setTransitionName(context.getResources().getString(R.string.transition_album_size));
            albumAdapterCallback.onSharedElementsViewCreated();
        }
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public AlbumResolver getItem(int position){
        return albumList.get(position);
    }

    public void closePopupWindow(ViewHolder holder){
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.popupwindow_dismiss);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                holder.setTransitionInEndState(!holder.isTransitionInEndState());
                popupWindow.dismiss();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        popupWindow.getContentView().startAnimation(animation);
        holder.albumOptions.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.anim_up_to_more_vert));
        AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) holder.albumOptions.getDrawable();
        animatedVectorDrawable.start();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView albumSize;
        public MaterialTextView albumName;
        private ImageButton albumOptions;
        public ImageView albumCover;
        private boolean transitionInEndState = false;
        private ConstraintLayout constraintLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            albumName = itemView.findViewById(R.id.albumItem_name);
            albumSize = itemView.findViewById(R.id.albumItem_size);
            albumCover = itemView.findViewById(R.id.albumItem_cover);
            albumOptions = itemView.findViewById(R.id.albumItem_options);
            constraintLayout = itemView.findViewById(R.id.album_motionlayout);
        }

        public boolean isTransitionInEndState() {
            return transitionInEndState;
        }

        public void setTransitionInEndState(boolean transitionInEndState) {
            this.transitionInEndState = transitionInEndState;
        }
    }
}
