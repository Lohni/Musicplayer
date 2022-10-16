package com.example.musicplayer.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.musicplayer.R;
import com.example.musicplayer.database.MusicplayerApplication;
import com.example.musicplayer.database.dao.PreferenceDataAccess;
import com.example.musicplayer.database.entity.Preference;
import com.example.musicplayer.database.viewmodel.PreferenceViewModel;
import com.example.musicplayer.ui.views.RangeSeekbar;

import java.util.List;
import java.util.Optional;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class SettingFragment extends Fragment {
    private RangeSeekbar rangeSeekbar;
    private PreferenceViewModel preferenceViewModel;
    private List<Preference> preferenceList;

    public SettingFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceDataAccess pda = ((MusicplayerApplication) requireActivity().getApplication()).getDatabase().preferenceDao();
        preferenceViewModel = new ViewModelProvider(this, new PreferenceViewModel.PreferenceViewModelFactory(pda)).get(PreferenceViewModel.class);
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