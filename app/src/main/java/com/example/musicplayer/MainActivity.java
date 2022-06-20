package com.example.musicplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.audiofx.EnvironmentalReverb;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;

import com.example.musicplayer.core.MotionLayoutTransitionListenerImpl;
import com.example.musicplayer.core.MusicplayerServiceConnection;
import com.example.musicplayer.database.MusicplayerApplication;
import com.example.musicplayer.database.dao.AudioEffectDataAccess;
import com.example.musicplayer.database.dao.MusicplayerDataAccess;
import com.example.musicplayer.database.entity.EqualizerPreset;
import com.example.musicplayer.database.entity.Track;
import com.example.musicplayer.database.viewmodel.AudioEffectViewModel;
import com.example.musicplayer.database.viewmodel.MusicplayerViewModel;
import com.example.musicplayer.entities.MusicResolver;
import com.example.musicplayer.inter.PlaybackControlInterface;
import com.example.musicplayer.inter.ServiceTriggerInterface;
import com.example.musicplayer.inter.SongInterface;
import com.example.musicplayer.ui.album.AlbumFragment;
import com.example.musicplayer.ui.audioeffects.AudioEffectInterface;
import com.example.musicplayer.ui.audioeffects.EqualizerViewPager;
import com.example.musicplayer.ui.audioeffects.database.AudioEffectSettingsHelper;
import com.example.musicplayer.ui.dashboard.DashboardFragment;
import com.example.musicplayer.ui.playbackcontrol.PlaybackControl;
import com.example.musicplayer.ui.playlist.PlaylistFragment;
import com.example.musicplayer.ui.songlist.SongList;
import com.example.musicplayer.ui.songlist.SongListInterface;
import com.example.musicplayer.ui.tagEditor.TagEditorDetailFragment;
import com.example.musicplayer.ui.tagEditor.TagEditorFragment;
import com.example.musicplayer.ui.tagEditor.TagEditorInterface;
import com.example.musicplayer.utils.NavigationControlInterface;
import com.example.musicplayer.utils.Permissions;
import com.example.musicplayer.utils.enums.PlaybackBehaviour;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

