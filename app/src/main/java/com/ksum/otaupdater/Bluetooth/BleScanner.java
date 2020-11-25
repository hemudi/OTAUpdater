package com.ksum.otaupdater.Bluetooth;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.ksum.otaupdater.Log.Tag;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.exceptions.BleScanException;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanSettings;

import java.util.ArrayList;

import io.reactivex.disposables.Disposable;

public class BleScanner {
    private static final int RESTART_DELAY_TIME = 1000 * 60 * 29;

    private RxBleClient mRxBleClient;
    private PendingIntent callbackIntent;
    private ScanSettings scanSettings;
    private final ArrayList<ScanFilter> scanFilterList = new ArrayList<>();

    private BleScanner(){}
    private static class LazyHolder {
        private static final BleScanner INSTANCE = new BleScanner();
    }

    public static BleScanner getInstance() {
        return LazyHolder.INSTANCE;
    }

    public synchronized boolean setRxClient(Context context){
        if(BleManager.getInstance().getBluetoothAdapter() == null){
            Log.d(Tag.BLE_SCANNER, "Bluetooth Adapter -> null");
            return false;
        }

        if(mRxBleClient != null){
            Log.d(Tag.BLE_SCANNER, "rxBleClient -> null");
            mRxBleClient = null;
        }

        mRxBleClient = RxBleClient.create(context);
        callbackIntent = PendingIntent.getBroadcast(context, 1000, new Intent(context, ScanReceiver.class), 0);

        return true;
    }

    public synchronized RxBleClient getRxBleClient(){
        return mRxBleClient;
    }

    public void setMacOfScanFilter(ArrayList<String> mac_list){
        ScanFilter.Builder builder = new ScanFilter.Builder();

        for(String mac : mac_list){
            builder.setDeviceAddress(mac);
            scanFilterList.add(builder.build());
        }
    }

    public void startScan(){
        new Handler().postDelayed(this::startScan, RESTART_DELAY_TIME);

        stopScan();

        setScanSettings();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            Log.d(Tag.BLE_SCANNER, "scan start in background!");
            scanProcessBackground();
        }

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            Log.d(Tag.BLE_SCANNER, "scan start");
            scanProcess();
        }
    }

    private void setScanSettings(){
        scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build();
    }

    private Disposable scanSubscription;

    private void scanProcess(){
        scanSubscription = mRxBleClient.scanBleDevices(
                scanSettings,
                scanFilterList.toArray(new ScanFilter[0])
        ).subscribe(
                scanResult -> {
                    // TODO MainActivity 로 scanResult 전송
                },
                throwable -> {
                    Log.d(Tag.BLE_SCANNER, "scanProcess Failed");
                    throwable.printStackTrace();
                }
        );
    }

    private void scanProcessBackground(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            try {
                mRxBleClient.getBackgroundScanner()
                            .scanBleDeviceInBackground(callbackIntent, scanSettings, scanFilterList.toArray(new ScanFilter[0]));
            } catch (BleScanException scanException){
                Log.d(Tag.BLE_SCANNER, "Background scan Failed");
            }
        }
    }

    public void stopScan(){
        if(scanSubscription != null && !scanSubscription.isDisposed()) {
            scanSubscription.dispose();
        }

        scanSubscription = null;
    }

    public void stopBackgroundScan(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            mRxBleClient.getBackgroundScanner().stopBackgroundBleScan(callbackIntent);
            Log.d(Tag.BLE_SCANNER, "Background Scan Stop!");
        }
    }
}
