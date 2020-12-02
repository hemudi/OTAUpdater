package com.ksum.otaupdater.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.ksum.otaupdater.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ResetDialog extends Dialog {

    @BindView(R.id.reset_cancel)
    Button cancel;
    @BindView(R.id.reset_confirm)
    Button confirm;
    @BindView(R.id.checkbox_scan)
    CheckBox cbScan;
    @BindView(R.id.checkbox_black)
    CheckBox cbBlack;
    @BindView(R.id.checkbox_update)
    CheckBox cbUpdate;

    private static final int CHECKBOX_INDEX_SCAN = 0;
    private static final int CHECKBOX_INDEX_BLACK = 1;
    private static final int CHECKBOX_INDEX_UPDATE = 2;

    private Context context;
    private ResetDialogClickListener resetDialogClickListener;
    private boolean[] isEmpty;

    public ResetDialog(@NonNull  Context context, ResetDialogClickListener resetDialogClickListener, boolean[] isEmpty){
        super(context);
        this.context = context;
        this.resetDialogClickListener = resetDialogClickListener;
        this.isEmpty = isEmpty;
    }

    public interface ResetDialogClickListener {
//        void onPositiveClick(boolean scanChecked, boolean blackChecked, boolean updateChecked);
//        void onPositiveClick(boolean[] isChecked, boolean[] isEnabled);
        void onPositiveClick(CheckBox scan, CheckBox black, CheckBox update);
    }

    // 그냥 업데이트 리스트에 있으면 제거되는 걸루 하자..!!!!

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_quest_reset);
        ButterKnife.bind(this);

        setAllChecked();
        setCheckBoxEnable();

        /* 확인 버튼 */
        confirm.setOnClickListener(v -> {
            boolean scanChecked = cbScan.isChecked();
            boolean blackChecked = cbBlack.isChecked();
            boolean updateChecked = cbUpdate.isChecked();

            if(!scanChecked && !blackChecked && !updateChecked){
                Toast.makeText(context, context.getString(R.string.toast_reset_all_unchecked), Toast.LENGTH_LONG).show();
                return;
            }

            resetDialogClickListener.onPositiveClick(cbScan, cbBlack, cbUpdate);
//            resetDialogClickListener.onPositiveClick(cbScan.isChecked(), cbBlack.isChecked(), cbUpdate.isChecked());  // checked 전달
            dismiss();
        });

        /* 취소 버튼 */
        cancel.setOnClickListener(v -> {
            dismiss();
        });
    }

    private void setCheckBoxEnable(){
        cbScan.setEnabled(!isEmpty[CHECKBOX_INDEX_SCAN]);
        cbBlack.setEnabled(!isEmpty[CHECKBOX_INDEX_BLACK]);
        cbUpdate.setEnabled(!isEmpty[CHECKBOX_INDEX_UPDATE]);
    }

    private void setAllChecked(){
        cbScan.setChecked(true);
        cbBlack.setChecked(true);
        cbUpdate.setChecked(true);
    }

    private boolean[] getIsEmptyList(){
        return new boolean[]{
                cbScan.isChecked(),
                cbBlack.isChecked(),
                cbUpdate.isChecked()
        };
    }

    private boolean[] getIsEnableList(){
        return new boolean[]{
                cbScan.isEnabled(),
                cbBlack.isEnabled(),
                cbUpdate.isEnabled()
        };
    }
}
