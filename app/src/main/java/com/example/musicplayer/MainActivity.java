package com.example.musicplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.audiofx.EnvironmentalReverb;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.transition.Fade;
import android.view.MenuItem;
import android.view.View;

import com.example.musicplayer.entities.MusicResolver;
import com.example.musicplayer.transition.AlbumDetailTransition;
import com.example.musicplayer.ui.DatabaseViewmodel;
import com.example.musicplayer.ui.album.AlbumDetailFragment;
import com.example.musicplayer.ui.album.AlbumFragment;
import com.example.musicplayer.ui.dashboard.DashboardFragment;
import com.example.musicplayer.ui.audioeffects.AudioEffectViewModel;
import com.example.musicplayer.ui.audioeffects.AudioEffectInterface;
import com.example.musicplayer.ui.audioeffects.EqualizerViewPager;
import com.example.musicplayer.ui.audioeffects.database.AudioEffectSettingsHelper;
import com.example.musicplayer.ui.expandedplaybackcontrol.ExpandedPlaybackControl;
import com.example.musicplayer.ui.expandedplaybackcontrol.ExpandedPlaybackControlInterface;
import com.example.musicplayer.ui.playbackcontrol.PlaybackControl;
import com.example.musicplayer.ui.playbackcontrol.PlaybackControlInterface;
import com.example.musicplayer.ui.playlist.Playlist;
import com.example.musicplayer.ui.playlist.PlaylistInterface;
import com.example.musicplayer.ui.playlistdetail.PlaylistDetail;
import com.example.musicplayer.ui.songlist.SongList;
import com.example.musicplayer.ui.songlist.SongListInterface;
import com.example.musicplayer.ui.tagEditor.TagEditorDetailFragment;
import com.example.musicplayer.ui.tagEditor.TagEditorFragment;
import com.example.musicplayer.ui.tagEditor.TagEditorInterface;
import com.example.musicplayer.ui.views.TestMotionLayout;
import com.example.musicplayer.utils.NavigationControlInterface;
import com.example.musicplayer.utils.Permissions;
import com.example.musicplayer.utils.enums.PlaybackBehaviour;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.NonNull;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity implements SongListInterface, PlaybackControlInterface, NavigationView.OnNavigationItemSelectedListener,
        ExpandedPlaybackControlInterface, PlaylistInterface, AudioEffectInterface, TagEditorInterface, NavigationControlInterface, AlbumFragment.AlbumListener {

    private int motionLayout_transition_state, motionLayout_startState, motionLayout_endState;

    DrawerLayout drawer;
    MusicService musicService;
    ArrayList<MusicResolver> songlist;

    private PlaybackControl playcontrol;
    private ExpandedPlaybackControl expandedPlaybackControl;
    private TestMotionLayout motionLayout;

    private DatabaseViewmodel databaseViewmodel;
    private AudioEffectViewModel audioEffectViewModel;

    private final String playlistDetail = "FRAGMENT_PLAYLISTDETAIL";

    private boolean isOnPause = true, isExpanded=false, isTransitionFragmentChanged = false;
    private Handler mHandler = new Handler();
    private ActionBarDrawerToggle toggle;

    private SharedPreferences sharedPreferences;
    private Fragment selectedDrawerFragment;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        audioEffectViewModel = new ViewModelProvider(this).get(AudioEffectViewModel.class);

        setSupportActionBar(toolbar);

        loadPlayControl(playcontrol = new PlaybackControl());
        loadDashboard(new DashboardFragment());

        Intent service = new Intent(this,MusicService.class);
        if (Permissions.permission(this, Manifest.permission.RECORD_AUDIO)){
            bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);
            startService(service);
        }

        databaseViewmodel=new ViewModelProvider(this).get(DatabaseViewmodel.class);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        motionLayout = findViewById(R.id.parentContainer);
        motionLayout_transition_state = motionLayout.getStartState();
        motionLayout_startState = motionLayout.getStartState();
        motionLayout_endState = motionLayout.getEndState();
        motionLayout.setTransitionListener(new MotionLayout.TransitionListener() {
            @Override
            public void onTransitionStarted(MotionLayout motionLayout, int i, int i1) {
            }

            @Override
            public void onTransitionChange(MotionLayout layout, int i, int i1, float progress) {
                if (motionLayout_transition_state == motionLayout_startState){
                    if (progress > 0.6 && !isTransitionFragmentChanged){
                        expandedPlaybackControl = new ExpandedPlaybackControl();
                        expandedPlaybackControl.setEnterTransition(new Fade().setDuration(500));
                        expandedPlaybackControl.setSharedElementEnterTransition(new AlbumDetailTransition());
                        expandedPlaybackControl.setSharedElementReturnTransition(new AlbumDetailTransition());

                        playcontrol.setExitTransition(new Fade().setDuration(500));

                        getSupportFragmentManager().beginTransaction()
                                .addSharedElement(playcontrol.getParentView(), getResources().getString(R.string.transition_playback_layout))
                                .addSharedElement(playcontrol.getTitleView(), getResources().getString(R.string.transition_playback_title))
                                .replace(R.id.playbackcontrol_holder,expandedPlaybackControl, getString(R.string.fragment_expandedPlaybackControl)).commit();

                        isTransitionFragmentChanged = true;
                    }
                } else if (motionLayout_transition_state == motionLayout_endState){
                    if (progress < 0.5 && !isTransitionFragmentChanged){
                        playcontrol = new PlaybackControl();
                        getSupportFragmentManager().beginTransaction().replace(R.id.playbackcontrol_holder, playcontrol).commit();
                        isTransitionFragmentChanged = true;
                        //motionLayout.transitionToStart();
                    }
                }
            }
            @Override
            public void onTransitionCompleted(MotionLayout motionLayout, int reachedState) {
                isTransitionFragmentChanged = false;
                motionLayout_transition_state = reachedState;
                if (reachedState == motionLayout_startState)onPlaybackControlStarted();
                else onExpandedPlaybackControlStarted();
            }

            @Override
            public void onTransitionTrigger(MotionLayout motionLayout, int i, boolean b, float v) {

            }
        });

        toggle = new ActionBarDrawerToggle(this, drawer,toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);

        toggle.syncState();
        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        songlist=new ArrayList<>();

        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                if (selectedDrawerFragment != null){
                    getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,selectedDrawerFragment).commit();
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        runnable.run();
        navigationView.setNavigationItemSelectedListener(this);

    }

    private void loadDashboard(Fragment fragment){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.nav_host_fragment,fragment);
        ft.commit();
    }

    private void loadPlayControl(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.playbackcontrol_holder,fragment);
        ft.commit();
    }

    //Service Connection
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)iBinder;
            musicService = binder.getServiceInstance();
            IntentFilter filter = new IntentFilter();
            filter.addAction(getString(R.string.intent_mediaplayer_play));
            filter.addAction("MUSICPLAYER_QUEUE_SIZE");
            filter.addAction("MUSICPLAYER_CURRENT_INDEX");

            //Init AudioEffects
            musicService.registerReceiver(broadcastReceiver,filter);
            playcontrol.setAudioSessionID(musicService.getSessionId());
            musicService.setReverbEnabled(sharedPreferences.getBoolean(getResources().getString(R.string.preference_reverb_isenabled), false));
            musicService.setEqualizerEnabled(sharedPreferences.getBoolean(getResources().getString(R.string.preference_equalizer_isenabled), false));
            musicService.setBassBoostEnabled(sharedPreferences.getBoolean(getResources().getString(R.string.preference_bassboost_isenabled), false));
            musicService.setVirtualizerEnabled(sharedPreferences.getBoolean(getResources().getString(R.string.preference_virtualizer_isenabled), false));
            musicService.setLoudnessEnhancerEnabled(sharedPreferences.getBoolean(getResources().getString(R.string.preference_loudnessenhancer_isenabled), false));

            musicService.setBassBoostStrength((short) sharedPreferences.getInt(getResources().getString(R.string.preference_bassboost_strength), 0));
            musicService.setVirtualizerStrength((short) sharedPreferences.getInt(getResources().getString(R.string.preference_virtualizer_strength), 0));
            musicService.setLoudnessEnhancerGain(sharedPreferences.getInt(getResources().getString(R.string.preference_loudnessenhancer_strength), 0));
            
            initialiseAudioEffects();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            musicService.pause();
            musicService = null;
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && permissions[0].equals(Manifest.permission.RECORD_AUDIO)){
            Intent service = new Intent(this,MusicService.class);
            bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);
            startService(service);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        unbindService(serviceConnection);
    }

    private void initialiseAudioEffects(){
        audioEffectViewModel.getCurrentActivePreset().observe(this, reverbSettings -> {
            if (reverbSettings != null)musicService.setEnvironmentalReverbSettings(AudioEffectSettingsHelper.extractReverbValues(reverbSettings));
        });
        audioEffectViewModel.getCurrentActiveEqualizerPreset().observe(this, equalizerSettings -> {
            if (equalizerSettings != null)musicService.setEqualizerBandLevels(equalizerSettings.getBandLevels());
        });
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null){
                if (intent.getAction().equals(getString(R.string.intent_mediaplayer_play))) {
                    MusicResolver currsong = musicService.getCurrSong();
                    if (!isExpanded)
                        playcontrol.setSongInfo(currsong.getTitle(), currsong.getArtist(), musicService.getDuration());
                    else
                        expandedPlaybackControl.setSongInfo(currsong.getTitle(), currsong.getArtist(), musicService.getDuration(), currsong.getId());
                } else if (intent.getAction().equals("MUSICPLAYER_QUEUE_SIZE")){
                    if (!isExpanded)playcontrol.updateQueueCount(intent.getIntExtra("MUSICPLAYER_QUEUE_SIZE", 0));
                    else expandedPlaybackControl.setQueueSize(intent.getIntExtra("MUSICPLAYER_QUEUE_SIZE", 0));
                } else if (intent.getAction().equals("MUSICPLAYER_CURRENT_INDEX")){
                    if (isExpanded)expandedPlaybackControl.setQueueIndex(intent.getIntExtra("MUSICPLAYER_CURRENT_INDEX", 0));
                }
            }
        }
    };

    final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(!isOnPause){
                if(!isExpanded)playcontrol.updateSeekbar(musicService.getCurrentPosition());
                else expandedPlaybackControl.updateSeekbar(musicService.getCurrentPosition());
            }
            mHandler.postDelayed(runnable,200);
        }
    };

    private void updatePlaybackControlState(boolean state){
        isOnPause = state;
        if(!isExpanded)playcontrol.setControlButton(state);
        else expandedPlaybackControl.setControlButton(state);
    }

    /*
    Interfaces
     */
    @Override
    public void OnSongListCreatedListener(ArrayList<MusicResolver> songList) {
        musicService.setSonglist(songList);
    }

    @Override
    public void OnSongSelectedListener(int index) {
        musicService.setSong(index);
        updatePlaybackControlState(false);
    }

    @Override
    public void OnSonglistShuffleClickListener() {
        musicService.setPlaybackBehaviour(PlaybackBehaviour.PlaybackBehaviourState.SHUFFLE);
        musicService.shuffle();
        updatePlaybackControlState(false);
    }

    /*
    ExpandedPlaybackControl
     */
    @Override
    public void OnStateChangeListener() {
        if(isOnPause){
            musicService.resume();
        } else {
            musicService.pause();
        }
        isOnPause = !isOnPause;
        updatePlaybackControlState(isOnPause);
    }

    @Override
    public void OnSeekbarChangeListener(int progress) {
        musicService.setProgress(progress);
    }

    @Override
    public void OnSkipPressedListener() {
        musicService.skip();
    }

    @Override
    public void OnSkipPreviousListener(){
        musicService.skipPrevious();
    }

    @Override
    public void OnExpandListener(ExpandedPlaybackControl expandedPlaybackControl) {
        this.expandedPlaybackControl = expandedPlaybackControl;
    }


    private void onPlaybackControlStarted(){
        isExpanded=false;
        playcontrol.setControlButton(isOnPause);
        playcontrol.setAudioSessionID(musicService.getSessionId());
        MusicResolver currsong = musicService.getCurrSong();
        if(currsong!=null)playcontrol.setSongInfo(currsong.getTitle(),currsong.getArtist(),musicService.getDuration());
    }

    private void onExpandedPlaybackControlStarted(){
        if(musicService!=null){
            isExpanded=true;
            MusicResolver currsong = musicService.getCurrSong();
            if(currsong!=null)expandedPlaybackControl.setSongInfo(currsong.getTitle(),currsong.getArtist(),musicService.getDuration(),currsong.getId());
            expandedPlaybackControl.setControlButton(isOnPause);
            expandedPlaybackControl.setBehaviourState(musicService.getPlaybackBehaviour());
            expandedPlaybackControl.setAudioSessionID(musicService.getSessionId());
            expandedPlaybackControl.setQueueSize(musicService.getQueueSize());
            expandedPlaybackControl.setQueueIndex(musicService.getCurrentSongIndex());
            motionLayout.setInteractionEnabled(false);
        }
    }

    @Override
    public void OnBehaviourChangedListener(PlaybackBehaviour.PlaybackBehaviourState newState){
        musicService.setPlaybackBehaviour(newState);
    }

    @Override
    public void OnCloseListener(){
        motionLayout.transitionToStart();
        motionLayout.setInteractionEnabled(true);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        switch (item.getItemId()){
            case R.id.nav_tracklist:{
                selectedDrawerFragment = new SongList();
                //getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,new SongList()).commit();
                break;
            }
            case R.id.nav_album:{
                AlbumFragment albumFragment = new AlbumFragment();
                albumFragment.setQueueDestination(playcontrol.getQueueScreenLocation());
                selectedDrawerFragment = albumFragment;
                //getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, albumFragment).commit();
                break;
            }
            case R.id.nav_playlist:{
                selectedDrawerFragment = new Playlist();
                break;
            }
            case R.id.nav_equalizer:{
                EqualizerViewPager equalizerFragment = new EqualizerViewPager();
                equalizerFragment.setSettings(musicService.getReverbSettings(),
                        musicService.isReverbEnabled(),
                        musicService.getEqualizerBandLevels(),
                        musicService.isEqualizerEnabled(),
                        musicService.getEqualizerProperties(),
                        musicService.isBassBoostEnabled(), musicService.getBassBoostStrength(),
                        musicService.isVirtualizerEnabled(), musicService.getVirtualizerStrength(),
                        musicService.isLoudnessEnhancerEnabled(), musicService.getLoudnessEnhancerStrength());
                selectedDrawerFragment = equalizerFragment;
                //getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,equalizerFragment).commit();
                break;
            }
            case R.id.nav_tagEditor:{
                selectedDrawerFragment = new TagEditorFragment();
                //getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,new TagEditorFragment()).commit();
                break;
            }
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void OnClickListener(String table, View view) {
        PlaylistDetail playlistDetailFragment = new PlaylistDetail();

        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, playlistDetailFragment, playlistDetail).addToBackStack(null).commit();
        databaseViewmodel.setTableName(table);
    }

    @Override
    public void OnPlaylistResumeListener(){
        databaseViewmodel.notifyDatabaseChanged();
    }

    @Override
    public void OnPlaylistCreatedListener(ArrayList<MusicResolver> tracklist){
        songlist=tracklist;
        musicService.setSonglist(tracklist);
    }

    @Override
    public void OnPlaylistItemSelectedListener(int index){
        musicService.setSong(index);
        updatePlaybackControlState(false);
    }

    @Override
    public void OnPlaylistShuffle() {
        musicService.setPlaybackBehaviour(PlaybackBehaviour.PlaybackBehaviourState.SHUFFLE);
        musicService.shuffle();
        updatePlaybackControlState(false);
    }

    @Override
    public void OnAddSongsListener(ArrayList<MusicResolver> selection, String table) {
        databaseViewmodel.addTableEntries(table,selection);
        onBackPressed();
    }

    @Override
    public void onEnvironmentalReverbChanged(EnvironmentalReverb.Settings settings) {
        musicService.setEnvironmentalReverbSettings(settings);
    }

    @Override
    public void onEnvironmentalReverbStatusChanged(boolean status) {
        musicService.setReverbEnabled(status);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getResources().getString(R.string.preference_reverb_isenabled), status);
        editor.apply();
    }

    @Override
    public void onEqualizerChanged(short[] bandLevel) {
        musicService.setEqualizerBandLevels(bandLevel);
    }

    @Override
    public void onEqualizerStatusChanged(boolean state) {
        musicService.setEqualizerEnabled(state);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getResources().getString(R.string.preference_equalizer_isenabled), state);
        editor.apply();
    }

    @Override
    public void onBassBoostChanged(int strength) {
        musicService.setBassBoostStrength((short) strength);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getResources().getString(R.string.preference_bassboost_strength), strength);
        editor.apply();
    }

    @Override
    public void onBassBoostStatusChanged(boolean state) {
        musicService.setBassBoostEnabled(state);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getResources().getString(R.string.preference_bassboost_isenabled), state);
        editor.apply();
    }

    @Override
    public void onVirtualizerChanged(int strength) {
        musicService.setVirtualizerStrength((short) strength);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getResources().getString(R.string.preference_virtualizer_strength), strength);
        editor.apply();
    }

    @Override
    public void onVirtualizerStatusChanged(boolean state) {
        musicService.setVirtualizerEnabled(state);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getResources().getString(R.string.preference_virtualizer_isenabled), state);
        editor.apply();
    }

    @Override
    public void onLoudnessEnhancerChanged(int strength) {
        musicService.setLoudnessEnhancerGain(strength);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getResources().getString(R.string.preference_loudnessenhancer_strength), strength);
        editor.apply();
    }

    @Override
    public void onLoudnessEnhancerStatusChanged(boolean state) {
        musicService.setLoudnessEnhancerEnabled(state);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getResources().getString(R.string.preference_loudnessenhancer_isenabled), state);
        editor.apply();
    }

    @Override
    public void onTrackSelectedListener(MusicResolver musicResolver) {
        TagEditorDetailFragment tagEditorDetailFragment = new TagEditorDetailFragment();
        tagEditorDetailFragment.setTrack(musicResolver);
        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, tagEditorDetailFragment).addToBackStack(null).commit();
    }

    /*
    Navigation Control Interface
     */

    @Override
    public void isDrawerEnabledListener(boolean state) {
        toggle.setDrawerIndicatorEnabled(state);
        if (state){
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        } else {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    @Override
    public void setHomeAsUpIndicator(int resId) {
        getSupportActionBar().setHomeAsUpIndicator(resId);
    }

    @Override
    public void setHomeAsUpEnabled(boolean state) {
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(state);
    }

    @Override
    public void setToolbarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    /*
    Album Interfaces
     */

    @Override
    public void onPlayAlbumListener(int position, ArrayList<MusicResolver> albumTrackList, boolean shuffle) {
        musicService.setSonglist(albumTrackList);
        if (shuffle){
            musicService.setPlaybackBehaviour(PlaybackBehaviour.PlaybackBehaviourState.SHUFFLE);
            musicService.shuffle();
        } else {
            musicService.setPlaybackBehaviour(PlaybackBehaviour.PlaybackBehaviourState.PLAY_ORDER);
            musicService.setSong(position);
        }
        updatePlaybackControlState(false);
    }

    @Override
    public void onQueueAlbumListener(ArrayList<MusicResolver> albumTrackList) {
        //Todo: Implement Queue
    }
}