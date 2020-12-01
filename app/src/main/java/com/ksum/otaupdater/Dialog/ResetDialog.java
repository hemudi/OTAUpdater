package com.ksum.otaupdater.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.ksum.otaupdater.R;

public class ResetDialog extends Dialog {

    private Context context;


    public ResetDialog(@NonNull  Context context){
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_quest_reset);
    }
}
