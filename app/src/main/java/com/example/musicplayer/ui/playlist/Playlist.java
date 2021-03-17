package com.example.musicplayer.ui.playlist;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;


import com.example.musicplayer.MainActivity;
import com.example.musicplayer.R;
import com.example.musicplayer.adapter.PlaylistAdapter;
import com.example.musicplayer.ui.DatabaseViewmodel;
import com.example.musicplayer.ui.playlistdetail.PlaylistDetail;
import com.example.musicplayer.utils.NavigationControlInterface;
import com.example.musicplayer.utils.Permissions;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.transition.MaterialContainerTransform;
import com.google.android.material.transition.MaterialElevationScale;

import java.util.ArrayList;



public class Playlist extends Fragment {

    private PlaylistAdapter mAdapter;
    private RecyclerView playlist;
    private RecyclerView.LayoutManager layoutManager;
    private View snackbar_anchor;

    private DatabaseViewmodel databaseViewmodel;

    private int playlistID, deleteID;
    private boolean undo=false, isSnackbarActive=false;
    private PlaylistInterface playlistInterface;
    private NavigationControlInterface navigationControlInterface;
    private String deletedTrack, deletedSize;
    private ArrayList<String> playlist_list,playlist_size;

    public Playlist() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setExitTransition(new MaterialElevationScale(false));
        setReenterTransition(new MaterialElevationScale(true));
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            navigationControlInterface = (NavigationControlInterface) context;
            playlistInterface = (PlaylistInterface) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + "must implement SongListInterface");
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.playlist_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.action_playlist_add){
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext()).setTitle("New playlist");

            final EditText input = new EditText(requireContext());
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String newTable = input.getText().toString();
                    databaseViewmodel.createNewTable(newTable);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            builder.show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);
        playlist = view.findViewById(R.id.playlist);
        snackbar_anchor = view.findViewById(R.id.snackbar_anchor);
        playlist.setHasFixedSize(true);

        navigationControlInterface.setHomeAsUpEnabled(false);
        navigationControlInterface.isDrawerEnabledListener(true);
        navigationControlInterface.setToolbarTitle("Playlist");

        Permissions.permission(requireActivity(), this, Manifest.permission.READ_EXTERNAL_STORAGE);

        playlist_list=new ArrayList<>();
        playlist_size=new ArrayList<>();
        mAdapter = new PlaylistAdapter(playlist_list,playlist_size,playlistInterface);

        databaseViewmodel = new ViewModelProvider(requireActivity()).get(DatabaseViewmodel.class);
        databaseViewmodel.fetchTables().observe(getViewLifecycleOwner(),allTables -> {
            if (playlist_list.size() > 0)playlist_list.clear();
            playlist_list.addAll(allTables);
            mAdapter.notifyDataSetChanged();
        });
        databaseViewmodel.fetchTableSizes().observe(getViewLifecycleOwner(),allTablesSizes ->{
            Log.e("LOADED",""+allTablesSizes);
            if (playlist_size.size() > 0)playlist_size.clear();
            playlist_size.addAll(allTablesSizes);
            mAdapter.notifyDataSetChanged();
        });

        layoutManager = new LinearLayoutManager(requireContext());
        playlist.setLayoutManager(layoutManager);

        Playlist.DividerItemDecoration dividerItemDecoration = new Playlist.DividerItemDecoration(requireContext(), R.drawable.recyclerview_divider);
        playlist.addItemDecoration(dividerItemDecoration);
        Paint p = new Paint();
        p.setColor(ContextCompat.getColor(requireContext(), R.color.colorSecondary));
        Bitmap icon = getBitmapFromVectorDrawable(requireContext(),R.drawable.ic_delete_sweep_black_24dp);

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                if (isSnackbarActive)return 0;
                else return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if(direction==ItemTouchHelper.LEFT && !isSnackbarActive){
                    deleteID = playlistID;
                    deletedTrack = playlist_list.get(playlistID);
                    deletedSize = playlist_size.get(playlistID);
                    playlist_list.remove(playlistID);
                    playlist_size.remove(playlistID);
                    mAdapter.notifyItemRemoved(playlistID);
                    isSnackbarActive=true;
                    mAdapter.setOnItemClickEnabled(false);
                    Snackbar.make(view,"Playlist " + deletedTrack + " will be deleted!",Snackbar.LENGTH_LONG).setAnchorView(snackbar_anchor).setAction("Undo", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            undo=true;
                            isSnackbarActive=false;
                        }
                    }).addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            super.onDismissed(transientBottomBar, event);
                            if (!undo){
                                databaseViewmodel.deleteTable(deletedTrack);
                            } else {
                                playlist_list.add(deleteID,deletedTrack);
                                playlist_size.add(deleteID,deletedSize);
                                mAdapter.notifyItemInserted(deleteID);
                                undo=false;
                            }
                            mAdapter.setOnItemClickEnabled(true);
                            isSnackbarActive=false;
                        }
                    }).setDuration(5000).show();
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                if(actionState==ItemTouchHelper.ACTION_STATE_SWIPE && !isSnackbarActive){
                    View itemView = viewHolder.itemView;
                    playlistID =viewHolder.getAdapterPosition();
                    if(dX<0){
                        c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                                (float) itemView.getRight(), (float) itemView.getBottom(), p);
                        if (-dX>icon.getWidth()*2){
                            c.drawBitmap(icon,(float) itemView.getRight() - convertDpToPx(16) - icon.getWidth(),
                                    (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - icon.getHeight())/2,
                                    p);
                        }
                    }
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                } else {
                    viewHolder.itemView.setTranslationX(0);
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(playlist);
        playlist.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        postponeEnterTransition();
        ((ViewGroup) view.getParent()).getViewTreeObserver()
                .addOnPreDrawListener(() -> {
                    startPostponedEnterTransition();
                    return true;
                });
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

    private class DividerItemDecoration extends RecyclerView.ItemDecoration{

        private Drawable divider;
        private int paddingLeft, paddingRight;

        public DividerItemDecoration(Context context, int resId) {
            divider = ContextCompat.getDrawable(context, resId);
            paddingLeft = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, context.getResources().getDisplayMetrics());
            paddingRight = paddingLeft;
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {

            int childCount = parent.getChildCount() - 1;
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);

                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + divider.getIntrinsicHeight();

                divider.setBounds(paddingLeft, top, parent.getWidth() - paddingRight, bottom);
                divider.draw(c);
            }
        }
    }
}
