package com.example.musicplayer.ui.playlistdetail;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.musicplayer.R;
import com.example.musicplayer.adapter.TrackSelectionAdapter;
import com.example.musicplayer.entities.MusicResolver;
import com.example.musicplayer.ui.playlist.PlaylistInterface;
import com.example.musicplayer.utils.NavigationControlInterface;
import com.google.android.material.datepicker.MaterialTextInputPicker;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class PlaylistDetailAdd extends Fragment implements OnTrackSelectedListener {
    private static final int PERMISSION_REQUEST_CODE = 0x03 ;

    private RecyclerView selection;
    private EditText search;
    private ArrayList<MusicResolver> trackList = new ArrayList<>();
    private TrackSelectionAdapter mAdapter;
    private ExtendedFloatingActionButton confirm;
    private PlaylistInterface playlistInterface;
    private NavigationControlInterface navigationControlInterface;
    private boolean isFiltered=false;
    private int selectedCount = 0;
    private String title = "";

    private LinearLayoutManager linearLayoutManager;
    private Map<String,Integer> mapIndex;
    private FrameLayout indexZoomHolder;
    private TextView indexZoom;

    public PlaylistDetailAdd() {
        // Required empty public constructor
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

        PlaylistDetailAdd.DividerItemDecoration dividerItemDecoration = new PlaylistDetailAdd.DividerItemDecoration(requireContext(), R.drawable.recyclerview_divider);
        selection.addItemDecoration(dividerItemDecoration);
        selection.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(requireContext(),R.anim.layout_animation_fall_down));

        linearLayoutManager = new LinearLayoutManager(requireContext());
        selection.setLayoutManager(linearLayoutManager);

        PlaylistSelectorViewModel model = new ViewModelProvider(this).get(PlaylistSelectorViewModel.class);
        model.getTracklist().observe(getViewLifecycleOwner(),trackList ->{
            this.trackList=trackList;
            Collections.sort(this.trackList, new Comparator<MusicResolver>(){
                public int compare(MusicResolver a, MusicResolver b){
                    return a.getTitle().compareToIgnoreCase(b.getTitle());
                }
            });

            mAdapter=new TrackSelectionAdapter(requireContext(), trackList, this);
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
                if (charSequence.length() != 0) isFiltered=true;
                else isFiltered=false;
                mAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playlistInterface.OnAddSongsListener(getSelected(), title);
            }
        });

        return view;
    }

    public ArrayList<MusicResolver> getSelected(){
        ArrayList<MusicResolver> selected_tracks = new ArrayList<>();
        for(int i=0;i<trackList.size();i++){
            if (trackList.get(i).isSelected())selected_tracks.add(trackList.get(i));
        }
        return selected_tracks;
    }

    public void setTitle(String title){this.title = title; }

    @Override
    public void onSongSelected(int i) {
        int position;
        if (isFiltered)position = mAdapter.getOriginalPosition(i);
        else position = i;
        MusicResolver track = mAdapter.getItem(position);
        if(track.isSelected()){
            track.setSelected(false);
            selectedCount-=1;
        }
        else {
            track.setSelected(true);
            selectedCount += 1;
        }
        trackList.set(position,track);
        mAdapter.notifyDataSetChanged();
        if (selectedCount > 0) {
            if (selectedCount == 1){
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
        LinearLayout indexLayout = new LinearLayout(requireActivity()){
            @Override
            public boolean dispatchTouchEvent(MotionEvent ev) {
                int x = Math.round(ev.getX());
                int y = Math.round(ev.getY());
                for (int i=0; i<getChildCount(); i++){
                    TextView child = (TextView) getChildAt(i);
                    if(x > child.getLeft() && x < child.getRight() && y > child.getTop() && y < child.getBottom()){
                        child.callOnClick();
                        //touch is within this child
                        if(ev.getAction() == MotionEvent.ACTION_UP){
                            indexZoomHolder.setVisibility(View.GONE);
                        }
                    }
                }
                if(!(x > getLeft() && x < getRight() && y > getTop() && y < getBottom())){
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
        int div = (int) ((int) 27*1.8);

        int textsize= ((abs_heigt-px)/(div))/((int) getContext().getResources().getDisplayMetrics().scaledDensity);

        MaterialTextView textView;
        List<String> indexList = new ArrayList<String>(mapIndex.keySet());
        for (String index : indexList) {
            textView = new MaterialTextView(requireActivity());
            textView.setTextSize(textsize);
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextLight));
            textView.setText(index);
            textView.setFocusable(false);
            textView.setGravity(Gravity.CENTER);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    indexZoomHolder.setVisibility(View.VISIBLE);
                    TextView selectedIndex = (TextView) view;
                    indexZoom.setText(selectedIndex.getText().subSequence(0,1));
                    linearLayoutManager.scrollToPositionWithOffset(mapIndex.get(selectedIndex.getText()), 0);
                }
            });
            indexLayout.addView(textView);
        }
        linearLayout.addView(indexLayout);
    }

    private void getIndexList() {
        mapIndex = new LinkedHashMap<String, Integer>();
        for (int i = 0; i < trackList.size(); i++) {
            MusicResolver item = trackList.get(i);
            String index = item.getTitle().substring(0,1);
            Character character = index.charAt(0);

            if(character <=64 || character >=123){
                index = "#";
            } else if(character >= 91 && character <= 96)index = "#";
            else if(character >96){
                character = Character.toUpperCase(character);
                index = character.toString();
            }

            if (mapIndex.get(index) == null)
                mapIndex.put(index, i);
        }
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