public class MainActivity extends AppCompatActivity implements SongListInterface, PlaybackControlInterface, NavigationView.OnNavigationItemSelectedListener,
        AudioEffectInterface, TagEditorInterface, NavigationControlInterface, AlbumFragment.AlbumListener, SongInterface,
        ServiceTriggerInterface {

    DrawerLayout drawer;
    MusicService musicService;

    private AudioEffectViewModel audioEffectViewModel;
    private MusicplayerViewModel musicplayerViewModel;

    private MotionLayout motionLayout;
    private ActionBarDrawerToggle toggle;
    private SharedPreferences sharedPreferences;
    private MusicplayerServiceConnection serviceConnection;

    private boolean isOnPause = true;
    private final Handler mHandler = new Handler();

    private Fragment selectedDrawerFragment;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawer = findViewById(R.id.drawer_layout);
        motionLayout = findViewById(R.id.parentContainer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        NavigationView navigationView = findViewById(R.id.nav_view);

        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        serviceConnection = new MusicplayerServiceConnection(this, sharedPreferences);

        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();
        toggle.setToolbarNavigationClickListener(view -> onBackPressed());
        drawer.addDrawerListener(toggle);

        motionLayout.setTransitionListener(new MotionLayoutTransitionListenerImpl(this, getSupportFragmentManager()));

        AudioEffectDataAccess aod = ((MusicplayerApplication) getApplication()).getDatabase().audioEffectDao();
        audioEffectViewModel = new ViewModelProvider(this, new AudioEffectViewModel.AudioEffectViewModelFactory(aod)).get(AudioEffectViewModel.class);
        MusicplayerDataAccess mda = ((MusicplayerApplication) getApplication()).getDatabase().musicplayerDao();
        musicplayerViewModel = new ViewModelProvider(this, new MusicplayerViewModel.MusicplayerViewModelFactory(mda)).get(MusicplayerViewModel.class);

        Intent service = new Intent(this, MusicService.class);

        setSupportActionBar(toolbar);
        loadPlayControl();
        loadDashboard(new DashboardFragment());

        if (Permissions.permission(this, Manifest.permission.RECORD_AUDIO)) {
            bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);
            startService(service);
        }

        //Todo: Own class
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                if (selectedDrawerFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, selectedDrawerFragment).commit();
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

        runnable.run();
        navigationView.setNavigationItemSelectedListener(this);

        updateTracks();
    }

    private void updateTracks() {
        musicplayerViewModel.getAllTracks().observe(this, tracks -> {
            compareToDatabase((ArrayList<Track>) tracks);
        });
    }

    private void compareToDatabase(ArrayList<Track> tracks) {
        ArrayList<Integer> idsFromDatabase = tracks.stream().map(Track::getTId).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Track> toInsert = new ArrayList<>();
        ArrayList<Integer> toDelete = new ArrayList<>();

        ContentResolver contentResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = contentResolver.query(musicUri, null, null, null, null);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            int titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);
            int albumid = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            int durationColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

            do {
                long thisalbumid = musicCursor.getLong(albumid);
                long thisId = musicCursor.getLong(idColumn);
                long duration = musicCursor.getLong(durationColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);

                if (!idsFromDatabase.contains((int) thisId)) {
                    Track track = new Track();
                    track.setTId((int) thisId);
                    track.setTAlbumId((int) thisalbumid);
                    track.setTTitle(thisTitle);
                    track.setTArtist(thisArtist);
                    track.setTDuration((int) duration);

                    toInsert.add(track);
                }

                //Todo: Handle delete
            }
            while (musicCursor.moveToNext());
        }

        if (musicCursor != null) musicCursor.close();
        musicplayerViewModel.insertTracks(toInsert);
    }

    private void loadDashboard(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.nav_host_fragment, fragment);
        ft.commit();
    }

    private void loadPlayControl() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.playbackcontrol_holder, new PlaybackControl(), getString(R.string.fragment_playbackControl));
        ft.commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && permissions[0].equals(Manifest.permission.RECORD_AUDIO)) {
            Intent service = new Intent(this, MusicService.class);
            bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);
            musicService.sendCurrentStateToPlaybackControl();
            initialiseAudioEffects();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(service);
            } else {
                startService(service);
            }
        }
    }

    private void initialiseAudioEffects() {
        audioEffectViewModel.getActiveAdvancedReverbPreset().observe(this, reverbSettings -> {
            if (reverbSettings != null)
                musicService.setEnvironmentalReverbSettings(AudioEffectSettingsHelper.extractReverbValues(reverbSettings));
        });
        audioEffectViewModel.getActiveEqualizerPreset().observe(this, equalizerPreset -> {
            if (equalizerPreset != null)
                musicService.setEqualizerBandLevels(equalizerPreset);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (!isOnPause) {
                musicService.sendCurrentStateToPlaybackControl();
                //if (!isExpanded) playcontrol.updateSeekbar(musicService.getCurrentPosition());
                //else expandedPlaybackControl.updateSeekbar(musicService.getCurrentPosition());
            }
            mHandler.postDelayed(runnable, 200);
        }
    };

    private void updatePlaybackControlState(boolean state) {
        isOnPause = state;
        //if (!isExpanded) playcontrol.setControlButton(state);
        //else expandedPlaybackControl.setControlButton(state);
    }

    /*
    Interfaces
     */
    @Override
    public void OnSongListCreatedListener(ArrayList<MusicResolver> songList) {
        //musicService.setSonglist(songList);
    }

    @Override
    public void OnSongSelectedListener(int index) {
        //musicService.setSong(index);
        updatePlaybackControlState(false);
    }

    @Override
    public void OnSonglistShuffleClickListener() {
        musicService.setPlaybackBehaviour(PlaybackBehaviour.PlaybackBehaviourState.SHUFFLE);
        musicService.shuffle();
        updatePlaybackControlState(false);
    }

    /*
    PlaybackControl
     */
    @Override
    public void onStateChangeListener() {
        if (isOnPause) {
            musicService.resume();
        } else {
            musicService.pause();
        }
        isOnPause = !isOnPause;
        updatePlaybackControlState(isOnPause);
    }

    @Override
    public void onProgressChangeListener(int progress) {
        musicService.setProgress(progress);
    }

    @Override
    public void onNextClickListener() {
        musicService.skip();
    }

    @Override
    public void onPreviousClickListener() {
        musicService.skipPrevious();
    }

    @Override
    public void onPlaybackBehaviourChangeListener(@NonNull PlaybackBehaviour.PlaybackBehaviourState newState) {
        musicService.setPlaybackBehaviour(newState);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        switch (item.getItemId()) {
            case R.id.nav_tracklist: {
                selectedDrawerFragment = new SongList();
                //getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,new SongList()).commit();
                break;
            }
            case R.id.nav_album: {
                AlbumFragment albumFragment = new AlbumFragment();
                //albumFragment.setQueueDestination(playcontrol.getQueueScreenLocation());
                selectedDrawerFragment = albumFragment;
                //getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, albumFragment).commit();
                break;
            }
            case R.id.nav_playlist: {
                selectedDrawerFragment = new PlaylistFragment();
                break;
            }
            case R.id.nav_equalizer: {
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
            case R.id.nav_tagEditor: {
                selectedDrawerFragment = new TagEditorFragment();
                //getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,new TagEditorFragment()).commit();
                break;
            }
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
    public void onEqualizerChanged(EqualizerPreset equalizerPreset) {
        musicService.setEqualizerBandLevels(equalizerPreset);
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
        if (state) {
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
        //musicService.setSonglist(albumTrackList);
        if (shuffle) {
            musicService.setPlaybackBehaviour(PlaybackBehaviour.PlaybackBehaviourState.SHUFFLE);
            musicService.shuffle();
        } else {
            musicService.setPlaybackBehaviour(PlaybackBehaviour.PlaybackBehaviourState.PLAY_ORDER);
            //musicService.setSong(albumTrackList.get(position));
        }
        updatePlaybackControlState(false);
    }

    @Override
    public void onQueueAlbumListener(ArrayList<MusicResolver> albumTrackList) {
        //Todo: Implement Queue
    }

    @Override
    public void triggerCurrentDataBroadcast() {
        if (musicService != null) {
            musicService.sendCurrentStateToPlaybackControl();
        }
    }

    @Override
    public void onSongSelectedListener(@NonNull Track track) {
        musicService.setSong(track);
        updatePlaybackControlState(false);
    }

    @Override
    public void onSongListCreatedListener(@NonNull List<? extends Track> trackList) {
        musicService.setSonglist((ArrayList<Track>) trackList);
    }

    @Override
    public void onAddSongsToSonglistListener(@NonNull List<? extends Track> trackList) {
        musicService.addToSonglist((ArrayList<Track>) trackList);
    }
}