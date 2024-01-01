package com.lohni.musicplayer;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.navigation.NavigationView;
import com.lohni.musicplayer.core.ApplicationDataViewModel;
import com.lohni.musicplayer.core.MediaStoreSync;
import com.lohni.musicplayer.core.MusicService;
import com.lohni.musicplayer.core.MusicplayerServiceConnection;
import com.lohni.musicplayer.database.MusicplayerApplication;
import com.lohni.musicplayer.database.dao.AudioEffectDataAccess;
import com.lohni.musicplayer.database.dao.MusicplayerDataAccess;
import com.lohni.musicplayer.database.dao.PreferenceDataAccess;
import com.lohni.musicplayer.database.entity.Album;
import com.lohni.musicplayer.database.entity.Track;
import com.lohni.musicplayer.database.enums.PreferenceEnum;
import com.lohni.musicplayer.database.viewmodel.AudioEffectViewModel;
import com.lohni.musicplayer.database.viewmodel.MusicplayerViewModel;
import com.lohni.musicplayer.database.viewmodel.PreferenceViewModel;
import com.lohni.musicplayer.interfaces.NavigationControlInterface;
import com.lohni.musicplayer.interfaces.PlaybackControlInterface;
import com.lohni.musicplayer.interfaces.QueueControlInterface;
import com.lohni.musicplayer.interfaces.ServiceConnectionListener;
import com.lohni.musicplayer.interfaces.ServiceTriggerInterface;
import com.lohni.musicplayer.ui.album.AlbumFragment;
import com.lohni.musicplayer.ui.audioeffects.EqualizerViewPager;
import com.lohni.musicplayer.ui.dashboard.DashboardFragment;
import com.lohni.musicplayer.ui.playbackcontrol.PlaybackControlSheet;
import com.lohni.musicplayer.ui.playlist.PlaylistFragment;
import com.lohni.musicplayer.ui.settings.DatabaseViewerFragment;
import com.lohni.musicplayer.ui.settings.SettingFragment;
import com.lohni.musicplayer.ui.songlist.SongList;
import com.lohni.musicplayer.utils.AdapterUtils;
import com.lohni.musicplayer.utils.Permissions;
import com.lohni.musicplayer.utils.enums.AudioEffectType;
import com.lohni.musicplayer.utils.enums.PlaybackAction;
import com.lohni.musicplayer.utils.enums.PlaybackBehaviour;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity implements PlaybackControlInterface, NavigationView.OnNavigationItemSelectedListener,
        NavigationControlInterface, QueueControlInterface, ServiceConnectionListener, ServiceTriggerInterface {

    private DrawerLayout drawer;
    private MusicService musicService;
    private AppBarLayout appBarLayout;

    private AudioEffectViewModel audioEffectViewModel;
    private MusicplayerViewModel musicplayerViewModel;
    private PreferenceViewModel preferenceViewModel;

    private ActionBarDrawerToggle toggle;
    private MusicplayerServiceConnection serviceConnection;

    private final Handler mHandler = new Handler();
    private Runnable runnable;
    private Fragment selectedDrawerFragment;
    private PlaybackControlSheet playbackControlSheet;
    private boolean destroyed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawer = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        appBarLayout = findViewById(R.id.appbar);
        NavigationView navigationView = findViewById(R.id.nav_view);

        FrameLayout frameLayout = findViewById(R.id.playback_control_sheet);
        playbackControlSheet = new PlaybackControlSheet(frameLayout, this);

        setSupportActionBar(toolbar);
        serviceConnection = new MusicplayerServiceConnection(this);

        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();
        toggle.setToolbarNavigationClickListener(view -> onBackPressed());
        drawer.addDrawerListener(toggle);

        AudioEffectDataAccess aod = ((MusicplayerApplication) getApplication()).getDatabase().audioEffectDao();
        audioEffectViewModel = new ViewModelProvider(this, new AudioEffectViewModel.AudioEffectViewModelFactory(aod)).get(AudioEffectViewModel.class);
        MusicplayerDataAccess mda = ((MusicplayerApplication) getApplication()).getDatabase().musicplayerDao();
        musicplayerViewModel = new ViewModelProvider(this, new MusicplayerViewModel.MusicplayerViewModelFactory(mda)).get(MusicplayerViewModel.class);

        PreferenceDataAccess pda = ((MusicplayerApplication) getApplication()).getDatabase().preferenceDao();
        preferenceViewModel = new ViewModelProvider(this, new PreferenceViewModel.PreferenceViewModelFactory(pda)).get(PreferenceViewModel.class);

        Intent service = new Intent(this, MusicService.class);

        if (Permissions.permission(this, Manifest.permission.RECORD_AUDIO)) {
            bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);
            startForegroundService(service);

            String perm = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    ? Manifest.permission.READ_MEDIA_AUDIO
                    : Manifest.permission.READ_EXTERNAL_STORAGE;

            if (Permissions.permission(this, perm)) {
                updateTracks();
                updateAlbums();
            }
        }

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

        navigationView.setNavigationItemSelectedListener(this);
    }

    private void updateTracks() {
        musicplayerViewModel.getAllTracks().observe(this, tracks -> {
            compareTracksToDatabase((ArrayList<Track>) tracks);
            musicplayerViewModel.getAllTracks().removeObservers(this);
            loadDashboard(new DashboardFragment());
        });
    }

    private void updateAlbums() {
        musicplayerViewModel.getAllAlbums().observe(this, albums -> {
            compareAlbumsToDatabase((ArrayList<Album>) albums);
            musicplayerViewModel.getAllAlbums().removeObservers(this);
        });
    }

    private void compareTracksToDatabase(ArrayList<Track> tracks) {
        preferenceViewModel.observeOnce(preferenceViewModel.getPreferenceById(PreferenceEnum.WA_AUDIO_REGEX.getId()), this, pref -> {
            ArrayList<Track> toInsert = MediaStoreSync.Companion.syncMediaStoreTracks(getApplicationContext(), tracks, pref);

            AdapterUtils.loadCoverImagesAsync(getBaseContext(), toInsert, new ViewModelProvider(this).get(ApplicationDataViewModel.class));

            ArrayList<Track> toDelete = tracks.stream().filter(track -> !toInsert.contains(track)).collect(Collectors.toCollection(ArrayList::new));
            if (!toDelete.isEmpty()) musicplayerViewModel.deleteTracks(toDelete);

            musicplayerViewModel.insertTracks(toInsert);
        });
    }

    private void compareAlbumsToDatabase(ArrayList<Album> albums) {
        ArrayList<Album> toInsert = MediaStoreSync.Companion.syncMediaStoreAlbums(getApplicationContext(), albums);
        musicplayerViewModel.insertAlbums(toInsert);

        musicplayerViewModel.getAllAlbumsWithTracks().observe(this, albumWithTracks -> {
            musicplayerViewModel.getAllAlbumsWithTracks().removeObservers(this);
            AdapterUtils.loadAlbumCoverImagesAsync(getBaseContext(), albumWithTracks, new ViewModelProvider(this).get(ApplicationDataViewModel.class));
        });
    }

    private void loadDashboard(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.nav_host_fragment, fragment);
        ft.commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (permissions[0].equals(Manifest.permission.RECORD_AUDIO)) {
                Intent service = new Intent(this, MusicService.class);
                bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);
                startService(service);

                String perm = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        ? Manifest.permission.READ_MEDIA_AUDIO
                        : Manifest.permission.READ_EXTERNAL_STORAGE;

                if (Permissions.permission(this, perm)) {
                    updateTracks();
                    updateAlbums();
                }
            } else if (permissions[0].equals(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    || (permissions[0].equals(Manifest.permission.READ_MEDIA_AUDIO))) {
                updateTracks();
                updateAlbums();
            }
        }
    }

    private void initialiseAudioEffects() {
        audioEffectViewModel.getActiveAdvancedReverbPreset().observe(this, reverbSettings -> {
            audioEffectViewModel.getActiveAdvancedReverbPreset().removeObservers(this);
            if (reverbSettings != null) {
                sendBroadcast(new Intent(getString(R.string.musicservice_audioeffect))
                        .putExtra("VALUES", reverbSettings)
                        .putExtra("EFFECT_TYPE", AudioEffectType.Companion.getIntFromAudioEffectType(AudioEffectType.ENV_REVERB)));
            }
        });
        audioEffectViewModel.getActiveEqualizerPreset().observe(this, equalizerPreset -> {
            audioEffectViewModel.getActiveEqualizerPreset().removeObservers(this);
            if (equalizerPreset != null)
                sendBroadcast(new Intent(getString(R.string.musicservice_audioeffect))
                        .putExtra("VALUES", equalizerPreset)
                        .putExtra("EFFECT_TYPE", AudioEffectType.Companion.getIntFromAudioEffectType(AudioEffectType.EQUALIZER)));
        });

        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        boolean eqEnabled = sharedPreferences.getBoolean(getString(R.string.preference_equalizer_isenabled), false);
        sendBroadcast(new Intent(getString(R.string.musicservice_audioeffect))
                .putExtra("EFFECT_TYPE", AudioEffectType.Companion.getIntFromAudioEffectType(AudioEffectType.EQUALIZER))
                .putExtra("ENABLED", eqEnabled));
        boolean bbEnabled = sharedPreferences.getBoolean(getString(R.string.preference_bassboost_isenabled), false);
        sendBroadcast(new Intent(getString(R.string.musicservice_audioeffect))
                .putExtra("EFFECT_TYPE", AudioEffectType.Companion.getIntFromAudioEffectType(AudioEffectType.BASSBOOST))
                .putExtra("ENABLED", bbEnabled));
        boolean loudnessEnabled = sharedPreferences.getBoolean(getString(R.string.preference_loudnessenhancer_isenabled), false);
        sendBroadcast(new Intent(getString(R.string.musicservice_audioeffect))
                .putExtra("EFFECT_TYPE", AudioEffectType.Companion.getIntFromAudioEffectType(AudioEffectType.LOUDNESS_ENHANCER))
                .putExtra("ENABLED", loudnessEnabled));
        boolean reverbEnabled = sharedPreferences.getBoolean(getString(R.string.preference_reverb_isenabled), false);
        sendBroadcast(new Intent(getString(R.string.musicservice_audioeffect))
                .putExtra("EFFECT_TYPE", AudioEffectType.Companion.getIntFromAudioEffectType(AudioEffectType.ENV_REVERB))
                .putExtra("ENABLED", reverbEnabled));
        boolean virtualizerEnabled = sharedPreferences.getBoolean(getString(R.string.preference_virtualizer_isenabled), false);
        sendBroadcast(new Intent(getString(R.string.musicservice_audioeffect))
                .putExtra("EFFECT_TYPE", AudioEffectType.Companion.getIntFromAudioEffectType(AudioEffectType.VIRTUALIZER))
                .putExtra("ENABLED", virtualizerEnabled));
    }

    @Override
    protected void onPause() {
        if (musicService != null) {
            musicService.sendOnSongCompleted();
        }
        destroyed = true;
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (destroyed) {
            destroyed = false;
            mHandler.post(runnable);
        }

        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        stopService(new Intent(this, MusicService.class));
    }

    /*
    PlaybackControl
     */
    @Override
    public void onStateChangeListener() {
        if (musicService.isPlaying()) {
            musicService.pause();
        } else {
            musicService.resume();
        }
    }

    @Override
    public void onNextClickListener() {
        musicService.skip(PlaybackAction.SKIP_NEXT);
    }

    @Override
    public void onPreviousClickListener() {
        musicService.skip(PlaybackAction.SKIP_PREVIOUS);
    }

    @Override
    public void onPlaybackBehaviourChangeListener(@NonNull PlaybackBehaviour newState) {
        musicService.setPlaybackBehaviour(newState);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        if (item.getItemId() == R.id.nav_tracklist) {
            selectedDrawerFragment = new SongList();
        } else if (item.getItemId() == R.id.nav_album) {
            selectedDrawerFragment = new AlbumFragment();
        } else if (item.getItemId() == R.id.nav_playlist) {
            selectedDrawerFragment = new PlaylistFragment();
        } else if (item.getItemId() == R.id.nav_equalizer) {
            EqualizerViewPager equalizerFragment = new EqualizerViewPager();
            equalizerFragment.setSettings(musicService.getEqualizerBandLevels(), musicService.getEqualizerProperties());
            selectedDrawerFragment = equalizerFragment;
        } else if (item.getItemId() == R.id.nav_settings) {
            selectedDrawerFragment = new SettingFragment();
        } else if (item.getItemId() == R.id.nav_database_viewer) {
            selectedDrawerFragment = new DatabaseViewerFragment();
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void isDrawerEnabledListener(boolean state) {
        toggle.setDrawerIndicatorEnabled(state);
        int drawerState = (state) ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
        drawer.setDrawerLockMode(drawerState);
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

    @Override
    public void onBackPressedListener() {
        onBackPressed();
    }

    @Override
    public void setToolbarBackground(boolean scrolling) {
        int surface = getResources().getColor(R.color.colorSurface, null);
        int level2 = getResources().getColor(R.color.colorSurfaceLevel2, null);

        if (scrolling) {
            ValueAnimator valueAnimatorToolbar = ObjectAnimator.ofArgb(appBarLayout, "backgroundColor", surface, level2);
            valueAnimatorToolbar.setDuration(500);
            valueAnimatorToolbar.setEvaluator(new ArgbEvaluator());

            ValueAnimator valueAnimatorStatusBar = ObjectAnimator.ofArgb(getWindow(), "statusBarColor", surface, level2);
            valueAnimatorStatusBar.setDuration(500);
            valueAnimatorStatusBar.setEvaluator(new ArgbEvaluator());

            valueAnimatorToolbar.start();
            valueAnimatorStatusBar.start();
        } else {
            ValueAnimator valueAnimatorToolbar = ObjectAnimator.ofArgb(appBarLayout, "backgroundColor", level2, surface);
            valueAnimatorToolbar.setDuration(300);
            valueAnimatorToolbar.setEvaluator(new ArgbEvaluator());

            ValueAnimator valueAnimatorStatusBar = ObjectAnimator.ofArgb(getWindow(), "statusBarColor", level2, surface);
            valueAnimatorStatusBar.setDuration(300);
            valueAnimatorStatusBar.setEvaluator(new ArgbEvaluator());

            valueAnimatorToolbar.start();
            valueAnimatorStatusBar.start();
        }
    }

    @Override
    public void triggerCurrentDataBroadcast() {
        if (musicService != null) musicService.sendCurrentStateToPlaybackControl();
    }

    @Override
    public void onSongSelectedListener(@NonNull Track track) {
        musicService.playSong(track);
    }

    @Override
    public void onSongListCreatedListener(@NonNull List<? extends Track> trackList, Object listTypePayload, boolean play) {
        musicService.removeAllTracks();
        musicService.setListTypePayload(listTypePayload);
        musicService.addSongList((ArrayList<Track>) trackList, play);
    }

    @Override
    public void onAddSongsToSonglistListener(@NonNull List<? extends Track> trackList, boolean next) {
        musicService.addSongList((ArrayList<Track>) trackList, next);
    }

    @Override
    public void onSongsRemoveListener(@NonNull List<? extends Track> tracks) {
        musicService.removeTracks((ArrayList<Track>) tracks);
    }

    @Override
    public void onRemoveAllSongsListener() {
        musicService.removeAllTracks();
    }

    @Override
    public void onOrderChangeListener(int fromPosition, int toPosition) {
        musicService.changeOrder(fromPosition, toPosition);
    }

    @Override
    public void onServiceConnected(@NonNull MusicService musicService) {
        this.musicService = musicService;
        initialiseAudioEffects();

        runnable = () -> {
            playbackControlSheet.updateSeekbar(musicService.getCurrentPosition());
            if (!destroyed) mHandler.postDelayed(runnable, 200);
        };

        runnable.run();
    }
}