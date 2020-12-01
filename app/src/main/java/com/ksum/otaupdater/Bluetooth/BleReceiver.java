package com.ksum.otaupdater.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ksum.otaupdater.Log.Tag;

public class BleReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        Log.d(Tag.BLE_RECEIVER, "BleReceiver onReceive!");

        if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

            switch (state){
                case BluetoothAdapter.STATE_OFF:
                    Log.d(Tag.BLE_RECEIVER, "Bluetooth State OFF!");
                    // 1. Scan 중이면 Scan 중단
                    // 2. Update 중이면 Update 중단
                    // 3. Bluetooth 활성화 SnackBar 띄우기
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    Log.d(Tag.BLE_RECEIVER, "Bluetooth State Turning off!");
                    break;
                case BluetoothAdapter.STATE_ON:
                    Log.d(Tag.BLE_RECEIVER, "Bluetooth State ON!");
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    Log.d(Tag.BLE_RECEIVER, "Bluetooth State Turning on!");
                    break;
            }
        }
    }
}
