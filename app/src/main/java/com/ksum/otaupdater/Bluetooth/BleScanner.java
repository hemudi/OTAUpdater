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

    private Context context;
    private RxBleClient mRxBleClient;
    private PendingIntent callbackIntent;
    private ScanSettings scanSettings;
    private final ArrayList<ScanFilter> scanFilterList = new ArrayList<>();
    private boolean isScanning = false;

    /* Constructor */
    private BleScanner(){}
    private static class LazyHolder {
        private static final BleScanner INSTANCE = new BleScanner();
    }

    public static BleScanner getInstance() {
        return LazyHolder.INSTANCE;
    }

    /* get & set RxClient */
    public synchronized RxBleClient getRxBleClient(){
        return mRxBleClient;
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

        this.context = context;
        mRxBleClient = RxBleClient.create(context);

        return true;
    }

    public synchronized void removeRxClient(){
        mRxBleClient = null;
    }

    /* Scan Start */
    public synchronized boolean startScan(){

        Log.d(Tag.SCAN_TEXT, "startScan");

        if(mRxBleClient == null)
            return false;

        new Handler().postDelayed(this::startScan, RESTART_DELAY_TIME);

        stopScan();
        setScanSettings();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            scanProcessBackground();
            Log.d(Tag.SCAN_TEXT, "scanProcessBackground");
            return true;
        }

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            scanProcess();
            Log.d(Tag.SCAN_TEXT, "scanProcess");
            return true;
        }

        return false;
    }

    /* ScanFilter 설정 */
    public void setScanFilterList(ArrayList<String> mac_list){
        ScanFilter.Builder builder = new ScanFilter.Builder();

        for(String mac : mac_list){
            builder.setDeviceAddress(mac);
            scanFilterList.add(builder.build());
        }
    }

    /* scanSetting 설정 */
    private synchronized void setScanSettings(){
        scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build();
    }

    /* Scan Process */
    private Disposable scanSubscription;
    private synchronized void scanProcess(){
        scanSubscription = mRxBleClient.scanBleDevices(
                scanSettings,
                scanFilterList.toArray(new ScanFilter[0])
        ).subscribe(
                scanResult -> BleDataManager.getInstance().addScanData(scanResult),
                throwable -> {
                    Log.d(Tag.BLE_SCANNER, "scanProcess Failed");
                    throwable.printStackTrace();
                }
        );

        isScanning = true;
    }

    /* Background Scan Process */
    private synchronized void scanProcessBackground(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            try {
                callbackIntent = PendingIntent.getBroadcast(context, 1000, new Intent(context, ScanReceiver.class), 0);

                mRxBleClient.getBackgroundScanner()
                        .scanBleDeviceInBackground(callbackIntent, scanSettings, scanFilterList.toArray(new ScanFilter[0]));

                isScanning = true;
            } catch (BleScanException scanException){
                Log.d(Tag.BLE_SCANNER, "Background scan Failed");
            }
        }
    }

    /* Scan Stop */
    public synchronized boolean stopScan(){

        Log.d(Tag.SCAN_TEXT, "stopScan");

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            stopBackgroundScan();
            Log.d(Tag.SCAN_TEXT, "stopBackgroundScan");
            return true;
        }

        if(scanSubscription != null && !scanSubscription.isDisposed()) {
            scanSubscription.dispose();
            isScanning = false;
            Log.d(Tag.SCAN_TEXT, "scanSubscription");
            return true;
        }

        scanSubscription = null;
        return false;
    }

    /* Background Scan Stop */
    public synchronized void stopBackgroundScan(){
        if(callbackIntent == null)
            return;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            mRxBleClient.getBackgroundScanner().stopBackgroundBleScan(callbackIntent);
            isScanning = false;
            Log.d(Tag.BLE_SCANNER, "Background Scan Stop!");
        }
    }

    /* 스캐닝 여부 */
    public boolean isScanning(){
        return isScanning;
    }
}
