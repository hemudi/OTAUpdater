package com.ksum.otaupdater.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ksum.otaupdater.R;
import com.ksum.otaupdater.Vo.DeviceInfo;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ScanListAdapter extends RecyclerView.Adapter<ScanViewHolder> {
    private Context context;
    private LinkedHashMap<String, DeviceInfo> mDataList;

    public ScanListAdapter(){}

    public ScanListAdapter(LinkedHashMap<String, DeviceInfo> dataList){
        mDataList = dataList;
    }

    @NonNull
    @Override
    public ScanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.list_item_device, parent, false);

        return new ScanViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull ScanViewHolder holder, int position) {
        ArrayList<DeviceInfo> dataList = new ArrayList<>(mDataList.values());
        DeviceInfo deviceInfo = dataList.get(position);

        holder.name.setText(deviceInfo.getName());
        holder.address.setText(deviceInfo.getAddress());
        holder.connectable.setText(deviceInfo.getConnectable());
        holder.status.setText(context.getString(R.string.item_status_no_implement));
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    // TODO 1 getStatusString()
    // TODO 2 getStatusImage()
}
