package com.example.musicplayer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.text.InputType;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.musicplayer.entities.MusicResolver;
import com.example.musicplayer.ui.DatabaseViewmodel;
import com.example.musicplayer.ui.dashboard.DashboardFragment;
import com.example.musicplayer.ui.equalizer.EffectFragment;
import com.example.musicplayer.ui.equalizer.EqualizerFragment;
import com.example.musicplayer.ui.equalizer.EqualizerInterface;
import com.example.musicplayer.ui.equalizer.EqualizerViewPager;
import com.example.musicplayer.ui.expandedplaybackcontrol.ExpandedPlaybackControl;
import com.example.musicplayer.ui.expandedplaybackcontrol.ExpandedPlaybackControlInterface;
import com.example.musicplayer.ui.expandedplaybackcontrol.ExpandedTransition;
import com.example.musicplayer.ui.playbackcontrol.PlaybackControl;
import com.example.musicplayer.ui.playbackcontrol.PlaybackControlInterface;
import com.example.musicplayer.ui.playlist.Playlist;
import com.example.musicplayer.ui.playlist.PlaylistInterface;
import com.example.musicplayer.ui.playlistdetail.PlaylistDetail;
import com.example.musicplayer.ui.playlistdetail.PlaylistDetailAdd;
import com.example.musicplayer.ui.songlist.SongList;
import com.example.musicplayer.ui.songlist.SongListInterface;
import com.example.musicplayer.ui.tagEditor.TagEditorDetailFragment;
import com.example.musicplayer.ui.tagEditor.TagEditorFragment;
import com.example.musicplayer.ui.tagEditor.TagEditorInterface;
import com.example.musicplayer.ui.views.PlaybackControlSeekbar;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.NonNull;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
        ExpandedPlaybackControlInterface, PlaylistInterface, EqualizerInterface, TagEditorInterface {

    private static final int PERMISSION_REQUEST_CODE = 0x03 ;

    DrawerLayout drawer;
    MusicService musicService;
    ArrayList<MusicResolver> songlist;
    private PlaybackControl playcontrol;
    private Playlist playlistFragment;
    private PlaylistDetail playlistDetailFragment;
    private PlaylistDetailAdd selectionFragment;
    private ExpandedPlaybackControl expandedPlaybackControl;

    private Equalizer equalizer;

    private DatabaseViewmodel databaseViewmodel;

    private final int MENU_CONFIG_PLAYLIST=1,MENU_CONFIG_PLAYLIST_DETAIL=2,MENU_CONFIG_TRACK_SELECTOR=3;
    private int actionbarMenuConfig = 0;

    private boolean isOnPause = true, isExpanded=false;
    private Handler mHandler = new Handler();
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        loadPlayControl(playcontrol = new PlaybackControl());
        loadDashboard(new DashboardFragment());

        permission();

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        switch (actionbarMenuConfig){
            case MENU_CONFIG_PLAYLIST:{
                getMenuInflater().inflate(R.menu.playlist_menu, menu);
                break;
            }
            case MENU_CONFIG_PLAYLIST_DETAIL:{
                getMenuInflater().inflate(R.menu.playlist_detail_add, menu);
                break;
            }
            case MENU_CONFIG_TRACK_SELECTOR:{
                break;
            }
            default:{
                getMenuInflater().inflate(R.menu.main, menu);
                break;
            }
        }
        return true;
    }

    //Service Connection
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)iBinder;
            musicService = binder.getServiceInstance();
            IntentFilter filter = new IntentFilter();
            filter.addAction("START");
            musicService.registerReceiver(broadcastReceiver,filter);
            equalizer = new Equalizer(0,musicService.getSessionId());
            playcontrol.setAudioSessionID(musicService.getSessionId());
            equalizer.setEnabled(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            musicService.pause();
            musicService = null;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Intent service = new Intent(this,MusicService.class);
        bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);
        startService(service);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        unbindService(serviceConnection);
        equalizer.release();
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

    //Interfaces
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.action_playlist_add){
            AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("New playlist");

            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String newTable = input.getText().toString();
                    databaseViewmodel.createNewTable(newTable);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            builder.show();
        } else if(item.getItemId()==R.id.action_playlist_detail_add){

            selectionFragment = new PlaylistDetailAdd();

            Slide anim = new Slide();
            anim.setSlideEdge(Gravity.RIGHT);
            anim.setDuration(200);

            selectionFragment.setEnterTransition(anim);
            getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,selectionFragment).addToBackStack(null).commit();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        toggle.setDrawerIndicatorEnabled(true);
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        switch (item.getItemId()){
            case R.id.nav_tracklist:{
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,new SongList()).commit();
                actionbarMenuConfig=0;
                invalidateOptionsMenu();
                break;
            }
            case R.id.nav_playlist:{
                playlistFragment = new Playlist();
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,playlistFragment).commit();
                actionbarMenuConfig=MENU_CONFIG_PLAYLIST;
                invalidateOptionsMenu();
                break;
            }
            case R.id.nav_equalizer:{
                EqualizerViewPager equalizerFragment = new EqualizerViewPager();
                equalizerFragment.setEqualizer(equalizer);
                equalizerFragment.setAudioSessionID(musicService.getSessionId());
                getSupportActionBar().setTitle("Equalizer");
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,equalizerFragment).commit();
            }
            case R.id.nav_tagEditor:{
                TagEditorFragment tagEditorFragment = new TagEditorFragment();
                getSupportActionBar().setTitle("Tag-Editor");
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,tagEditorFragment).commit();
            }
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void OnClickListener(String table, View view) {
        playlistDetailFragment = new PlaylistDetail();
        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, playlistDetailFragment).addToBackStack(null).commit();
        actionbarMenuConfig=MENU_CONFIG_PLAYLIST_DETAIL;

        toggle.setDrawerIndicatorEnabled(false);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);

        databaseViewmodel.setTableName(table);
        getSupportActionBar().setTitle("");
        invalidateOptionsMenu();
    }

    @Override
    public void OnPlaylistResumeListener(){
        databaseViewmodel.notifyDatabaseChanged();
        invalidateOptionsMenu();
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
    public void OnAddSongsListener() {
        ArrayList<MusicResolver> selection = selectionFragment.getSelected();
        String table = getSupportActionBar().getTitle().toString();
        getSupportActionBar().setTitle("");
        databaseViewmodel.addTableEntries(table,selection);

        onBackPressed();
        invalidateOptionsMenu();
    }

    @Override
    public void OnPlaylistDetailResumeListener() {
        getSupportActionBar().setTitle("");
    }

    @Override
    public void OnTracklistLoadedListener() {
        getSupportActionBar().setTitle(playlistDetailFragment.getTable());
        actionbarMenuConfig=MENU_CONFIG_TRACK_SELECTOR;
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        toggle.setDrawerIndicatorEnabled(false);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_black_24dp);
        invalidateOptionsMenu();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        switch (actionbarMenuConfig){
            case MENU_CONFIG_TRACK_SELECTOR:
                actionbarMenuConfig=MENU_CONFIG_PLAYLIST_DETAIL;
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                toggle.setDrawerIndicatorEnabled(false);

                Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
                break;

            case MENU_CONFIG_PLAYLIST_DETAIL:
                actionbarMenuConfig = MENU_CONFIG_PLAYLIST;
                Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
                toggle.setDrawerIndicatorEnabled(true);
                break;

            default:
                break;
        }
        invalidateOptionsMenu();
    }

    private void permission(){
        //if (Build.VERSION.SDK_INT >= 23) {
        //Check whether your app has access to the READ permission//
        if (checkPermission()) {
            //If your app has access to the device’s storage, then print the following message to Android Studio’s Logcat//
            Log.e("permission", "MODIFY_AUDIO_SETTINGS-Permission already granted.");
        } else {
            //If your app doesn’t have permission to access external storage, then call requestPermission//
            requestPermission();
        }
        //}
    }

    private boolean checkPermission() {
        //Check for READ_EXTERNAL_STORAGE access, using ContextCompat.checkSelfPermission()//
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.MODIFY_AUDIO_SETTINGS);
        //If the app does have this permission, then return true//
        //If the app doesn’t have this permission, then return false//
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.MODIFY_AUDIO_SETTINGS}, PERMISSION_REQUEST_CODE);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onPresetReverbCreated(int id) {
        musicService.setEffect(id);
    }

    @Override
    public void onTrackSelectedListener(MusicResolver musicResolver) {
        TagEditorDetailFragment tagEditorDetailFragment = new TagEditorDetailFragment();
        tagEditorDetailFragment.setTrack(musicResolver);
        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,tagEditorDetailFragment).commit();
    }
}