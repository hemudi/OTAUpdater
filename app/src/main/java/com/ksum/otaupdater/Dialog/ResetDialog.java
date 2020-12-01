package com.ksum.otaupdater.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.annotation.NonNull;

import com.ksum.otaupdater.R;

import butterknife.BindView;

public class ResetDialog extends Dialog {

    @BindView(R.id.button_reset_cancel)
    Button cancel;
    @BindView(R.id.button_reset_confirm)
    Button confirm;
    @BindView(R.id.checkbox_remove)
    CheckBox remove;

    private Context context;
    private ResetDialogClickListener resetDialogClickListener;
    private boolean checked;

    public interface ResetDialogClickListener {
        void onPositiveClick(boolean checked);
    }

    public ResetDialog(@NonNull  Context context, ResetDialogClickListener resetDialogClickListener){
        super(context);
        this.context = context;
        this.resetDialogClickListener = resetDialogClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_quest_reset);

        confirm.setOnClickListener(v -> {
            this.resetDialogClickListener.onPositiveClick(remove.isChecked());
            dismiss();
        });

        cancel.setOnClickListener(v -> {
            dismiss();
        });
    }
}
