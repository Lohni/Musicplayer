package com.example.musicplayer;

import android.Manifest;
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
import android.transition.Slide;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

import com.example.musicplayer.entities.MusicResolver;
import com.example.musicplayer.ui.DatabaseViewmodel;
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
import com.example.musicplayer.ui.views.PlaybackControlSeekbar;
import com.example.musicplayer.utils.NavigationControlInterface;
import com.example.musicplayer.utils.Permissions;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.NonNull;

import androidx.appcompat.app.ActionBarDrawerToggle;
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
        ExpandedPlaybackControlInterface, PlaylistInterface, AudioEffectInterface, TagEditorInterface, NavigationControlInterface {

    private static final int PERMISSION_REQUEST_CODE = 0x03 ;

    DrawerLayout drawer;
    MusicService musicService;
    ArrayList<MusicResolver> songlist;

    private PlaybackControl playcontrol;
    private ExpandedPlaybackControl expandedPlaybackControl;

    private DatabaseViewmodel databaseViewmodel;
    private AudioEffectViewModel audioEffectViewModel;

    private final String playlistDetail = "FRAGMENT_PLAYLISTDETAIL";

    private boolean isOnPause = true, isExpanded=false;
    private Handler mHandler = new Handler();
    private ActionBarDrawerToggle toggle;

    private SharedPreferences sharedPreferences;

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
            filter.addAction("START");

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

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null){
                if ("START".equals(intent.getAction())) {
                    MusicResolver currsong = musicService.getCurrSong();
                    if (!isExpanded)
                        playcontrol.setSongInfo(currsong.getTitle(), currsong.getArtist(), musicService.getDuration());
                    else
                        expandedPlaybackControl.setSongInfo(currsong.getTitle(), currsong.getArtist(), musicService.getDuration(), currsong.getId());
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

    /*
    Interfaces
     */
    @Override
    public void OnSongListCreatedListener(ArrayList<MusicResolver> songList) {
        musicService.setSonglist(songList);
    }

    @Override
    public void OnSongSelectedListener(int index) {
        isOnPause=false;
        musicService.setSong(index);
        if(!isExpanded)playcontrol.setControlButton(isOnPause);
        else expandedPlaybackControl.setControlButton(isOnPause);
    }

    @Override
    public void OnSonglistShuffleClickListener() {
        isOnPause=false;
        musicService.setShuffle(true);
        musicService.shuffle();
        if(!isExpanded)playcontrol.setControlButton(isOnPause);
        else expandedPlaybackControl.setControlButton(isOnPause);
    }

    /*
    Listener
     */
    @Override
    public void OnStateChangeListener() {
        if(isOnPause){
            isOnPause=false;
            musicService.resume();
        } else {
            isOnPause = true;
            musicService.pause();
        }
        if(!isExpanded)playcontrol.setControlButton(isOnPause);
        else expandedPlaybackControl.setControlButton(isOnPause);
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
    public void OnExpandListener(PlaybackControlSeekbar view, View text) {
        FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
        expandedPlaybackControl = new ExpandedPlaybackControl();
        expandedPlaybackControl.setAudioSessionID(musicService.getSessionId());
        Slide anim = new Slide();
        anim.setSlideEdge(Gravity.BOTTOM);
        anim.setDuration(100);

        expandedPlaybackControl.setEnterTransition(anim);
        ft.replace(R.id.parentContainer,expandedPlaybackControl).addToBackStack(null).commit();

        postponeEnterTransition();
    }

    @Override
    public void OnCloseListener() {
        isExpanded=false;
        MusicResolver currsong = musicService.getCurrSong();
        if(currsong!=null)playcontrol.setSongInfo(currsong.getTitle(),currsong.getArtist(),musicService.getDuration());
        playcontrol.setControlButton(isOnPause);
    }

    @Override
    public void OnStartListener(){
        if(musicService!=null){
            isExpanded=true;
            MusicResolver currsong = musicService.getCurrSong();
            if(currsong!=null)expandedPlaybackControl.setSongInfo(currsong.getTitle(),currsong.getArtist(),musicService.getDuration(),currsong.getId());
            expandedPlaybackControl.setControlButton(isOnPause);
            expandedPlaybackControl.setShuffleButton(musicService.getShuffle());
            expandedPlaybackControl.setRepeatButton(musicService.getRepeat());
            expandedPlaybackControl.setLoopButton(musicService.getRepeatSong());
        }
    }

    @Override
    public void OnShuffleClickListener(){
        boolean shuffle = musicService.getShuffle();
        musicService.setShuffle(!shuffle);
        expandedPlaybackControl.setShuffleButton(!shuffle);
    }

    @Override
    public void OnRepeatClickListener(){
        boolean repeat = musicService.getRepeat();
        musicService.setRepeat(!repeat);
        expandedPlaybackControl.setRepeatButton(!repeat);
    }

    @Override
    public void OnLoopClickListener(){
        boolean loop = musicService.getRepeatSong();
        musicService.setRepeatSong(!loop);
        expandedPlaybackControl.setLoopButton(!loop);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        switch (item.getItemId()){
            case R.id.nav_tracklist:{
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,new SongList()).commit();
                break;
            }
            case R.id.nav_playlist:{
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, new Playlist()).commit();
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
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,equalizerFragment).commit();
                break;
            }
            case R.id.nav_tagEditor:{
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,new TagEditorFragment()).commit();
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
        isOnPause=false;
        musicService.setSong(index);
        if(!isExpanded)playcontrol.setControlButton(isOnPause);
        else expandedPlaybackControl.setControlButton(isOnPause);
    }

    @Override
    public void OnShuffle() {
        isOnPause=false;
        musicService.setShuffle(true);
        musicService.shuffle();
        if(!isExpanded)playcontrol.setControlButton(isOnPause);
        else expandedPlaybackControl.setControlButton(isOnPause);
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
}