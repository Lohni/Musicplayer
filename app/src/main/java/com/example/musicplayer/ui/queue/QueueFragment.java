package com.example.musicplayer.ui.queue;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.musicplayer.R;
import com.example.musicplayer.adapter.QueueAdapter;
import com.example.musicplayer.database.entity.Track;
import com.example.musicplayer.helper.DragItemTouchHelper;
import com.example.musicplayer.interfaces.PlaybackControlInterface;
import com.example.musicplayer.interfaces.ServiceTriggerInterface;
import com.example.musicplayer.interfaces.SongInterface;
import com.example.musicplayer.interfaces.NavigationControlInterface;
import com.example.musicplayer.utils.enums.PlaybackBehaviour;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class QueueFragment extends Fragment {
    private ArrayList<Track> queueTracks = new ArrayList<>();
    private RecyclerView queueList;
    private QueueAdapter adapter;
    private View menuLayout;
    private LinearLayoutManager linearLayoutManager;

    private SongInterface songInterface;
    private PlaybackControlInterface playbackControlInterface;
    private ServiceTriggerInterface serviceTriggerInterface;
    private NavigationControlInterface navigationControlInterface;
    private PlaybackBehaviour.PlaybackBehaviourState playbackBehaviour;
    private int queuePosition = -1;
    private boolean scrolling = false;

    public QueueFragment() {
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.queue_menu, menu);

        menu.getItem(0).setIcon(PlaybackBehaviour.getDrawableResourceIdForState(playbackBehaviour));
        menu.getItem(0).setIconTintList(ContextCompat.getColorStateList(requireContext(), R.color.colorOnSurface));
        menu.getItem(1).setIconTintList(ContextCompat.getColorStateList(requireContext(), R.color.colorOnSurface));
        menu.getItem(2).setIconTintList(ContextCompat.getColorStateList(requireContext(), R.color.colorOnSurface));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_queue_delete && queueTracks.size() > 0) {
            menuLayout.setAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.popupwindow_show));

            int wrapContent = LinearLayout.LayoutParams.WRAP_CONTENT;
            PopupWindow filterMenu = new PopupWindow(menuLayout, wrapContent, wrapContent, true);

            menuLayout.findViewById(R.id.queue_delete_menu_all).setOnClickListener((view) -> {
                filterMenu.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext()).setTitle("Clear queue");
                builder.setPositiveButton("Clear all", (dialogInterface, i) -> {
                    int size = queueTracks.size();
                    songInterface.onSongsRemoveListener(queueTracks);
                    queueTracks.clear();
                    adapter.notifyItemRangeRemoved(0, size);
                });
                builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
                builder.show();
            });

            if (playbackBehaviour == PlaybackBehaviour.PlaybackBehaviourState.REPEAT_LIST) {
                menuLayout.findViewById(R.id.queue_delete_menu_played).setOnClickListener((view) -> {
                    filterMenu.dismiss();
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext()).setTitle("Clear played songs");
                    builder.setPositiveButton("Clear played", (dialogInterface, i) -> {
                        ArrayList<Track> toDelete = new ArrayList<>();
                        for (int j = 0; j < queuePosition; j++) {
                            toDelete.add(queueTracks.get(j));
                        }
                        queueTracks.removeAll(toDelete);
                        adapter.notifyItemRangeRemoved(0, queuePosition);
                        songInterface.onSongsRemoveListener(toDelete);
                        queuePosition = 0;
                        adapter.setNewQueuePosition(0);
                    });
                    builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
                    builder.show();
                });
            } else {
                ((TextView) menuLayout.findViewById(R.id.queue_delete_menu_played_title)).setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.colorBackground));
                menuLayout.findViewById(R.id.queue_delete_menu_played).setOnClickListener((view) -> {
                    Toast.makeText(requireContext(), "Not available in current playback state", Toast.LENGTH_SHORT).show();
                });
            }

            filterMenu.setContentView(menuLayout);
            filterMenu.setOutsideTouchable(true);
            filterMenu.showAsDropDown(requireActivity().findViewById(R.id.toolbar), 0, 0, Gravity.END);
        } else if (item.getItemId() == R.id.action_queue_behaviour) {
            playbackBehaviour = PlaybackBehaviour.getNextState(playbackBehaviour);
            item.setIcon(PlaybackBehaviour.getDrawableResourceIdForState(playbackBehaviour));
            adapter.updatePlaybackBehaviourState(playbackBehaviour);
            playbackControlInterface.onPlaybackBehaviourChangeListener(playbackBehaviour);
        } else if (item.getItemId() == R.id.action_queue_jumpto) {
            if (queuePosition >= 0) {
                linearLayoutManager.scrollToPosition(queuePosition);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            serviceTriggerInterface = (ServiceTriggerInterface) context;
            navigationControlInterface = (NavigationControlInterface) context;
            songInterface = (SongInterface) context;
            playbackControlInterface = (PlaybackControlInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + "must implement PlaybackControlInterface");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        postponeEnterTransition();
        if (getArguments() != null) {
            queuePosition = getArguments().getInt("QUEUE_POS", -1);
            playbackBehaviour = PlaybackBehaviour.getStateFromInteger(getArguments().getInt("BEHAVIOUR_STATE", 3));
        }

        IntentFilter intentFilter = new IntentFilter(getResources().getString(R.string.playback_control_values));
        intentFilter.addAction(getResources().getString(R.string.musicservice_song_prepared));

        requireActivity().registerReceiver(receiver, intentFilter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_queue, container, false);
        queueList = view.findViewById(R.id.queue_list);
        View anchor = view.findViewById(R.id.queue_snackbar_anchor);
        menuLayout = inflater.inflate(R.layout.queue_delete_menu, null);

        navigationControlInterface.setHomeAsUpEnabled(false);
        navigationControlInterface.isDrawerEnabledListener(true);
        navigationControlInterface.setToolbarTitle("Queue");

        adapter = new QueueAdapter(requireContext(), queueTracks, queuePosition, playbackBehaviour, songInterface);
        linearLayoutManager = new LinearLayoutManager(requireContext());
        queueList.setLayoutManager(linearLayoutManager);
        queueList.setHasFixedSize(true);
        queueList.setAdapter(adapter);

        DragItemTouchHelper dragItemCallback = new DragItemTouchHelper(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT, requireContext(), adapter, anchor);
        dragItemCallback.setTargetList(queueTracks);
        dragItemCallback.setDeleteOnSwipe(true);
        dragItemCallback.setOnItemMovedListener((fromPos, toPos) -> songInterface.onOrderChangeListener(fromPos, toPos));
        dragItemCallback.setOnItemDeletedListener((list) -> songInterface.onSongsRemoveListener((List<Track>) list));
        dragItemCallback.setOnItemRemovedListener((index) -> {
            if (index < queuePosition) {
                queuePosition--;
                adapter.setNewQueuePosition(queuePosition);
            }
        });
        dragItemCallback.setOnItemRestoredListener((index) -> {
            if (index < queuePosition) {
                queuePosition++;
                adapter.setNewQueuePosition(queuePosition);
            }
        });

        ItemTouchHelper touchHelper = new ItemTouchHelper(dragItemCallback);
        touchHelper.attachToRecyclerView(queueList);

        serviceTriggerInterface.triggerCurrentDataBroadcast();

        queueList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (!scrolling && linearLayoutManager.findFirstCompletelyVisibleItemPosition() > 0) {
                    scrolling = true;
                    navigationControlInterface.setToolbarBackground(true);
                } else if (scrolling && linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                    scrolling = false;
                    navigationControlInterface.setToolbarBackground(false);
                }

                super.onScrolled(recyclerView, dx, dy);
            }
        });

        return view;
    }

    @Override
    public void onDetach() {
        requireActivity().unregisterReceiver(receiver);
        navigationControlInterface.setToolbarBackground(false);
        super.onDetach();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(getResources().getString(R.string.playback_control_values))) {
                Bundle bundle = intent.getExtras();
                queuePosition = bundle.getInt("QUEUE_INDEX", -1);
                if (queueTracks.isEmpty()) {
                    playbackBehaviour = PlaybackBehaviour.getStateFromInteger(bundle.getInt("BEHAVIOUR_STATE", 3));
                    queueTracks.addAll(bundle.getParcelableArrayList(getString(R.string.parcelable_track_list)));
                    adapter.getAllBackgroundImages(queueTracks);
                    adapter.notifyItemRangeInserted(0, queueTracks.size());
                    startPostponedEnterTransition();
                    linearLayoutManager.scrollToPosition(queuePosition);
                }
                adapter.setNewQueuePosition(queuePosition);
            } else if (intent.getAction().equals(getResources().getString(R.string.musicservice_song_prepared))) {
                Bundle bundle = intent.getExtras();
                queuePosition = bundle.getInt("LIST_INDEX", -1);
                adapter.setNewQueuePosition(queuePosition);
            }
        }
    };
}