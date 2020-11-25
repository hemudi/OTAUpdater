package com.ksum.otaupdater.Interface;

import com.ksum.otaupdater.Vo.DeviceInfo;

import java.util.LinkedHashMap;

public interface DataReceiver {
    void scanCallback(LinkedHashMap<String, DeviceInfo> scannedDevices);
}
