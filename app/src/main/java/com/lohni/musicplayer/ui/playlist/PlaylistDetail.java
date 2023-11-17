package com.lohni.musicplayer.ui.playlist;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.InputType;
import android.transition.Slide;
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

import com.lohni.musicplayer.R;
import com.lohni.musicplayer.adapter.PlaylistDetailAdapter;
import com.lohni.musicplayer.core.ApplicationDataViewModel;
import com.lohni.musicplayer.core.DeleteSnackbar;
import com.lohni.musicplayer.database.MusicplayerApplication;
import com.lohni.musicplayer.database.dao.PlaylistDataAccess;
import com.lohni.musicplayer.database.dto.PlaylistItemDTO;
import com.lohni.musicplayer.database.entity.Playlist;
import com.lohni.musicplayer.database.entity.PlaylistPlayed;
import com.lohni.musicplayer.database.entity.Track;
import com.lohni.musicplayer.database.viewmodel.PlaylistDetailViewModel;
import com.lohni.musicplayer.database.viewmodel.PlaylistViewModel;
import com.lohni.musicplayer.interfaces.NavigationControlInterface;
import com.lohni.musicplayer.interfaces.OnStartDragListener;
import com.lohni.musicplayer.interfaces.PlaybackControlInterface;
import com.lohni.musicplayer.interfaces.QueueControlInterface;
import com.lohni.musicplayer.utils.GeneralUtils;
import com.lohni.musicplayer.utils.enums.PlaybackBehaviour;
import com.lohni.musicplayer.utils.images.ImageUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class PlaylistDetail extends Fragment implements OnStartDragListener, PlaylistDetailAdapter.PlaylistClickListener {

    private PlaylistDetailAdapter playlistDetailAdapter;
    private NavigationControlInterface navigationControlInterface;
    private DeleteSnackbar<PlaylistItemDTO> snackbar;
    private ItemTouchHelper itemTouchhelper;
    private PlaylistViewModel playlistViewModel;
    private PlaylistDetailViewModel playlistDetailViewModel;
    private ApplicationDataViewModel applicationDataViewModel;
    private QueueControlInterface songInterface;
    protected PlaybackControlInterface playbackControlInterface;
    private final ArrayList<PlaylistItemDTO> playlistItemList = new ArrayList<>();
    private int deleteID = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int playlistId = getArguments().getInt("PLAYLIST_ID");

        PlaylistDataAccess pda = ((MusicplayerApplication) requireActivity().getApplication()).getDatabase().playlistDao();
        playlistViewModel = new ViewModelProvider(requireActivity(), new PlaylistViewModel.PlaylistViewModelFactory(pda)).get(PlaylistViewModel.class);
        playlistDetailViewModel = new ViewModelProvider(requireActivity(), new PlaylistDetailViewModel.PlaylistDetailViewModelFactory(pda)).get(PlaylistDetailViewModel.class);
        applicationDataViewModel = new ViewModelProvider(requireActivity()).get(ApplicationDataViewModel.class);

        playlistDetailViewModel.setPlaylistById(playlistId);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            navigationControlInterface = (NavigationControlInterface) context;
            songInterface = (QueueControlInterface) context;
            playbackControlInterface = (PlaybackControlInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + "must implement interface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        recalcOrdinal();
    }

    @Override
    public void onStart() {
        super.onStart();

        setHasOptionsMenu(true);
        navigationControlInterface.isDrawerEnabledListener(false);
        navigationControlInterface.setHomeAsUpEnabled(true);
        navigationControlInterface.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
    }

    @Override
    public void onResume() {
        super.onResume();
        recalcOrdinal();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.playlist_detail_add, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        playlistDetailViewModel.observeOnce(playlistDetailViewModel.getPlaylist(), getViewLifecycleOwner(), playlist -> {
            playlistDetailViewModel.getPlaylist().removeObservers(this);

            if (item.getItemId() == R.id.action_playlist_detail_add) {
                PlaylistDetailAdd playlistDetailAdd = new PlaylistDetailAdd();

                Bundle bundle = new Bundle();
                bundle.putInt("PLAYLIST_ID", playlist.getPId());
                bundle.putInt("PLAYLIST_SIZE", playlistDetailAdapter.getItemCount());
                playlistDetailAdd.setArguments(bundle);

                Slide anim = new Slide();
                anim.setSlideEdge(Gravity.END);
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
        });
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_playlist_detail, container, false);
        RecyclerView list = view.findViewById(R.id.playlistdetail_list);
        View snackbar_anchor = view.findViewById(R.id.playlist_detail_snackbar_anchor);
        TextView info = view.findViewById(R.id.playlistdetail_duration);
        ConstraintLayout shuffle = view.findViewById(R.id.playlistdetail_shuffle);
        list.setHasFixedSize(true);
        list.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_animation_fall_down));

        list.setAdapter(playlistDetailAdapter = new PlaylistDetailAdapter(requireContext(), playlistItemList, this, this));

        playlistDetailViewModel.getPlaylist().observe(getViewLifecycleOwner(), playlist -> {
            navigationControlInterface.setToolbarTitle(playlist.getPName());
        });

        applicationDataViewModel.getTrackImages().observe(getViewLifecycleOwner(), drawables -> {
            playlistDetailAdapter.setDrawableHashMap(drawables);
        });

        playlistDetailViewModel.getPlaylistItems().observe(getViewLifecycleOwner(), playlistItemDtos -> {
            int sizePreClear = playlistItemList.size();
            playlistItemList.clear();
            playlistDetailAdapter.notifyItemRangeRemoved(0, sizePreClear);
            playlistItemList.addAll(playlistItemDtos);
            playlistDetailAdapter.notifyItemRangeInserted(0, playlistItemDtos.size());

            int time = playlistItemDtos.stream().map(PlaylistItemDTO::getTrack).map(Track::getTDuration).reduce(0, Integer::sum);
            String infoText = playlistItemDtos.size() + " songs - " + GeneralUtils.convertTime(time);
            info.setText(infoText);
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(requireContext());
        list.setLayoutManager(layoutManager);
        list.setItemAnimator(null);

        Paint p = new Paint();
        p.setColor(ContextCompat.getColor(requireContext(), R.color.colorSecondary));
        Bitmap icon = ImageUtil.getBitmapFromVectorDrawable(requireContext(), R.drawable.ic_delete_sweep_black_24dp);

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getBindingAdapterPosition();
                int toPosition = target.getBindingAdapterPosition();

                if (fromPosition < toPosition) {
                    for (int i = fromPosition; i < toPosition; i++) {
                        Collections.swap(playlistItemList, i, i + 1);
                    }
                } else {
                    for (int i = fromPosition; i > toPosition; i--) {
                        Collections.swap(playlistItemList, i, i - 1);
                    }
                }

                playlistDetailAdapter.notifyItemMoved(fromPosition, toPosition);
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.LEFT) {
                    addToSnackbar(snackbar_anchor);
                }
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return true;
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
                            c.drawBitmap(icon, (float) itemView.getRight() - ImageUtil.convertDpToPixel(16f, getResources()) - icon.getWidth(),
                                    (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - icon.getHeight()) / 2, p);
                        }
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        itemTouchhelper = new ItemTouchHelper(simpleCallback);
        itemTouchhelper.attachToRecyclerView(list);

        shuffle.setOnClickListener(shuffleView -> {
            playlistDetailViewModel.observeOnce(playlistDetailViewModel.getPlaylist(), getViewLifecycleOwner(), playlist -> {
                playbackControlInterface.onPlaybackBehaviourChangeListener(PlaybackBehaviour.SHUFFLE);
                playbackControlInterface.onNextClickListener();
                onPlaylistPlayed(playlist);
            });
        });

        snackbar = new DeleteSnackbar<>(5000);
        snackbar.setOnUndoListener(toUndo -> {
            toUndo.stream().sorted((Comparator.comparing(o -> o.getPlaylistItem().getPiCustomOrdinal())))
                    .forEach(item -> {
                        playlistItemList.add(item.getPlaylistItem().getPiCustomOrdinal(), item);
                        playlistDetailAdapter.notifyItemInserted(item.getPlaylistItem().getPiCustomOrdinal());
                    });
        });

        snackbar.setOnDissmissedListener(toDelete -> {
            playlistViewModel.deletePlaylistItemList(toDelete.stream().map(PlaylistItemDTO::getPlaylistItem).collect(Collectors.toList()));
            recalcOrdinal();
        });

        return view;
    }

    private void recalcOrdinal() {
        for (int i = 0; i < playlistItemList.size(); i++) {
            playlistItemList.get(i).getPlaylistItem().setPiCustomOrdinal(i);
        }

        playlistViewModel.updatePlaylistItemList(playlistItemList.stream().map(PlaylistItemDTO::getPlaylistItem).collect(Collectors.toList()));
    }

    private void addToSnackbar(View anchor) {
        snackbar.addToDelete(playlistItemList.get(deleteID), anchor);
        playlistItemList.remove(deleteID);
        playlistDetailAdapter.notifyItemRemoved(deleteID);
    }

    @Override
    public void onStartDrag(PlaylistDetailAdapter.ViewHolder viewHolder) {
        itemTouchhelper.startDrag(viewHolder);
    }

    @Override
    public void onAdapterItemClickListener(int position) {
        playlistDetailViewModel.observeOnce(playlistDetailViewModel.getPlaylist(), getViewLifecycleOwner(), playlist -> {
            List<Track> trackList = playlistItemList.stream().map(PlaylistItemDTO::getTrack).collect(Collectors.toList());
            songInterface.onSongListCreatedListener(trackList, playlist, false);
            songInterface.onSongSelectedListener(trackList.get(position));
            onPlaylistPlayed(playlist);
        });
    }

    private void onPlaylistPlayed(Playlist playlist) {
        PlaylistPlayed playlistPlayed = new PlaylistPlayed();
        playlistPlayed.setPpPId(playlist.getPId());
        playlistPlayed.setPpPlayed(GeneralUtils.getCurrentUTCTimestamp());
        playlistViewModel.insertPlaylistPlayed(playlistPlayed);
    }
}
