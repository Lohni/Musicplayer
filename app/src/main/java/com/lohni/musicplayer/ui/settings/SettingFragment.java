package com.lohni.musicplayer.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.lohni.musicplayer.R;
import com.lohni.musicplayer.database.MusicplayerApplication;
import com.lohni.musicplayer.database.dao.MusicplayerDataAccess;
import com.lohni.musicplayer.database.dao.PreferenceDataAccess;
import com.lohni.musicplayer.database.entity.Preference;
import com.lohni.musicplayer.database.entity.Track;
import com.lohni.musicplayer.database.enums.PreferenceEnum;
import com.lohni.musicplayer.database.viewmodel.MusicplayerViewModel;
import com.lohni.musicplayer.database.viewmodel.PreferenceViewModel;
import com.lohni.musicplayer.ui.views.DeletedTrackDialog;
import com.lohni.musicplayer.ui.views.RangeSeekbar;

import java.util.List;
import java.util.Optional;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class SettingFragment extends Fragment {
    private PreferenceViewModel preferenceViewModel;
    private MusicplayerViewModel musicplayerViewModel;
    private List<Preference> preferenceList;

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
        RangeSeekbar rangeSeekbar = view.findViewById(R.id.settings_range_seekbar);
        SwitchMaterial switchWA = view.findViewById(R.id.settings_exclude_wa_switch);

        init(rangeSeekbar, switchWA);

        rangeSeekbar.setOnValueChangedListener((value, dragHandle) -> {
            PreferenceEnum targetPref = (dragHandle == RangeSeekbar.DragHandle.FROM)
                    ? PreferenceEnum.INCLUDE_DURATION_FROM
                    : PreferenceEnum.INCLUDE_DURATION_TO;

            Optional<Preference> toUpdate = preferenceList.stream()
                    .filter(pref -> pref.getPrefId().equals(targetPref.getId()))
                    .findFirst();

            toUpdate.ifPresent(pref -> {
                pref.setPrefValue(String.valueOf(value));
                preferenceViewModel.updatePreference(pref);
            });
        });

        switchWA.setOnClickListener(v -> {
            Optional<Preference> toUpdate = preferenceList.stream()
                    .filter(pref -> pref.getPrefId().equals(PreferenceEnum.WA_AUDIO_REGEX.getId()))
                    .findFirst();

            toUpdate.ifPresent(pref -> {
                if (pref.getPrefValue().isEmpty())
                    pref.setPrefValue(requireContext().getString(R.string.wa_audio_regex));
                else pref.setPrefValue("");
                switchWA.setChecked(pref.getPrefValue().isEmpty());
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
                dialog.show(getParentFragmentManager(), "DELETE_DIALOG");
            });
        });

        return view;
    }

    private void init(RangeSeekbar rangeSeekbar, SwitchMaterial switchWA) {
        preferenceViewModel.observeOnce(preferenceViewModel.getAllPreferences(), getViewLifecycleOwner(), preferences -> {
            preferenceList = preferences;

            Optional<String> includeFrom = preferenceList.stream()
                    .filter(pref -> pref.getPrefId().equals(PreferenceEnum.INCLUDE_DURATION_FROM.getId()))
                    .map(Preference::getPrefValue)
                    .findFirst();

            Optional<String> includeTo = preferenceList.stream()
                    .filter(pref -> pref.getPrefId().equals(PreferenceEnum.INCLUDE_DURATION_TO.getId()))
                    .map(Preference::getPrefValue)
                    .findFirst();

            Optional<String> excludeWa = preferenceList.stream()
                    .filter(pref -> pref.getPrefId().equals(PreferenceEnum.WA_AUDIO_REGEX.getId()))
                    .map(Preference::getPrefValue)
                    .findFirst();

            includeFrom.ifPresent(s -> rangeSeekbar.setFrom(Long.parseLong(s)));
            includeTo.ifPresent(s -> rangeSeekbar.setTo(Long.parseLong(s)));
            excludeWa.ifPresent(s -> switchWA.setChecked(!s.isEmpty()));
        });
    }
}