package com.ksum.otaupdater.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ksum.otaupdater.ItemTouchHelper.ItemTouchHelperListener;
import com.ksum.otaupdater.Log.Tag;
import com.ksum.otaupdater.R;
import com.ksum.otaupdater.Vo.DeviceInfo;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ScanAdapter extends RecyclerView.Adapter<ScanAdapter.ViewHolder> implements ItemTouchHelperListener {
    private Context context;
    private ArrayList<DeviceInfo> dataList;
    private OnStateButtonClickListener onStateButtonClickListener;

    public ScanAdapter(ArrayList<DeviceInfo> dataList){
        this.dataList = dataList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.textView_name)
        TextView deviceName;
        @BindView(R.id.textView_address)
        TextView address;
        @BindView(R.id.textView_connectable)
        TextView connectable;
        @BindView(R.id.textView_state)
        TextView state;
        @BindView(R.id.progressBar_update)
        ProgressBar updateBar;
        @BindView(R.id.imageButton_state)
        ImageButton stateImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            /* stateImage onClick */
            stateImage.setOnClickListener(v -> {
                int position = getAdapterPosition();

                if(position != RecyclerView.NO_POSITION) {
                    onStateButtonClickListener.onStateClick(dataList.get(position));
                }
            });
        }
    }

    /* Interface State Button Click Listener */
    public interface OnStateButtonClickListener {
        void onStateClick(DeviceInfo deviceInfo);
        void removeDevice(String address);
    }

    public void setOnStateButtonClickListener(OnStateButtonClickListener onStateButtonClickListener){
        this.onStateButtonClickListener = onStateButtonClickListener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.list_item_device, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DeviceInfo deviceInfo = dataList.get(position);

        String name = deviceInfo.getName();

        if(name == null)
            name = context.getString(R.string.device_default_name);

        holder.deviceName.setText(name);
        holder.address.setText(deviceInfo.getAddress());
        holder.connectable.setText(deviceInfo.getConnectable());
        holder.state.setText(context.getText(R.string.item_state_no_implement));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void addItem(DeviceInfo deviceInfo){
        dataList.add(deviceInfo);
        notifyDataSetChanged();
    }

    public DeviceInfo getItem(int position){
        return dataList.get(position);
    }

    @Override
    public boolean onItemMove(int from_position, int to_position) {
        return false;
    }

    @Override
    public void onItemSwipe(int position) {
        DeviceInfo removeDevice = dataList.get(position);
        dataList.remove(position);
        onStateButtonClickListener.removeDevice(removeDevice.getAddress());
    }
}
