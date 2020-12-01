package com.ksum.otaupdater.Bluetooth;

import android.util.Log;

import com.ksum.otaupdater.Interface.DataReceiver;
import com.ksum.otaupdater.Log.Tag;
import com.ksum.otaupdater.Vo.DeviceInfo;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.scan.ScanRecord;
import com.polidea.rxandroidble2.scan.ScanResult;

import java.util.LinkedHashMap;
import java.util.Timer;
import java.util.TimerTask;

public class BleDataManager {
    Timer dataSendTimer;
    DataReceiver dataReceiver;
    LinkedHashMap<String, DeviceInfo> scannedDevices = new LinkedHashMap<>();

    private static class LazyHolder{
        private static final BleDataManager INSTANCE = new BleDataManager();
    }

    private BleDataManager(){}

    public static BleDataManager getInstance(){
        return LazyHolder.INSTANCE;
    }

    public void bindReceiver(DataReceiver dataReceiver){
        unbindReceiver();
        this.dataReceiver = dataReceiver;
        if(dataReceiver != null)
            Log.d(Tag.RECYCLER_TEST, "bind Receiver Success");
    }

    public void unbindReceiver(){
        this.dataReceiver = null;
    }

    public void addScanData(ScanResult scanResult){
        RxBleDevice rxBleDevice = scanResult.getBleDevice();
        String name = rxBleDevice.getName();
        String address = rxBleDevice.getMacAddress();
        String connectable = rxBleDevice.getConnectionState().toString();
        ScanRecord scanRecord = scanResult.getScanRecord();

        DeviceInfo newDevice = new DeviceInfo(name, address, connectable, scanRecord, rxBleDevice);

        //Log.d(Tag.BLE_DATA_MANAGER, "New Device : " + address);
        scannedDevices.put(address, newDevice);
    }

    private void sendToActivity(){

        if(dataReceiver == null){
            Log.d(Tag.BLE_DATA_MANAGER, "sendToActivity() - dataReceiver -> null");
            return;
        }
        else {
            Log.d(Tag.RECYCLER_TEST, "bind Receiver is not null!");
        }

        dataReceiver.scanCallback(scannedDevices);
    }

    public synchronized void startTimer() {
        stopTimer();

        dataSendTimer = new Timer();
        dataSendTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendToActivity();
                scannedDevices = new LinkedHashMap<>();
            }
        }, 1000, 1000);

    }

    public synchronized void stopTimer(){

        if(dataSendTimer != null){
            dataSendTimer.cancel();
            dataSendTimer = null;
        }
    }

}
