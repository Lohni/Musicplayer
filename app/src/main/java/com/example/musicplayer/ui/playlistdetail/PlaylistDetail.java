package com.example.musicplayer.ui.playlistdetail;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.transition.Explode;
import android.transition.Slide;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.musicplayer.MainActivity;
import com.example.musicplayer.R;
import com.example.musicplayer.adapter.PlaylistDetailAdapter;
import com.example.musicplayer.adapter.SongListAdapter;
import com.example.musicplayer.entities.MusicResolver;
import com.example.musicplayer.ui.DatabaseViewmodel;
import com.example.musicplayer.ui.playlist.PlaylistInterface;
import com.example.musicplayer.utils.NavigationControlInterface;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Objects;


public class PlaylistDetail extends Fragment {

    private ArrayList<MusicResolver> trackList;
    private RecyclerView list;
    private RecyclerView.LayoutManager layoutManager;
    private PlaylistDetailAdapter playlistDetailAdapter;
    private DatabaseViewmodel databaseViewmodel;
    private PlaylistInterface playlistInterface;
    private NavigationControlInterface navigationControlInterface;
    private TextView title, info;
    private ConstraintLayout shuffle;
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
        setHasOptionsMenu(true);
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
        inflater.inflate(R.menu.playlist_detail_add, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.action_playlist_detail_add){
            PlaylistDetailAdd playlistDetailAdd = new PlaylistDetailAdd();
            playlistDetailAdd.setTitle(title.getText().toString());

            Slide anim = new Slide();
            anim.setSlideEdge(Gravity.RIGHT);
            anim.setDuration(200);

            playlistDetailAdd.setEnterTransition(anim);
            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,playlistDetailAdd,"FRAGMENT_PLAYLISTDETAILADD").addToBackStack(null).commit();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_playlist_detail, container, false);
        list= view.findViewById(R.id.playlistdetail_list);
        snackbar_anchor = view.findViewById(R.id.playlist_detail_snackbar_anchor);
        title = view.findViewById(R.id.playlistdetail_title);
        info = view.findViewById(R.id.playlistdetail_duration);
        shuffle = (ConstraintLayout) view.findViewById(R.id.playlistdetail_shuffle);
        list.setHasFixedSize(true);
        list.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(requireContext(),R.anim.layout_animation_fall_down));

        navigationControlInterface.isDrawerEnabledListener(false);
        navigationControlInterface.setHomeAsUpEnabled(true);
        navigationControlInterface.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        navigationControlInterface.setToolbarTitle("");

        trackList=new ArrayList<>();
        databaseViewmodel = new ViewModelProvider(requireActivity()).get(DatabaseViewmodel.class);

        databaseViewmodel.getTableName().observe(getViewLifecycleOwner(), table ->{
            this.table = table;
            title.setText(table);
            info.setText(trackList.size()+ " songs - 00:00");

            databaseViewmodel.fetchTableContent(table, requireContext()).observe(getViewLifecycleOwner(),trackList ->{
                this.trackList.addAll(trackList);
                playlistDetailAdapter.notifyDataSetChanged();
                playlistInterface.OnPlaylistCreatedListener(this.trackList);
            });
        });

        layoutManager = new LinearLayoutManager(requireContext());
        list.setLayoutManager(layoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(requireContext(), R.drawable.recyclerview_divider);
        list.addItemDecoration(dividerItemDecoration);
        list.setAdapter(playlistDetailAdapter=new PlaylistDetailAdapter(this.trackList,playlistInterface));
        Paint p = new Paint();
        p.setColor(ContextCompat.getColor(requireContext(), R.color.colorSecondary));
        Bitmap icon = getBitmapFromVectorDrawable(requireContext(),R.drawable.ic_delete_sweep_black_24dp);

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
        shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playlistInterface.OnShuffle();
            }
        });

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

    public String getTable(){return table;}

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
