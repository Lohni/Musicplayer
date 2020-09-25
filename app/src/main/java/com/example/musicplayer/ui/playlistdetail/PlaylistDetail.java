package com.example.musicplayer.ui.playlistdetail;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.transition.Explode;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ListView;

import com.example.musicplayer.MainActivity;
import com.example.musicplayer.R;
import com.example.musicplayer.adapter.PlaylistDetailAdapter;
import com.example.musicplayer.adapter.SongListAdapter;
import com.example.musicplayer.entities.MusicResolver;
import com.example.musicplayer.ui.DatabaseViewmodel;
import com.example.musicplayer.ui.playlist.PlaylistInterface;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;


public class PlaylistDetail extends Fragment {

    private ArrayList<MusicResolver> trackList;
    private RecyclerView list;
    private RecyclerView.LayoutManager layoutManager;
    private PlaylistDetailAdapter playlistDetailAdapter;
    private DatabaseViewmodel databaseViewmodel;
    private PlaylistInterface playlistInterface;
    private View snackbar_anchor;
    private Snackbar snackbar;
    private boolean isSnackbarActive = false, undo = false, isSecondDelete=false;

    private String table;

    private int deleteID, oldDeleteID=-1;
    private MusicResolver deletedSong, newDeleteSong;

    public PlaylistDetail() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            playlistInterface = (PlaylistInterface) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + "must implement SongListInterface");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_playlist_detail, container, false);
        list= view.findViewById(R.id.playlistdetail_list);
        snackbar_anchor = view.findViewById(R.id.playlist_detail_snackbar_anchor);
        list.setHasFixedSize(true);
        list.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(requireContext(),R.anim.layout_animation_fall_down));

        trackList=new ArrayList<>();
        databaseViewmodel = new ViewModelProvider(requireActivity()).get(DatabaseViewmodel.class);

        MainActivity mainActivity =(MainActivity) requireActivity();
        table = mainActivity.getSupportActionBar().getTitle().toString();

        databaseViewmodel.fetchTableContent(table).observe(getViewLifecycleOwner(),trackList ->{
            this.trackList.addAll(trackList);
            playlistDetailAdapter.notifyDataSetChanged();
            playlistInterface.OnPlaylistCreatedListener(this.trackList);
        });
        //trackList=databaseViewmodel.fetchTableContent(table).getValue();

        layoutManager = new LinearLayoutManager(requireContext());
        list.setLayoutManager(layoutManager);
        list.addItemDecoration(new DividerItemDecoration(requireContext(),DividerItemDecoration.VERTICAL));
        list.setAdapter(playlistDetailAdapter=new PlaylistDetailAdapter(this.trackList,playlistInterface));
        Paint p = new Paint();
        p.setColor(getResources().getColor(R.color.colorSecondary));
        Bitmap icon = getBitmapFromVectorDrawable(requireContext(),R.drawable.ic_delete_sweep_black_24dp);
        int width =(int) getResources().getDisplayMetrics().widthPixels/4;

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if(direction==ItemTouchHelper.LEFT){
                    if (isSnackbarActive && snackbar != null){
                        isSecondDelete=true;
                        snackbar.dismiss();
                    }
                    if (!isSecondDelete){
                        removeTrack();
                        createSnackbar();
                        isSnackbarActive=true;
                    }
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                if(actionState==ItemTouchHelper.ACTION_STATE_SWIPE){
                    View itemView = viewHolder.itemView;
                    int id = viewHolder.getAdapterPosition();
                    if (id != -1)deleteID=id;
                    if(dX<0){
                        c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                                (float) itemView.getRight(), (float) itemView.getBottom(), p);
                        if (-dX>icon.getWidth()*2){
                            c.drawBitmap(icon,(float) itemView.getRight() - convertDpToPx(16) - icon.getWidth(),
                                    (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - icon.getHeight())/2,
                                    p);
                        }
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(list);
        return view;
    }

    private void createSnackbar(){
        snackbar = Snackbar.make(snackbar_anchor, "song will be deleted!", Snackbar.LENGTH_LONG).setAction("Undo", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                undo=true;
            }
        }).addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
                if (undo){
                    trackList.add(oldDeleteID,deletedSong);
                    playlistDetailAdapter.notifyItemInserted(oldDeleteID);
                } else {
                    databaseViewmodel.deleteTableEntry(table, deletedSong.getId());
                }
                undo=false;
                if (isSecondDelete){
                    isSecondDelete=false;
                    removeTrack();
                    createSnackbar();
                } else isSnackbarActive=false;
            }
        }).setDuration(5000);
        snackbar.show();
    }

    private void removeTrack(){
        deletedSong = trackList.get(deleteID);
        trackList.remove(deleteID);
        playlistDetailAdapter.notifyItemRemoved(deleteID);
        oldDeleteID = deleteID;
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private int convertDpToPx(int dp){
        return Math.round(dp * (getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
