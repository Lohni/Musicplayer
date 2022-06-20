package com.example.musicplayer.ui.playlistdetail;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.musicplayer.R;
import com.example.musicplayer.adapter.PlaylistDetailAdapter;
import com.example.musicplayer.database.MusicplayerApplication;
import com.example.musicplayer.database.dao.MusicplayerDataAccess;
import com.example.musicplayer.database.dao.PlaylistDataAccess;
import com.example.musicplayer.database.entity.Playlist;
import com.example.musicplayer.database.entity.PlaylistItem;
import com.example.musicplayer.database.entity.Track;
import com.example.musicplayer.database.viewmodel.MusicplayerViewModel;
import com.example.musicplayer.database.viewmodel.PlaylistViewModel;
import com.example.musicplayer.inter.PlaybackControlInterface;
import com.example.musicplayer.inter.SongInterface;
import com.example.musicplayer.ui.other.CustomDividerItemDecoration;
import com.example.musicplayer.utils.NavigationControlInterface;
import com.example.musicplayer.utils.enums.PlaybackBehaviour;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class PlaylistDetail extends Fragment implements OnStartDragListener {

    private RecyclerView list;
    private RecyclerView.LayoutManager layoutManager;
    private PlaylistDetailAdapter playlistDetailAdapter;
    private NavigationControlInterface navigationControlInterface;
    private TextView info;
    private ConstraintLayout shuffle;
    private View snackbar_anchor;
    private Snackbar snackbar;
    private ItemTouchHelper itemTouchhelper;
    private boolean isSnackbarActive = false, undo = false, isSecondDelete = false;

    private int deleteID, oldDeleteID = -1;
    private Track deletedSong;

    private PlaylistViewModel playlistViewModel;
    private MusicplayerViewModel musicplayerViewModel;

    private Playlist playlist;
    private ArrayList<PlaylistItem> playlistItems;
    private ArrayList<Track> trackList = new ArrayList<>();

    private SongInterface songInterface;
    private PlaybackControlInterface playbackControlInterface;

    private int playlistId;

    public PlaylistDetail() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        playlistId = getArguments().getInt("PLAYLIST_ID");

        setHasOptionsMenu(true);

        navigationControlInterface.isDrawerEnabledListener(false);
        navigationControlInterface.setHomeAsUpEnabled(true);
        navigationControlInterface.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);

        PlaylistDataAccess aod = ((MusicplayerApplication) requireActivity().getApplication()).getDatabase().playlistDao();
        playlistViewModel = new ViewModelProvider(requireActivity(), new PlaylistViewModel.PlaylistViewModelFactory(aod)).get(PlaylistViewModel.class);

        MusicplayerDataAccess mda = ((MusicplayerApplication) requireActivity().getApplication()).getDatabase().musicplayerDao();
        musicplayerViewModel = new ViewModelProvider(this, new MusicplayerViewModel.MusicplayerViewModelFactory(mda)).get(MusicplayerViewModel.class);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            navigationControlInterface = (NavigationControlInterface) context;
            songInterface = (SongInterface) context;
            playbackControlInterface = (PlaybackControlInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + "must implement SongListInterface");
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.playlist_detail_add, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_playlist_detail_add) {
            PlaylistDetailAdd playlistDetailAdd = new PlaylistDetailAdd();
            playlistDetailAdd.setTitle(playlist.getPName());

            Slide anim = new Slide();
            anim.setSlideEdge(Gravity.RIGHT);
            anim.setDuration(200);

            playlistDetailAdd.setEnterTransition(anim);
            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, playlistDetailAdd, "FRAGMENT_PLAYLISTDETAILADD").addToBackStack(null).commit();
        } else if (item.getItemId() == R.id.action_playlist_detail_edit) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext()).setTitle("Rename playlist");

            final EditText input = new EditText(requireContext());
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setText(playlist.getPName());
            builder.setView(input);

            builder.setPositiveButton("Rename", (dialogInterface, i) -> {
                playlist.setPName(input.getText().toString());
                playlistViewModel.renamePlaylist(playlist);

                navigationControlInterface.setToolbarTitle(playlist.getPName());
                Toast.makeText(requireContext(), "Renamed playlist to: " + playlist.getPName(), Toast.LENGTH_SHORT).show();

            });
            builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
            builder.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_playlist_detail, container, false);
        list = view.findViewById(R.id.playlistdetail_list);
        snackbar_anchor = view.findViewById(R.id.playlist_detail_snackbar_anchor);
        info = view.findViewById(R.id.playlistdetail_duration);
        shuffle = view.findViewById(R.id.playlistdetail_shuffle);
        list.setHasFixedSize(true);
        list.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_animation_fall_down));

        CustomDividerItemDecoration dividerItemDecoration = new CustomDividerItemDecoration(requireContext(), R.drawable.recyclerview_divider);
        list.addItemDecoration(dividerItemDecoration);
        list.setAdapter(playlistDetailAdapter = new PlaylistDetailAdapter(requireContext(), this.trackList, songInterface, this));

        playlistViewModel.getPlaylistById(playlistId).observe(getViewLifecycleOwner(), target -> {
            this.playlist = target;
            navigationControlInterface.setToolbarTitle(playlist.getPName());
        });

        playlistViewModel.getPlaylistItemsByPlaylistId(playlistId).observe(getViewLifecycleOwner(), items -> {
            this.playlistItems = new ArrayList<>();
            this.playlistItems.addAll(items);

            List<Integer> trackIdList = items.stream()
                    .map(PlaylistItem::getPiTId).collect(Collectors.toList());

            musicplayerViewModel.getTracksByIds(trackIdList).observe(getViewLifecycleOwner(), tracks -> {
                this.trackList.clear();
                this.trackList.addAll(tracks);

                playlistDetailAdapter.notifyItemRangeChanged(0, trackList.size());

                long time = tracks.stream().map(Track::getTDuration).reduce(0, Integer::sum).longValue();
                String infoText = tracks.size() + " songs - " + convertTime(time);
                info.setText(infoText);
            });
        });

        layoutManager = new LinearLayoutManager(requireContext());
        list.setLayoutManager(layoutManager);


        Paint p = new Paint();
        p.setColor(ContextCompat.getColor(requireContext(), R.color.colorSecondary));
        Bitmap icon = getBitmapFromVectorDrawable(requireContext(), R.drawable.ic_delete_sweep_black_24dp);

        //Todo: Drag/ change order
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getBindingAdapterPosition();
                int toPosition = target.getBindingAdapterPosition();

                if (fromPosition < toPosition) {
                    for (int i = fromPosition; i < toPosition; i++) {
                        Collections.swap(trackList, i, i + 1);
                        Collections.swap(playlistItems, i, i + 1);
                    }
                } else {
                    for (int i = fromPosition; i > toPosition; i--) {
                        Collections.swap(trackList, i, i - 1);
                        Collections.swap(playlistItems, i, i - 1);
                    }
                }

                playlistDetailAdapter.notifyItemMoved(fromPosition, toPosition);

                //Todo: recalc custom ordinal
                for (int i = 0; i < trackList.size(); i++) {
                    playlistItems.get(i).setPiCustomOrdinal(i);
                }

                playlistViewModel.updatePlaylistItemList(playlistItems);
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.LEFT) {
                    if (isSnackbarActive && snackbar != null) {
                        isSecondDelete = true;
                        snackbar.dismiss();
                    }
                    if (!isSecondDelete) {
                        removeTrack();
                        createSnackbar();
                        isSnackbarActive = true;
                    }
                }
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return true;
                //return super.isLongPressDragEnabled();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;
                    int id = viewHolder.getBindingAdapterPosition();

                    if (id != -1) deleteID = id;
                    if (dX < 0) {
                        c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                                (float) itemView.getRight(), (float) itemView.getBottom(), p);


                        if (-dX > icon.getWidth() * 2) {
                            c.drawBitmap(icon, (float) itemView.getRight() - convertDpToPx(16) - icon.getWidth(),
                                    (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - icon.getHeight()) / 2,
                                    p);
                        }
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        itemTouchhelper = new ItemTouchHelper(simpleCallback);
        itemTouchhelper.attachToRecyclerView(list);

        shuffle.setOnClickListener(shuffleView -> playbackControlInterface.onPlaybackBehaviourChangeListener(PlaybackBehaviour.PlaybackBehaviourState.SHUFFLE));
        return view;
    }

    private void createSnackbar() {
        snackbar = Snackbar.make(snackbar_anchor, "song will be deleted!", Snackbar.LENGTH_LONG).setAction("Undo", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                undo = true;
            }
        }).addCallback(new BaseTransientBottomBar.BaseCallback<>() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
                if (undo) {
                    trackList.add(oldDeleteID, deletedSong);
                    playlistDetailAdapter.notifyItemInserted(oldDeleteID);
                } else {
                    Optional<PlaylistItem> itemToDelete = playlistItems.stream()
                            .filter(item -> item.getPiTId().equals(deletedSong.getTId()))
                            .findFirst();

                    itemToDelete.ifPresent(playlistItem -> playlistViewModel.deletePlaylistItem(playlistItem));
                }
                undo = false;
                if (isSecondDelete) {
                    isSecondDelete = false;
                    removeTrack();
                    createSnackbar();
                } else isSnackbarActive = false;
            }
        }).setDuration(5000);
        snackbar.show();
    }

    private void removeTrack() {
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

    private int convertDpToPx(int dp) {
        return Math.round(dp * (getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public String getTable() {
        return playlist.getPName();
    }

    private String convertTime(long duration) {
        float d = (float) duration / (1000 * 60);
        int min = (int) d;
        float seconds = (d - min) * 60;
        int sec = (int) seconds;
        String minute = min + "", second = sec + "";
        if (min < 10) minute = "0" + minute;
        if (sec < 10) second = "0" + second;
        return minute + ":" + second;
    }

    @Override
    public void onStartDrag(PlaylistDetailAdapter.ViewHolder viewHolder) {
        itemTouchhelper.startDrag(viewHolder);
    }
}
