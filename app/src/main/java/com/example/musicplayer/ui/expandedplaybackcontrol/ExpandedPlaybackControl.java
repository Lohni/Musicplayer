package com.example.musicplayer.ui.expandedplaybackcontrol;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.musicplayer.R;

public class ExpandedPlaybackControl extends Fragment {

    private TextView expanded_title,expanded_artist,expanded_currtime,expanded_absolute_time;
    private ImageButton expanded_play,expanded_skipforward,expanded_skipback,expanded_shuffle,expanded_repeat,expanded_loop;

    private SeekBar expanded_seekbar;

    private ViewPager2 mPager;
    private FragmentStateAdapter mAdapter;
    private View view;

    private ExpandedPlaybackControlInterface epcInterface;

    private int newProgress;
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
        expanded_shuffle = view.findViewById(R.id.expanded_control_shuffle);
        expanded_repeat = view.findViewById(R.id.expanded_control_repeat);
        expanded_currtime = view.findViewById(R.id.expanded_current_time);
        expanded_absolute_time = view.findViewById(R.id.expanded_absolute_time);
        expanded_seekbar = view.findViewById(R.id.expanded_seekbar);
        expanded_loop = view.findViewById(R.id.expanded_control_loop);

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

        expanded_shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                epcInterface.OnShuffleClickListener();
            }
        });

        expanded_repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                epcInterface.OnRepeatClickListener();
            }
        });

        expanded_loop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                epcInterface.OnLoopClickListener();
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
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        epcInterface.OnCloseListener();
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

    public void setSongInfo(String title, String artist,int length){
        expanded_absolute_time.setText(convertTime(length));
        expanded_title.setText(title);
        expanded_artist.setText(artist);
        expanded_seekbar.setMax(length);
    }

    public void setControlButton(boolean isOnPause){
        if(!isOnPause){
            expanded_play.setBackground(ContextCompat.getDrawable(requireContext(),R.drawable.ic_pause_black_24dp));
        } else {
            expanded_play.setBackground(ContextCompat.getDrawable(requireContext(),R.drawable.ic_play_arrow_black_24dp));
        }
    }

    public void setShuffleButton(boolean shuffle){
        if(!shuffle){
            expanded_shuffle.setBackgroundTintList(getResources().getColorStateList(R.color.colorTransparent));
            Toast.makeText(requireContext(),"Disable shuffle list",Toast.LENGTH_LONG).show();
        } else{
            expanded_shuffle.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimaryDark));
            Toast.makeText(requireContext(),"Shuffle list",Toast.LENGTH_LONG).show();
        }
    }

    public void setRepeatButton(boolean repeat){
        if(!repeat){
            expanded_repeat.setBackgroundTintList(getResources().getColorStateList(R.color.colorTransparent));
            Toast.makeText(requireContext(),"Disable repeat list",Toast.LENGTH_LONG).show();
        } else{
            expanded_repeat.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimaryDark));
            Toast.makeText(requireContext(),"Repeat list",Toast.LENGTH_LONG).show();
        }
    }

    public void setLoopButton(boolean loop){
        if(!loop){
            expanded_loop.setBackgroundTintList(getResources().getColorStateList(R.color.colorTransparent));
            Toast.makeText(requireContext(),"Disable looping",Toast.LENGTH_LONG).show();
        } else{
            expanded_loop.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimaryDark));
            Toast.makeText(requireContext(),"Loop current song",Toast.LENGTH_LONG).show();
        }
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
