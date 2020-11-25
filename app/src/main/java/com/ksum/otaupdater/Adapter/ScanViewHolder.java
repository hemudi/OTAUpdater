package com.ksum.otaupdater.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ksum.otaupdater.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ScanViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.textView_name)
    TextView name;
    @BindView(R.id.textView_address)
    TextView address;
    @BindView(R.id.textView_connectable)
    TextView connectable;
    @BindView(R.id.textView_status)
    TextView status;
    @BindView(R.id.progressBar_update)
    ProgressBar updateBar;
    @BindView(R.id.imageButton_status)
    ImageButton statusImage;

    private final Context context;

    public ScanViewHolder(@NonNull View itemView, Context context) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.context = context;
    }
}
