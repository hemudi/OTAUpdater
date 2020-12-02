package com.ksum.otaupdater.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.ksum.otaupdater.Log.Tag;
import com.polidea.rxandroidble2.exceptions.BleScanException;
import com.polidea.rxandroidble2.scan.BackgroundScanner;
import com.polidea.rxandroidble2.scan.ScanResult;

import java.util.List;

public class ScanReceiver extends BroadcastReceiver {
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {

        final String action = intent.getAction();

        if(!action.equals("ScanResult")){
            Log.d(Tag.SCAN_RECEIVER, "difference action");
            return;
        }

        if(BleScanner.getInstance().getRxBleClient() == null){
            Log.d(Tag.SCAN_RECEIVER, "rxBleClient -> null");
            return;
        }

        //Log.d(Tag.SCAN_RECEIVER, "ScanReceiver onReceive!");

        scanResultProcess(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void scanResultProcess(Intent intent){
        BackgroundScanner backgroundScanner = BleScanner.getInstance().getRxBleClient().getBackgroundScanner();

        if(backgroundScanner == null){
            Log.d(Tag.SCAN_RECEIVER, "Background Scanner is null!");
            return;
        }

        try {
            final List<ScanResult> scanResultList = backgroundScanner.onScanResultReceived(intent);

            if(scanResultList == null){
                Log.d(Tag.SCAN_RECEIVER, "scanResultList -> null");
                return;
            }

            for(ScanResult scanResult : scanResultList){
                BleDataManager.getInstance().addScanData(scanResult);
            }
        } catch (BleScanException exception){
            Log.d(Tag.SCAN_RECEIVER, "Failed to scan devices : " + exception.getMessage());
        }
    }
}
