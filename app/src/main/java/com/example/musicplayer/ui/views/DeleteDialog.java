package com.example.musicplayer.ui.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.musicplayer.R;
import com.google.android.material.button.MaterialButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;

public class DeleteDialog extends AppCompatDialog {
    private String title, description;

    private View.OnClickListener onDelete;

    public DeleteDialog(@NonNull Context context) {
        super(context);
    }

    public DeleteDialog(@NonNull Context context, String title, String description) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete_dialog);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        if (title != null) {
            TextView title = findViewById(R.id.delete_dialog_title);
            title.setText(this.title);
        }

        if (description != null) {
            TextView description = findViewById(R.id.delete_dialog_description);
            description.setText(this.description);
        }

        MaterialButton delete = findViewById(R.id.delete_dialog_delete);
        MaterialButton cancel = findViewById(R.id.delete_dialog_cancel);

        delete.setOnClickListener((view) -> {
            onDelete.onClick(view);
            dismiss();
        });
        cancel.setOnClickListener((view) -> dismiss());
    }

    public void setOnDeleteListener(View.OnClickListener onDelete) {
        this.onDelete = onDelete;
    }
}
