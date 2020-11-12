package com.example.musicplayer.ui.expandedplaybackcontrol;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.musicplayer.R;
import com.example.musicplayer.entities.MusicResolver;
import com.example.musicplayer.ui.views.AudioVisualizerView;

public class ExpandedPlaybackControl extends Fragment {
    private static final int PERMISSION_REQUEST_CODE = 0x03;
    private TextView expanded_title,expanded_artist,expanded_currtime,expanded_absolute_time;
    private ImageButton expanded_play,expanded_skipforward,expanded_skipback,collapse, expanded_fav, expanded_behaviourControl, expanded_more;
    private AudioVisualizerView audioVisualizerView;
    private ImageView cover;
    private SeekBar expanded_seekbar;

    private ViewPager2 mPager;
    private FragmentStateAdapter mAdapter;
    private View view;

    private ExpandedPlaybackControlInterface epcInterface;

    private int newProgress, audioSessionID;
    private boolean seekbarUserAction=false;

    public ExpandedPlaybackControl() {
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
            epcInterface = (ExpandedPlaybackControlInterface) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + "must implement PlaybackControlInterface");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_expanded_playback_control, container, false);
        mPager = view.findViewById(R.id.expanded_control_viewpager);
        expanded_title = view.findViewById(R.id.expanded_control_title);
        expanded_artist = view.findViewById(R.id.expanded_control_artist);
        expanded_play = view.findViewById(R.id.expanded_control_play);
        expanded_skipforward = view.findViewById(R.id.expanded_control_skipforward);
        expanded_skipback = view.findViewById(R.id.expanded_control_skipback);
        expanded_fav = view.findViewById(R.id.expanded_favourite);
        expanded_behaviourControl = view.findViewById(R.id.expanded_control_behaviour);
        expanded_currtime = view.findViewById(R.id.expanded_current_time);
        expanded_absolute_time = view.findViewById(R.id.expanded_absolute_time);
        expanded_seekbar = view.findViewById(R.id.expanded_seekbar);
        expanded_more = view.findViewById(R.id.expanded_menu_more);
        audioVisualizerView = view.findViewById(R.id.audioView);
        collapse = view.findViewById(R.id.expanded_control_collapse);
        cover = view.findViewById(R.id.expanded_cover);

        permission();
        expanded_title.setSelected(true);

        requireActivity().startPostponedEnterTransition();

        mAdapter = new PlaybackPagerAdapter(requireActivity());
        mPager.setAdapter(mAdapter);

        expanded_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                epcInterface.OnStateChangeListener();
            }
        });

        expanded_skipforward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                epcInterface.OnSkipPressedListener();
            }
        });

        expanded_skipback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                epcInterface.OnSkipPreviousListener();
            }
        });

        collapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getParentFragmentManager().popBackStack();
            }
        });

        expanded_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                newProgress=i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekbarUserAction=true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                epcInterface.OnSeekbarChangeListener(newProgress);
                seekbarUserAction=false;
            }
        });
        epcInterface.OnStartListener();
        audioVisualizerView.initVisualizer(audioSessionID);
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        epcInterface.OnCloseListener();
        audioVisualizerView.setenableVisualizer(false);
    }

    public void updateSeekbar(int time){
        if (!seekbarUserAction)expanded_seekbar.setProgress(time);
        expanded_currtime.setText(convertTime(time));
    }

    private String convertTime(int duration){
        float d = (float)duration /(1000*60);
        int min = (int)d;
        float seconds = (d - min)*60;
        int sec = (int)seconds;
        String minute=min+"", second=sec+"";
        if(min<10) minute="0"+minute;
        if(sec<10) second="0"+second;
        return minute + ":" + second;
    }

    public void setSongInfo(String title, String artist,int length, long id){
        expanded_absolute_time.setText(convertTime(length));
        expanded_title.setText(title);
        expanded_artist.setText(artist);
        expanded_seekbar.setMax(length);
        loadCover(id);
    }

    public void setAudioSessionID(int audioSessionID){
        this.audioSessionID=audioSessionID;
    }

    public void setControlButton(boolean isOnPause){
        if(!isOnPause){
            expanded_play.setBackground(ContextCompat.getDrawable(requireContext(),R.drawable.ic_pause_black_24dp));
        } else {
            expanded_play.setBackground(ContextCompat.getDrawable(requireContext(),R.drawable.ic_play_arrow_black_24dp));
        }
    }

    private void loadCover(long song){
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,song);
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(requireContext(),trackUri);
        byte [] thumbnail = mmr.getEmbeddedPicture();
        mmr.release();
        if (thumbnail != null){
            setCoverImage(new BitmapDrawable(getResources(),BitmapFactory.decodeByteArray(thumbnail,0,thumbnail.length)),false);
        } else {
            setCoverImage(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_music_note_24,null),true);
        }
    }

    private void setCoverImage(Drawable coverImage, boolean custom){
        if (custom)cover.setImageTintList(AppCompatResources.getColorStateList(requireContext(),R.color.colorPrimaryNight));
        this.cover.setImageDrawable(coverImage);
    }

    public void setShuffleButton(boolean shuffle){
        if(!shuffle){
            //expanded_shuffle.setBackgroundTintList(getResources().getColorStateList(R.color.colorTransparent));
            Toast.makeText(requireContext(),"Disable shuffle list",Toast.LENGTH_LONG).show();
        } else{
            //expanded_shuffle.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimaryDark));
            Toast.makeText(requireContext(),"Shuffle list",Toast.LENGTH_LONG).show();
        }
    }

    public void setRepeatButton(boolean repeat){
        if(!repeat){
            //expanded_repeat.setBackgroundTintList(getResources().getColorStateList(R.color.colorTransparent));
            Toast.makeText(requireContext(),"Disable repeat list",Toast.LENGTH_LONG).show();
        } else{
            //expanded_repeat.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimaryDark));
            Toast.makeText(requireContext(),"Repeat list",Toast.LENGTH_LONG).show();
        }
    }

    public void setLoopButton(boolean loop){
        if(!loop){
            //expanded_loop.setBackgroundTintList(getResources().getColorStateList(R.color.colorTransparent));
            Toast.makeText(requireContext(),"Disable looping",Toast.LENGTH_LONG).show();
        } else{
            //expanded_loop.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimaryDark));
            Toast.makeText(requireContext(),"Loop current song",Toast.LENGTH_LONG).show();
        }
    }

    private void permission(){
        //if (Build.VERSION.SDK_INT >= 23) {
        //Check whether your app has access to the READ permission//
        if (checkPermission()) {
            //If your app has access to the device’s storage, then print the following message to Android Studio’s Logcat//
            Log.e("permission", "Permission already granted.");
        } else {
            //If your app doesn’t have permission to access external storage, then call requestPermission//
            requestPermission();
        }
        //}
    }

    private boolean checkPermission() {
        //Check for READ_EXTERNAL_STORAGE access, using ContextCompat.checkSelfPermission()//
        int result = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.RECORD_AUDIO);
        //If the app does have this permission, then return true//
        //If the app doesn’t have this permission, then return false//
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
    }

    private class PlaybackPagerAdapter extends FragmentStateAdapter{

        public PlaybackPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return null;
        }

        @Override
        public int getItemCount() {
            return 0;
        }
    }
}
