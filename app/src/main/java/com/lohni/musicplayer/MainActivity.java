package com.lohni.musicplayer;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.navigation.NavigationView;
import com.lohni.musicplayer.core.ApplicationDataViewModel;
import com.lohni.musicplayer.core.MusicService;
import com.lohni.musicplayer.core.MusicplayerServiceConnection;
import com.lohni.musicplayer.database.MusicplayerApplication;
import com.lohni.musicplayer.database.dao.AudioEffectDataAccess;
import com.lohni.musicplayer.database.dao.MusicplayerDataAccess;
import com.lohni.musicplayer.database.dto.AlbumTrackDTO;
import com.lohni.musicplayer.database.entity.Album;
import com.lohni.musicplayer.database.entity.Track;
import com.lohni.musicplayer.database.viewmodel.AudioEffectViewModel;
import com.lohni.musicplayer.database.viewmodel.MusicplayerViewModel;
import com.lohni.musicplayer.interfaces.NavigationControlInterface;
import com.lohni.musicplayer.interfaces.PlaybackControlInterface;
import com.lohni.musicplayer.interfaces.QueueControlInterface;
import com.lohni.musicplayer.interfaces.ServiceConnectionListener;
import com.lohni.musicplayer.interfaces.ServiceTriggerInterface;
import com.lohni.musicplayer.transition.MotionLayoutTransitionListenerImpl;
import com.lohni.musicplayer.ui.album.AlbumFragment;
import com.lohni.musicplayer.ui.audioeffects.EqualizerViewPager;
import com.lohni.musicplayer.ui.dashboard.DashboardFragment;
import com.lohni.musicplayer.ui.playbackcontrol.ExpandedPlaybackControl;
import com.lohni.musicplayer.ui.playbackcontrol.PlaybackControl;
import com.lohni.musicplayer.ui.playlist.PlaylistFragment;
import com.lohni.musicplayer.ui.settings.SettingFragment;
import com.lohni.musicplayer.ui.songlist.SongList;
import com.lohni.musicplayer.ui.tagEditor.TagEditorFragment;
import com.lohni.musicplayer.utils.AdapterUtils;
import com.lohni.musicplayer.utils.GeneralUtils;
import com.lohni.musicplayer.utils.Permissions;
import com.lohni.musicplayer.utils.enums.PlaybackAction;
import com.lohni.musicplayer.utils.enums.PlaybackBehaviourState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

