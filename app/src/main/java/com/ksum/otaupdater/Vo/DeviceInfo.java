package com.ksum.otaupdater.Vo;

import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.scan.ScanRecord;

public class DeviceInfo {
    String name;
    String address;
    String connectable;
    ScanRecord scanRecord;
    RxBleDevice bleDevice;

    public DeviceInfo(){}
    public DeviceInfo(String name, String address, String connectable, ScanRecord scanRecord, RxBleDevice bleDevice) {
        this.name = name;
        this.address = address;
        this.connectable = connectable;
        this.scanRecord = scanRecord;
        this.bleDevice = bleDevice;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getConnectable() { return connectable; }
    public void setConnectable(String connectable) { this.connectable = connectable; }

    public ScanRecord getScanRecord() { return scanRecord; }
    public void setScanRecord(ScanRecord scanRecord) { this.scanRecord = scanRecord; }

    public RxBleDevice getBleDevice() { return bleDevice; }
    public void setBleDevice(RxBleDevice bleDevice) { this.bleDevice = bleDevice; }
}
