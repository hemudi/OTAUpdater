package com.ksum.otaupdater.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

public class BleManager {
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private static class LazyHolder {
        private static final BleManager INSTANCE = new BleManager();
    }

    private BleManager(){}

    public static BleManager getInstance(){
        return LazyHolder.INSTANCE;
    }

    public BluetoothAdapter getBluetoothAdapter() { return bluetoothAdapter; }
    public void setBluetoothAdapter(BluetoothAdapter bluetoothAdapter) { this.bluetoothAdapter = bluetoothAdapter; }

    public boolean isNotSupportBle(Context context){
        return !context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public boolean bluetoothIsNotEnabled(){
        return !BleManager.getInstance().getBluetoothAdapter().isEnabled();
    }

    public synchronized void setBleEnable(){
        if(!BleManager.getInstance().getBluetoothAdapter().isEnabled())
            BleManager.getInstance().getBluetoothAdapter().enable();
    }
}
