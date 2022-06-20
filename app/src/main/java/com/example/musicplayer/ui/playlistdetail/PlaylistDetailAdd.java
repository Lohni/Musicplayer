package com.example.musicplayer.ui.playlistdetail;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.musicplayer.R;
import com.example.musicplayer.adapter.TrackSelectionAdapter;
import com.example.musicplayer.database.MusicplayerApplication;
import com.example.musicplayer.database.dao.MusicplayerDataAccess;
import com.example.musicplayer.database.dao.PlaylistDataAccess;
import com.example.musicplayer.database.entity.Track;
import com.example.musicplayer.database.viewmodel.MusicplayerViewModel;
import com.example.musicplayer.database.viewmodel.PlaylistViewModel;
import com.example.musicplayer.entities.MusicResolver;
import com.example.musicplayer.ui.other.CustomDividerItemDecoration;
import com.example.musicplayer.utils.NavigationControlInterface;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class PlaylistDetailAdd extends Fragment implements OnTrackSelectedListener {
    private static final int PERMISSION_REQUEST_CODE = 0x03;

    private RecyclerView selection;
    private EditText search;
    private ArrayList<Track> trackList = new ArrayList<>();
    private TrackSelectionAdapter mAdapter;
    private ExtendedFloatingActionButton confirm;
    private NavigationControlInterface navigationControlInterface;
    private String title = "";

    private LinearLayoutManager linearLayoutManager;
    private Map<String, Integer> mapIndex;
    private FrameLayout indexZoomHolder;
    private TextView indexZoom;

    private PlaylistViewModel playlistViewModel;
    private MusicplayerViewModel musicplayerViewModel;

    public PlaylistDetailAdd() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            navigationControlInterface = (NavigationControlInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + "must implement SongListInterface");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PlaylistDataAccess pda = ((MusicplayerApplication) requireActivity().getApplication()).getDatabase().playlistDao();
        MusicplayerDataAccess mda = ((MusicplayerApplication) requireActivity().getApplication()).getDatabase().musicplayerDao();

        playlistViewModel = new ViewModelProvider(requireActivity(), new PlaylistViewModel.PlaylistViewModelFactory(pda)).get(PlaylistViewModel.class);
        musicplayerViewModel = new ViewModelProvider(requireActivity(), new MusicplayerViewModel.MusicplayerViewModelFactory(mda)).get(MusicplayerViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_playlist_detail_add, container, false);
        selection = view.findViewById(R.id.selection_list);
        search = view.findViewById(R.id.playlist_add_search);
        confirm = view.findViewById(R.id.playlist_detail_add_confirm);
        indexZoom = view.findViewById(R.id.playlist_detail_add_indexzoom);
        indexZoomHolder = view.findViewById(R.id.playlist_add_indexzoom_holder);
        indexZoomHolder.setVisibility(View.GONE);
        confirm.setVisibility(View.INVISIBLE);

        navigationControlInterface.isDrawerEnabledListener(false);
        navigationControlInterface.setHomeAsUpEnabled(true);
        navigationControlInterface.setHomeAsUpIndicator(R.drawable.ic_clear_black_24dp);
        navigationControlInterface.setToolbarTitle(title);

        selection.setHasFixedSize(true);

        CustomDividerItemDecoration dividerItemDecoration = new CustomDividerItemDecoration(requireContext(), R.drawable.recyclerview_divider);
        selection.addItemDecoration(dividerItemDecoration);
        selection.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_animation_fall_down));

        linearLayoutManager = new LinearLayoutManager(requireContext());
        selection.setLayoutManager(linearLayoutManager);

        musicplayerViewModel.getAllTracks().observe(getViewLifecycleOwner(), tracklist -> {
            this.trackList.clear();
            this.trackList.addAll(tracklist);

            Collections.sort(this.trackList, (a, b) -> a.getTTitle().compareToIgnoreCase(b.getTTitle()));
            mAdapter = new TrackSelectionAdapter(requireContext(), trackList, this);
            selection.setAdapter(mAdapter);
            getIndexList();
            displayIndex(getResources().getDisplayMetrics().heightPixels, view);
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Add to DB
                //OnBackPressed
                //Todo
                //playlistInterface.OnAddSongsListener(getSelected(), title);
            }
        });

        return view;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public void onSongSelected(int i) {
        int selectedCount = mAdapter.getSelectedCount();
        if (selectedCount > 0) {
            if (selectedCount == 1) {
                confirm.setVisibility(View.VISIBLE);
                confirm.setText("ADD " + selectedCount + " SONG");
            } else confirm.setText("ADD " + selectedCount + " SONGS");
        } else confirm.setVisibility(View.INVISIBLE);
    }

    /*
    Side Index
     */
    private void displayIndex(int abs_heigt, View view) {
        LinearLayout linearLayout = view.findViewById(R.id.playlist_detail_add_side_index);
        LinearLayout indexLayout = new LinearLayout(requireActivity()) {
            @Override
            public boolean dispatchTouchEvent(MotionEvent ev) {
                int x = Math.round(ev.getX());
                int y = Math.round(ev.getY());
                for (int i = 0; i < getChildCount(); i++) {
                    TextView child = (TextView) getChildAt(i);
                    if (x > child.getLeft() && x < child.getRight() && y > child.getTop() && y < child.getBottom()) {
                        child.callOnClick();
                        //touch is within this child
                        if (ev.getAction() == MotionEvent.ACTION_UP) {
                            indexZoomHolder.setVisibility(View.GONE);
                        }
                    }
                }
                if (!(x > getLeft() && x < getRight() && y > getTop() && y < getBottom())) {
                    //Touch is out if Layout
                    indexZoomHolder.setVisibility(View.GONE);
                }
                return true;
            }
        };
        indexLayout.setOrientation(LinearLayout.VERTICAL);

        float dip = 61f;
        Resources r = getResources();
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.getDisplayMetrics()
        );
        int div = (int) ((int) 27 * 1.8);

        int textsize = ((abs_heigt - px) / (div)) / ((int) getContext().getResources().getDisplayMetrics().scaledDensity);

        MaterialTextView textView;
        List<String> indexList = new ArrayList<String>(mapIndex.keySet());
        for (String index : indexList) {
            textView = new MaterialTextView(requireActivity());
            textView.setTextSize(textsize);
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextLight));
            textView.setText(index);
            textView.setFocusable(false);
            textView.setGravity(Gravity.CENTER);
            textView.setOnClickListener(view1 -> {
                indexZoomHolder.setVisibility(View.VISIBLE);
                TextView selectedIndex = (TextView) view1;
                indexZoom.setText(selectedIndex.getText().subSequence(0, 1));
                linearLayoutManager.scrollToPositionWithOffset(mapIndex.get(selectedIndex.getText()), 0);
            });
            indexLayout.addView(textView);
        }
        linearLayout.addView(indexLayout);
    }

    private void getIndexList() {
        mapIndex = new LinkedHashMap<>();
        for (int i = 0; i < trackList.size(); i++) {
            Track item = trackList.get(i);
            String index = item.getTTitle().substring(0, 1);
            Character character = index.charAt(0);

            if (character <= 64 || character >= 123) {
                index = "#";
            } else if (character >= 91 && character <= 96) index = "#";
            else if (character > 96) {
                character = Character.toUpperCase(character);
                index = character.toString();
            }

            mapIndex.putIfAbsent(index, i);
        }
    }
}
