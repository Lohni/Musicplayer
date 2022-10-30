package com.lohni.musicplayer.ui.settings;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lohni.musicplayer.R;
import com.lohni.musicplayer.database.MusicplayerApplication;
import com.lohni.musicplayer.database.dao.MusicplayerDataAccess;
import com.lohni.musicplayer.database.dao.PreferenceDataAccess;
import com.lohni.musicplayer.database.entity.Preference;
import com.lohni.musicplayer.database.entity.Track;
import com.lohni.musicplayer.database.viewmodel.MusicplayerViewModel;
import com.lohni.musicplayer.database.viewmodel.PreferenceViewModel;
import com.lohni.musicplayer.ui.views.DeletedTrackDialog;
import com.lohni.musicplayer.ui.views.RangeSeekbar;

import java.util.List;
import java.util.Optional;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.Slide;

public class SettingFragment extends Fragment {
    private RangeSeekbar rangeSeekbar;
    private PreferenceViewModel preferenceViewModel;
    private MusicplayerViewModel musicplayerViewModel;
    private List<Preference> preferenceList;

    public SettingFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceDataAccess pda = ((MusicplayerApplication) requireActivity().getApplication()).getDatabase().preferenceDao();
        preferenceViewModel = new ViewModelProvider(this, new PreferenceViewModel.PreferenceViewModelFactory(pda)).get(PreferenceViewModel.class);

        MusicplayerDataAccess mda = ((MusicplayerApplication) requireActivity().getApplication()).getDatabase().musicplayerDao();
        musicplayerViewModel = new ViewModelProvider(this, new MusicplayerViewModel.MusicplayerViewModelFactory(mda)).get(MusicplayerViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        rangeSeekbar = view.findViewById(R.id.settings_range_seekbar);

        init();

        rangeSeekbar.setOnValueChangedListener((value, dragHandle) -> {
            String key = (dragHandle == RangeSeekbar.DragHandle.FROM)
                    ? requireContext().getString(R.string.db_preference_include_from)
                    : requireContext().getString(R.string.db_preference_include_to);

            Optional<Preference> toUpdate = preferenceList.stream()
                    .filter(pref -> pref.getPrefKey().equals(key))
                    .findFirst();

            toUpdate.ifPresent(pref -> {
                pref.setPrefValue(String.valueOf(value));
                preferenceViewModel.updatePreference(pref);
            });
        });

        ConstraintLayout deletedTrack = view.findViewById(R.id.settings_deleted_tracks);
        deletedTrack.setOnClickListener((v) -> {
            musicplayerViewModel.getDeletedTracks().observe(getViewLifecycleOwner(), tracks -> {
                musicplayerViewModel.getDeletedTracks().removeObservers(getViewLifecycleOwner());
                DeletedTrackDialog dialog = new DeletedTrackDialog(tracks);
                dialog.setOnRestoreClickListener((toRestore) -> {
                    for (Track track : toRestore) {
                        track.setTDeleted(0);
                        musicplayerViewModel.updateTrack(track);
                    }
                });
                Slide slide = new Slide();
                slide.setDuration(500);
                slide.setSlideEdge(Gravity.END);
                dialog.setEnterTransition(slide);
                dialog.show(getParentFragmentManager(), "DELETE_DIALOG");
            });
        });

        return view;
    }

    private void init() {
        preferenceViewModel.getAllPreferences().observe(getViewLifecycleOwner(), preferences -> {
            preferenceViewModel.getAllPreferences().removeObservers(getViewLifecycleOwner());
            preferenceList = preferences;

            String includeKeyFrom = requireContext().getString(R.string.db_preference_include_from);
            String includeKeyTo = requireContext().getString(R.string.db_preference_include_to);

            Optional<String> includeFrom = preferenceList.stream()
                    .filter(pref -> pref.getPrefKey().equals(includeKeyFrom))
                    .map(Preference::getPrefValue)
                    .findFirst();

            Optional<String> includeTo = preferenceList.stream()
                    .filter(pref -> pref.getPrefKey().equals(includeKeyTo))
                    .map(Preference::getPrefValue)
                    .findFirst();

            includeFrom.ifPresent(s -> rangeSeekbar.setFrom(Long.parseLong(s)));
            includeTo.ifPresent(s -> rangeSeekbar.setTo(Long.parseLong(s)));
        });
    }
}