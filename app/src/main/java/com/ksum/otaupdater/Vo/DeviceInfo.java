package com.ksum.otaupdater.Vo;

import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.scan.ScanRecord;

public class DeviceInfo {
    private final String name;
    private final String address;
    private String connectable;
    private ScanRecord scanRecord;
    private RxBleDevice bleDevice;
    private int failedCount;
    private boolean isWaiting;      // 대기열에서 대기 중
    private boolean isUpdating;     // 업데이트 중
    private boolean isFailed;       // 업데이트 실패

    public DeviceInfo(String name, String address, String connectable, ScanRecord scanRecord, RxBleDevice bleDevice) {
        this.name = name;
        this.address = address;
        this.connectable = connectable;
        this.scanRecord = scanRecord;
        this.bleDevice = bleDevice;
        this.failedCount = 0;
        this.isWaiting = false;
    }

    public DeviceInfo(String name, String address, String connectable) {
        this.name = name;
        this.address = address;
        this.connectable = connectable;
        this.failedCount = 0;
        this.isWaiting = false;
    }

    public String getName() { return name; }

    public String getAddress() { return address; }

    public String getConnectable() { return connectable; }
    public void setConnectable(String connectable) { this.connectable = connectable; }

    public ScanRecord getScanRecord() { return scanRecord; }
    public void setScanRecord(ScanRecord scanRecord) { this.scanRecord = scanRecord; }

    public RxBleDevice getBleDevice() { return bleDevice; }
    public void setBleDevice(RxBleDevice bleDevice) { this.bleDevice = bleDevice; }

    // failed count 는 대기열에 들어갔을때 사용
    public void addFailedCount(){ failedCount++; }
    public void clearFailedCount(){ failedCount = 0; }
    public int getFailedCount(){ return failedCount; }

    public void setIsWaiting(boolean recentState){ isWaiting = recentState; }
    public boolean isWaiting(){ return isWaiting; }

    public void setIsUpdating(boolean recentState) { isUpdating = recentState; }
    public boolean isUpdating(){ return isUpdating; }
}
