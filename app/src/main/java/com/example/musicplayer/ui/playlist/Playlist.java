package com.example.musicplayer.ui.playlist;


import android.content.ClipData;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.example.musicplayer.MainActivity;
import com.example.musicplayer.R;
import com.example.musicplayer.adapter.PlaylistAdapter;
import com.example.musicplayer.ui.DatabaseViewmodel;

import java.util.ArrayList;



public class Playlist extends Fragment {

    private PlaylistAdapter mAdapter;
    private RecyclerView playlist;
    private RecyclerView.LayoutManager layoutManager;

    private DatabaseViewmodel databaseViewmodel;

    private int deleteID;
    private PlaylistInterface playlistInterface;

    private ArrayList<String> playlist_list,playlist_size;

    public Playlist() {
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
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);
        playlist = view.findViewById(R.id.playlist);
        playlist.setHasFixedSize(true);

        MainActivity mainActivity =(MainActivity) requireActivity();
        mainActivity.getSupportActionBar().setTitle("Playlist");

        playlist_list=new ArrayList<>();
        playlist_size=new ArrayList<>();
        mAdapter = new PlaylistAdapter(playlist_list,playlist_size,playlistInterface);

        databaseViewmodel = new ViewModelProvider(requireActivity()).get(DatabaseViewmodel.class);
        databaseViewmodel.fetchTables().observe(getViewLifecycleOwner(),allTables -> {
            playlist_list.addAll(allTables);
            mAdapter.notifyDataSetChanged();
        });
        databaseViewmodel.fetchTableSizes().observe(getViewLifecycleOwner(),allTablesSizes ->{
            Log.e("LOADED",""+allTablesSizes);
            playlist_size.addAll(allTablesSizes);
            mAdapter.notifyDataSetChanged();
        });

        layoutManager = new LinearLayoutManager(requireContext());
        playlist.setLayoutManager(layoutManager);

        playlist.addItemDecoration(new DividerItemDecoration(requireContext(),DividerItemDecoration.VERTICAL));
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
                    databaseViewmodel.deleteTable(playlist_list.get(deleteID));
                    playlist_list.remove(deleteID);
                    playlist_size.remove(deleteID);
                    mAdapter.notifyItemRemoved(deleteID);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                if(actionState==ItemTouchHelper.ACTION_STATE_SWIPE){
                    View itemView = viewHolder.itemView;
                    deleteID=viewHolder.getAdapterPosition();
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
        itemTouchHelper.attachToRecyclerView(playlist);
        playlist.setAdapter(mAdapter);

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        playlistInterface.OnPlaylistResumeListener();
    }


    private static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
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
