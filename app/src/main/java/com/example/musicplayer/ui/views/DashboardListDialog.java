package com.example.musicplayer.ui.views;

import android.app.Dialog;
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

import com.example.musicplayer.R;
import com.example.musicplayer.utils.enums.DashboardEnumDeserializer;
import com.example.musicplayer.utils.enums.DashboardListType;
import com.example.musicplayer.utils.enums.ListFilterType;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import androidx.annotation.NonNull;

public class DashboardListDialog extends Dialog implements View.OnClickListener {
    private final String title;
    private MaterialButton save;
    private MaterialButton cancel;
    private TextInputEditText listSizeInput;

    private DashboardListType selectedListType;
    private ListFilterType selectedFilterType;

    private int listSize;

    ChipGroup typeChips;
    ChipGroup filterChips;

    private Context context;

    private View.OnClickListener onFinish;

    public DashboardListDialog(@NonNull Context context, String title, DashboardListType selectedListType, ListFilterType selectedFilterType, int listSize) {
        super(context);
        this.title = title;
        this.selectedFilterType = selectedFilterType;
        this.selectedListType = selectedListType;
        this.context = context;
        this.listSize = listSize;
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

        listSizeInput.setText(String.valueOf(listSize));
        for (ListFilterType filterType : ListFilterType.values()) {
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
                    listSize = value;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

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

    public ListFilterType getSelectedFilterType() {
        for (int i = 0; i < filterChips.getChildCount(); i++) {
            Chip chip = (Chip) filterChips.getChildAt(i);

            if (chip.isChecked()) {
                return (ListFilterType) chip.getTag();
            }
        }

        return ListFilterType.TIMES_PLAYED;
    }

    public int getSelectedListSize() {
        return listSize;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.dashboard_list_dialog_save) {
            onFinish.onClick(view);
        }

        dismiss();
    }
}