public class MainActivity extends AppCompatActivity implements PlaybackControlInterface, NavigationView.OnNavigationItemSelectedListener,
        NavigationControlInterface, QueueControlInterface, ServiceConnectionListener, ServiceTriggerInterface {

    private DrawerLayout drawer;
    private MusicService musicService;
    private AppBarLayout appBarLayout;

    private AudioEffectViewModel audioEffectViewModel;
    private MusicplayerViewModel musicplayerViewModel;

    private MotionLayout motionLayout;
    private ActionBarDrawerToggle toggle;
    private MusicplayerServiceConnection serviceConnection;

    private final Handler mHandler = new Handler();
    private Runnable runnable;
    private Fragment selectedDrawerFragment;
    private boolean destroyed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawer = findViewById(R.id.drawer_layout);
        motionLayout = findViewById(R.id.parentContainer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        appBarLayout = findViewById(R.id.appbar);
        NavigationView navigationView = findViewById(R.id.nav_view);

        setSupportActionBar(toolbar);
        serviceConnection = new MusicplayerServiceConnection(this);

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
                    if (selectedDrawerFragment instanceof SongList && musicService.getCurrSong() != null) {
                        Bundle bundle = new Bundle();
                        bundle.putInt("ID", musicService.getCurrSong().getTId());
                        selectedDrawerFragment.setArguments(bundle);
                    }
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
            loadPlayControl();
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
        ArrayList<Track> toInsert = new ArrayList<>();

        Uri musicUri = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                ? MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                : MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Cursor musicCursor = getApplicationContext().getContentResolver().query(musicUri, null, null, null, null);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            int titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);
            int albumid = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            int durationColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int trackIdColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TRACK);

            do {
                long thisalbumid = musicCursor.getLong(albumid);
                long thisId = musicCursor.getLong(idColumn);
                long duration = musicCursor.getLong(durationColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                int trackId = musicCursor.getInt(trackIdColumn);

                Track track = new Track();
                track.setTId((int) thisId);
                track.setTAlbumId((int) thisalbumid);
                track.setTTitle(thisTitle);
                track.setTArtist(thisArtist);
                track.setTDuration((int) duration);
                track.setTTrackNr(trackId);
                track.setTIsFavourite(0);
                track.setTDeleted(0);
                track.setTCreated(GeneralUtils.getCurrentUTCTimestamp());

                Optional<Track> optionalTrackDB = tracks.stream().filter(trackDB -> trackDB.getTId().equals((int) thisId)).findFirst();
                if (optionalTrackDB.isPresent()) {
                    Track trackDB = optionalTrackDB.get();
                    track.setTIsFavourite(trackDB.getTIsFavourite());
                    track.setTCreated(trackDB.getTCreated());
                    track.setTDeleted(trackDB.getTDeleted());
                }

                toInsert.add(track);
            } while (musicCursor.moveToNext());
        }

        if (musicCursor != null) musicCursor.close();

        AdapterUtils.loadCoverImagesAsync(getBaseContext(), toInsert, new ViewModelProvider(this).get(ApplicationDataViewModel.class));

        ArrayList<Integer> insertIds = toInsert.stream().map(Track::getTId).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Track> toDelete = tracks.stream().filter(track -> !insertIds.contains(track.getTId())).collect(Collectors.toCollection(ArrayList::new));

        if (!toDelete.isEmpty()) {
            musicplayerViewModel.deleteTracks(toDelete);
        }

        musicplayerViewModel.insertTracks(toInsert);
    }

    private void compareAlbumsToDatabase(ArrayList<Album> albums) {
        ArrayList<Integer> idsFromDatabase = albums.stream().map(Album::getAId).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Album> toInsert = new ArrayList<>();
        ArrayList<Integer> toDelete = new ArrayList<>();

        ContentResolver contentResolver = getApplication().getContentResolver();
        Uri musicUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;

        final String _id = MediaStore.Audio.Albums._ID;
        final String album_name = MediaStore.Audio.Albums.ALBUM;
        final String totSongs = MediaStore.Audio.Albums.NUMBER_OF_SONGS;
        final String artist_Name = MediaStore.Audio.Albums.ARTIST;
        final String albumArt = MediaStore.Audio.Albums.ALBUM_ART;

        Cursor cursor = contentResolver.query(musicUri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int albumIdColum = cursor.getColumnIndex(_id);
            int albumArtistColumn = cursor.getColumnIndex(artist_Name);
            int albumNameColumn = cursor.getColumnIndex(album_name);
            int albumTotSongsColumn = cursor.getColumnIndex(totSongs);
            int artUriColumn = cursor.getColumnIndex(albumArt);

            do {
                long albumId = cursor.getLong(albumIdColum);
                String albumName = cursor.getString(albumNameColumn);
                String albumArtist = cursor.getString(albumArtistColumn);
                int totalSongs = cursor.getInt(albumTotSongsColumn);
                String artUri = cursor.getString(artUriColumn);

                Album album = new Album();
                album.setAId((int) albumId);
                album.setAArtUri(artUri);
                album.setANumSongs(totalSongs);
                album.setAArtistName(albumArtist);
                album.setAName(albumName);
                album.setACreated(GeneralUtils.getCurrentUTCTimestamp());

                toInsert.add(album);

            } while (cursor.moveToNext());
        }
        if (cursor != null) cursor.close();
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

    private void loadPlayControl() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.playbackcontrol_holder, new PlaybackControl(), getString(R.string.fragment_playbackControl));
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
            if (reverbSettings != null)
                sendBroadcast(new Intent(getString(R.string.musicservice_reverb_values)).putExtra("VALUES", reverbSettings));
        });
        audioEffectViewModel.getActiveEqualizerPreset().observe(this, equalizerPreset -> {
            audioEffectViewModel.getActiveEqualizerPreset().removeObservers(this);
            if (equalizerPreset != null)
                sendBroadcast(new Intent(getString(R.string.musicservice_equalizer_values)).putExtra("VALUES", equalizerPreset));
        });

        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        boolean eqEnabled = sharedPreferences.getBoolean(getString(R.string.preference_equalizer_isenabled), false);
        sendBroadcast(new Intent(getString(R.string.musicservice_equalizer_enabled)).putExtra("ENABLED", eqEnabled));
        boolean bbEnabled = sharedPreferences.getBoolean(getString(R.string.preference_bassboost_isenabled), false);
        sendBroadcast(new Intent(getString(R.string.musicservice_bassboost_enabled)).putExtra("ENABLED", bbEnabled));
        boolean loudnessEnabled = sharedPreferences.getBoolean(getString(R.string.preference_loudnessenhancer_isenabled), false);
        sendBroadcast(new Intent(getString(R.string.musicservice_loudness_enhancer_enabled)).putExtra("ENABLED", loudnessEnabled));
        boolean reverbEnabled = sharedPreferences.getBoolean(getString(R.string.preference_reverb_isenabled), false);
        sendBroadcast(new Intent(getString(R.string.musicservice_reverb_enabled)).putExtra("ENABLED", reverbEnabled));
        boolean virtualizerEnabled = sharedPreferences.getBoolean(getString(R.string.preference_virtualizer_isenabled), false);
        sendBroadcast(new Intent(getString(R.string.musicservice_virtualizer_enabled)).putExtra("ENABLED", virtualizerEnabled));
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
    public void onProgressChangeListener(int progress) {
        musicService.setProgress(progress);
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
    public void onPlaybackBehaviourChangeListener(@NonNull PlaybackBehaviourState newState) {
        musicService.setPlaybackBehaviour(newState);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        switch (item.getItemId()) {
            case R.id.nav_tracklist: {
                selectedDrawerFragment = new SongList();
                break;
            }
            case R.id.nav_album: {
                selectedDrawerFragment = new AlbumFragment();
                break;
            }
            case R.id.nav_playlist: {
                selectedDrawerFragment = new PlaylistFragment();
                break;
            }
            case R.id.nav_equalizer: {
                EqualizerViewPager equalizerFragment = new EqualizerViewPager();
                equalizerFragment.setSettings(musicService.getEqualizerBandLevels(), musicService.getEqualizerProperties());
                selectedDrawerFragment = equalizerFragment;
                break;
            }
            case R.id.nav_tagEditor: {
                selectedDrawerFragment = new TagEditorFragment();
                break;
            }
            case R.id.nav_settings: {
                selectedDrawerFragment = new SettingFragment();
                break;
            }
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
        musicService.changeOrder(fromPosition, toPosition, true);
    }

    @Override
    public void onServiceConnected(@NonNull MusicService musicService) {
        this.musicService = musicService;
        initialiseAudioEffects();
        musicService.sendCurrentStateToPlaybackControl();

        runnable = () -> {
            PlaybackControl pc = (PlaybackControl) getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_playbackControl));
            if (pc != null) {
                pc.updateSeekbar(musicService.getCurrentPosition());
            } else {
                ExpandedPlaybackControl epc = (ExpandedPlaybackControl) getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_expandedPlaybackControl));
                if (epc != null) {
                    epc.updateSeekbar(musicService.getCurrentPosition());
                }
            }

            if (!destroyed) {
                mHandler.postDelayed(runnable, 200);
            }
        };

        runnable.run();
    }
}