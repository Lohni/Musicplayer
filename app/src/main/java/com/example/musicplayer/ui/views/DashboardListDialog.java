package com.example.musicplayer.ui.views;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.icu.text.CaseMap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.example.musicplayer.R;
import com.example.musicplayer.utils.enums.DashboardEnumDeserializer;
import com.example.musicplayer.utils.enums.DashboardFilterType;
import com.example.musicplayer.utils.enums.DashboardListType;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import androidx.annotation.NonNull;

public class DashboardListDialog extends Dialog implements View.OnClickListener{
    private final String title;
    private MaterialButton save;
    private MaterialButton cancel;

    private DashboardListType selectedListType;
    private DashboardFilterType selectedFilterType;

    ChipGroup typeChips;
    ChipGroup filterChips;

    private Context context;

    private View.OnClickListener onFinish;

    public DashboardListDialog(@NonNull Context context, String title, DashboardListType selectedListType, DashboardFilterType selectedFilterType) {
        super(context);
        this.title = title;
        this.selectedFilterType = selectedFilterType;
        this.selectedListType = selectedListType;
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_list_edit_dialog);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView title = findViewById(R.id.dashboard_list_dialog_title);
        title.setText(this.title);
        save = findViewById(R.id.dashboard_list_dialog_save);
        cancel = findViewById(R.id.dashboard_list_dialog_cancel);

        typeChips = findViewById(R.id.dashboard_list_dialog_first_chipgroup);
        filterChips = findViewById(R.id.dashboard_list_dialog_second_chipgroup);

        for (DashboardFilterType filterType : DashboardFilterType.values()) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_without_image, filterChips, false);
            chip.setText(DashboardEnumDeserializer.getTitleForFilterType(filterType));
            chip.setTag(filterType);

            if (filterType.equals(selectedFilterType)) {
                chip.setChecked(true);
            }

            filterChips.addView(chip);
        }

        for (DashboardListType listType : DashboardListType.values()) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_without_image, typeChips, false);
            chip.setText(DashboardEnumDeserializer.getTitleForListType(listType));
            chip.setTag(listType);

            if (listType.equals(selectedListType)) {
                chip.setChecked(true);
            }

            typeChips.addView(chip);
        }

        save.setOnClickListener(this);
        cancel.setOnClickListener(this);
    }

    public void setOnFinishListener(View.OnClickListener onFinish) {
        this.onFinish = onFinish;
    }

    public DashboardListType getSelectedListType() {
        for (int i = 0; i < typeChips.getChildCount(); i++) {
            Chip chip = (Chip) typeChips.getChildAt(i);

            if (chip.isChecked()) {
                return (DashboardListType) chip.getTag();
            }
        }

        return DashboardListType.TRACK;
    }

    public DashboardFilterType getSelectedFilterType() {
        for (int i = 0; i < filterChips.getChildCount(); i++) {
            Chip chip = (Chip) filterChips.getChildAt(i);

            if (chip.isChecked()) {
                return (DashboardFilterType) chip.getTag();
            }
        }

        return DashboardFilterType.TIMES_PLAYED;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.dashboard_list_dialog_save) {
            onFinish.onClick(view);
        }

        dismiss();
    }
}
