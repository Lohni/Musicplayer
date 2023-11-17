package com.lohni.musicplayer.ui.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.lohni.musicplayer.R;
import com.lohni.musicplayer.database.entity.DashboardListConfiguration;
import com.lohni.musicplayer.utils.enums.ListFilterType;
import com.lohni.musicplayer.utils.enums.ListType;

import java.util.Optional;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;

public class DashboardListDialog extends AppCompatDialog implements View.OnClickListener {
    private final String title;
    private MaterialButton save;
    private MaterialButton cancel;
    private TextInputEditText listSizeInput;
    private DashboardListConfiguration listConfiguration;
    private ChipGroup typeChips;
    private ChipGroup filterChips;
    private Context context;

    private View.OnClickListener onFinish;

    public DashboardListDialog(@NonNull Context context, String title, DashboardListConfiguration listConfiguration) {
        super(context);
        this.title = title;
        this.listConfiguration = listConfiguration;
        this.context = context;
    }

    public DashboardListConfiguration getListConfiguration() {
        listConfiguration.setListFilterType(getSelectedFilterType());
        listConfiguration.setListType(getSelectedListType());
        return listConfiguration;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.dashboard_list_dialog_save) {
            onFinish.onClick(view);
        }
        dismiss();
    }

    public void setOnFinishListener(View.OnClickListener onFinish) {
        this.onFinish = onFinish;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_list_edit_dialog);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        TextView title = findViewById(R.id.dashboard_list_dialog_title);
        title.setText(this.title);
        save = findViewById(R.id.dashboard_list_dialog_save);
        cancel = findViewById(R.id.dashboard_list_dialog_cancel);
        listSizeInput = findViewById(R.id.dashboard_list_dialog_list_size);

        typeChips = findViewById(R.id.dashboard_list_dialog_first_chipgroup);
        filterChips = findViewById(R.id.dashboard_list_dialog_second_chipgroup);

        listSizeInput.setText(String.valueOf(listConfiguration.getListSize()));
        for (ListFilterType filterType : ListFilterType.values()) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_without_image, filterChips, false);
            chip.setText(ListFilterType.Companion.getTitleForFilterType(filterType));
            chip.setTag(filterType);

            if (filterType.equals(listConfiguration.getListFilterType())) {
                chip.setChecked(true);
            }

            filterChips.addView(chip);
        }

        for (ListType listType : ListType.values()) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_without_image, typeChips, false);
            chip.setText(ListType.Companion.getTitleForListType(listType));
            chip.setTag(listType);

            if (listType.equals(listConfiguration.getListType())) {
                chip.setChecked(true);
            }

            typeChips.addView(chip);
        }

        listSizeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().matches("^[0-9]+$")) {
                    Toast.makeText(context, "Input must be a number", Toast.LENGTH_SHORT).show();
                    save.setEnabled(false);
                } else {
                    save.setEnabled(true);
                    int value = Integer.parseInt(charSequence.toString());
                    if (value > 100) {
                        value = 100;
                        listSizeInput.setText("100");
                        Toast.makeText(context, "Max size reached", Toast.LENGTH_SHORT).show();
                    }
                    listConfiguration.setListSize(value);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        save.setOnClickListener(this);
        cancel.setOnClickListener(this);
    }

    private ListType getSelectedListType() {
        return getSelectedChipTag(typeChips).map(res -> (ListType) res).orElse(ListType.TRACK);
    }

    private ListFilterType getSelectedFilterType() {
        return getSelectedChipTag(filterChips).map(res -> (ListFilterType) res).orElse(ListFilterType.TIMES_PLAYED);
    }

    private Optional<Object> getSelectedChipTag(ChipGroup chipGroup) {
        Object res = null;
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (chip.isChecked()) {
                res = chip.getTag();
                break;
            }
        }
        return Optional.ofNullable(res);
    }
}
