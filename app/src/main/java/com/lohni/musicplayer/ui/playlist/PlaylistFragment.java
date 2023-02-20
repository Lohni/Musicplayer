package com.lohni.musicplayer.ui.playlist;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.lohni.musicplayer.R;
import com.lohni.musicplayer.adapter.PlaylistAdapter;
import com.lohni.musicplayer.database.MusicplayerApplication;
import com.lohni.musicplayer.database.dao.PlaylistDataAccess;
import com.lohni.musicplayer.database.dto.PlaylistDTO;
import com.lohni.musicplayer.database.entity.Playlist;
import com.lohni.musicplayer.database.viewmodel.PlaylistViewModel;
import com.lohni.musicplayer.interfaces.PlaylistInterface;
import com.lohni.musicplayer.ui.views.CustomDividerItemDecoration;
import com.lohni.musicplayer.interfaces.NavigationControlInterface;
import com.lohni.musicplayer.utils.GeneralUtils;
import com.lohni.musicplayer.utils.Permissions;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.transition.MaterialElevationScale;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class PlaylistFragment extends Fragment implements PlaylistInterface {

    private PlaylistAdapter mAdapter;
    private RecyclerView playlist;
    private RecyclerView.LayoutManager layoutManager;
    private View snackbar_anchor;

    private int playlistID, deleteID;
    private boolean undo = false, isSnackbarActive = false;
    private PlaylistInterface playlistInterface;
    private NavigationControlInterface navigationControlInterface;
    private PlaylistDTO deletedTrack;
    private ArrayList<PlaylistDTO> playlistWithSize;
    private PlaylistViewModel playlistViewModel;

    private boolean isInsert = false, isStartup = true;

    public PlaylistFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setExitTransition(new MaterialElevationScale(false));
        setReenterTransition(new MaterialElevationScale(true));

        playlistWithSize = new ArrayList<>();

        PlaylistDataAccess aod = ((MusicplayerApplication) requireActivity().getApplication()).getDatabase().playlistDao();
        playlistViewModel = new ViewModelProvider(requireActivity(), new PlaylistViewModel.PlaylistViewModelFactory(aod)).get(PlaylistViewModel.class);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        navigationControlInterface = (NavigationControlInterface) context;
        playlistInterface = this;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.playlist_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_playlist_add) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext()).setTitle("New playlist");

            final EditText input = new EditText(requireContext());
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("Create", (dialogInterface, i) -> {
                Playlist playlist = new Playlist();
                playlist.setPName(input.getText().toString());
                playlist.setPCustomOrdinal(playlistWithSize.size());
                playlist.setPCreated(GeneralUtils.getCurrentUTCTimestamp());
                playlistViewModel.insertPlaylist(playlist);
                isInsert = true;
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
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);
        playlist = view.findViewById(R.id.playlist);
        snackbar_anchor = view.findViewById(R.id.snackbar_anchor);
        playlist.setHasFixedSize(true);

        playlistViewModel.getAllPlaylistsWithSize().observe(getViewLifecycleOwner(), list -> {
            playlistWithSize.clear();
            playlistWithSize.addAll(list);

            if (isInsert) {
                mAdapter.notifyItemInserted(list.size() - 1);
                isInsert = false;
            } else if (isStartup) {
                mAdapter.notifyItemRangeInserted(0, list.size());
                isStartup = false;
            }
        });

        navigationControlInterface.setHomeAsUpEnabled(false);
        navigationControlInterface.isDrawerEnabledListener(true);
        navigationControlInterface.setToolbarTitle("Playlist");

        Permissions.permission(requireActivity(), this, Manifest.permission.READ_EXTERNAL_STORAGE);

        mAdapter = new PlaylistAdapter(playlistWithSize, playlistInterface);

        layoutManager = new LinearLayoutManager(requireContext());
        playlist.setLayoutManager(layoutManager);

        CustomDividerItemDecoration dividerItemDecoration = new CustomDividerItemDecoration(requireContext(), R.drawable.recyclerview_divider);
        playlist.addItemDecoration(dividerItemDecoration);
        Paint p = new Paint();
        p.setColor(ContextCompat.getColor(requireContext(), R.color.colorSecondary));
        Bitmap icon = getBitmapFromVectorDrawable(requireContext(), R.drawable.ic_delete_sweep_black_24dp);

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                if (isSnackbarActive) return 0;
                else return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.LEFT && !isSnackbarActive) {
                    deleteID = playlistID;
                    deletedTrack = playlistWithSize.get(playlistID);
                    PlaylistDTO toDelete = playlistWithSize.get(playlistID);
                    playlistWithSize.remove(playlistID);
                    mAdapter.notifyItemRemoved(playlistID);
                    isSnackbarActive = true;
                    mAdapter.setOnItemClickEnabled(false);
                    Snackbar.make(view, "Playlist " + deletedTrack.getPlaylist().getPName() + " will be deleted!", Snackbar.LENGTH_LONG).setAnchorView(snackbar_anchor).setAction("Undo", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            undo = true;
                            isSnackbarActive = false;
                        }
                    }).addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            super.onDismissed(transientBottomBar, event);
                            if (!undo) {
                                playlistViewModel.deletePlaylist(toDelete.getPlaylist());
                            } else {
                                playlistWithSize.add(toDelete);
                                mAdapter.notifyItemInserted(deleteID);
                                undo = false;
                            }
                            mAdapter.setOnItemClickEnabled(true);
                            isSnackbarActive = false;
                        }
                    }).setDuration(5000).show();
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && !isSnackbarActive) {
                    View itemView = viewHolder.itemView;
                    playlistID = viewHolder.getAdapterPosition();
                    if (dX < 0) {
                        c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                                (float) itemView.getRight(), (float) itemView.getBottom(), p);
                        if (-dX > icon.getWidth() * 2) {
                            c.drawBitmap(icon, (float) itemView.getRight() - convertDpToPx(16) - icon.getWidth(),
                                    (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - icon.getHeight()) / 2, p);
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
    public void OnClickListener(Integer playlistID, View view) {
        PlaylistDetail playlistDetailFragment = new PlaylistDetail();

        Bundle bundle = new Bundle();
        bundle.putInt("PLAYLIST_ID", playlistID);
        playlistDetailFragment.setArguments(bundle);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, playlistDetailFragment, getString(R.string.fragment_playlist_detail))
                .addToBackStack(null).commit();
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

    private int convertDpToPx(int dp) {
        return Math.round(dp * (getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
