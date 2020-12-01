package com.ksum.otaupdater.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ksum.otaupdater.Adapter.ScanAdapter;
import com.ksum.otaupdater.Adapter.UpdateAdapter;
import com.ksum.otaupdater.Bluetooth.BleDataManager;
import com.ksum.otaupdater.Bluetooth.BleManager;
import com.ksum.otaupdater.Bluetooth.BleReceiver;
import com.ksum.otaupdater.Bluetooth.BleScanner;
import com.ksum.otaupdater.Interface.DataReceiver;
import com.ksum.otaupdater.Log.Tag;
import com.ksum.otaupdater.Manager.PermissionManager;
import com.ksum.otaupdater.R;
import com.ksum.otaupdater.Vo.DeviceInfo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements DataReceiver {
    @BindView(R.id.button_file)
    Button fileBtn;
    @BindView(R.id.button_filter)
    Button filterBtn;
    @BindView(R.id.button_scan)
    Button scanBtn;
    @BindView(R.id.button_reset)
    Button resetBtn;

    @BindView(R.id.recyclerview_device_list)
    RecyclerView rvScan;
    @BindView(R.id.recyclerview_update_list)
    RecyclerView rvUpdate;

    private PermissionManager permissionManager;
    private ScanAdapter scanAdapter;
    private UpdateAdapter updateAdapter;
    private ConcurrentHashMap<String, DeviceInfo> scannedMap;           // scan 한 기기
    private ConcurrentHashMap<String, DeviceInfo> removeMap;            // 제거 대상 기기
    private ArrayList<DeviceInfo> scannedList;                          // scan 한 기기 adapter 용
    private ArrayList<DeviceInfo> updateList;                           // update 할 기기 adapter 용

    private boolean isScanning;
    private Timer viewUpdateTimer;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        createPermissionManager();              // Permission Check

        initialize();                           // 변수 초기화
        initBluetooth();                        // 블루투스 설정

        initRecyclerView();                     // 리사이클러뷰 초기화
        startViewUpdate();                      // 리사이클러뷰 업데이트 시작
    }

    @Override
    protected void onResume() {
        super.onResume();
        permissionManager.permissionCheckOnResume();
        BleManager.getInstance().setBleEnable();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BleDataManager.getInstance().unbindReceiver();
        stopViewUpdate();
    }

    private void initialize(){
        isScanning = false;

        scannedMap = new ConcurrentHashMap<>();
        removeMap = new ConcurrentHashMap<>();
        scannedList = new ArrayList<>();
        updateList = new ArrayList<>();

        scanBtn.setOnClickListener(v -> scanControl());
        resetBtn.setOnClickListener(v -> resetList());
    }

    /* RecyclerView ---------------------------------------------------------------------------------------------------------------------*/
    private void setRecyclerView(RecyclerView recyclerView){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        //recyclerView.setItemAnimator(null);  => 혹시 이거 땜??
    }

    private void initRecyclerView(){
        setRecyclerView(rvScan);
        setRecyclerView(rvUpdate);

        scanAdapter = new ScanAdapter(scannedList);
        updateAdapter = new UpdateAdapter(updateList);

        rvScan.setAdapter(scanAdapter);
        rvUpdate.setAdapter(updateAdapter);

        scanAdapter.setOnStateButtonClickListener(deviceInfo -> {
            if(deviceInfo == null)
                return;

            addUpdateList(deviceInfo);
        });

        updateAdapter.setOnRemoveButtonClickListener(deviceInfo -> {
            if(deviceInfo == null)
                return;
            removeUpdateList(deviceInfo);
        });

    }

    private void changeRvUpdateVisibility(){
        if(updateList.size() > 0)
            rvUpdate.setVisibility(View.VISIBLE);
        else
            rvUpdate.setVisibility(View.GONE);
    }

    private void addUpdateList(DeviceInfo deviceInfo){
        deviceInfo.setIsWaiting(true);                            // 대기 상태 true
        updateList.add(deviceInfo);                               // 업데이트 리스트 추가
        scannedList.remove(deviceInfo);                           // 스캔 리스트 제거
        removeMap.put(deviceInfo.getAddress(), deviceInfo);       // 제거 리스트 추가
        changeRvUpdateVisibility();                               // 업데이트 리스트 visibility 변경
    }

    private void removeUpdateList(DeviceInfo deviceInfo){
        // 대기 상태라면
        if(deviceInfo.isWaiting()){                               // 대기 상태라면
            deviceInfo.setIsWaiting(false);                       // 대기 상태 false
            scannedList.add(deviceInfo);                          // 스캔 리스트 추가
            updateList.remove(deviceInfo);                        // 업데이트 리스트 제거
            removeMap.remove(deviceInfo.getAddress());            // 제거 리스트에서 제거
        }

        changeRvUpdateVisibility();                               // 업데이트 리스트 visibility 변경
    }

    private int getItemIndex(String address){
        for(int index = 0; index < scannedList.size(); index++){
            if(scannedList.get(index).getAddress().equals(address))
                return index;
        }

        return -1;
    }

    private void startViewUpdate(){
        viewUpdateTimer = new Timer();
        TimerTask viewUpdateTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    scanAdapter.notifyDataSetChanged();
                    updateAdapter.notifyDataSetChanged();
                });
            }
        };

        viewUpdateTimer.schedule(viewUpdateTask, 500, 500);
    }

    private void stopViewUpdate(){
        viewUpdateTimer.cancel();
    }

    private boolean isScanningWithToast(){
        if(isScanning){
            Toast.makeText(this, getString(R.string.toast_scanning_retry), Toast.LENGTH_SHORT);
            return true;
        }

        return false;
    }

    // Update List 랑 remove List 는 없애면 안댐
    private void resetList() {
        if(isScanningWithToast())
            return;

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("스캔된 리스트 초기화");
        alertDialog.setMessage("리스트를 초기화 하시겠습니까?");
        alertDialog.setPositiveButton("초기화", ((dialog, which) -> {
            scannedMap.clear();
            scannedList.clear();
        }));
        alertDialog.setNegativeButton("취소", ((dialog, which) -> {
            alertDialog.create().dismiss();
        }));

        alertDialog.setCancelable(false);
        alertDialog.create().show();
    }

    /* Bluetooth ------------------------------------------------------------------------------------------------------------------------*/
    private void initBluetooth(){
        BleManager.getInstance().setBleEnable();
        BleDataManager.getInstance().bindReceiver(MainActivity.this);
        BleScanner.getInstance().setRxClient(this);
    }

    private void scanControl(){
        Log.d(Tag.SCAN_TEXT, "scanProcess");

        if(isScanning) {
            isScanning = !BleScanner.getInstance().stopScan();
            BleDataManager.getInstance().stopTimer();
            scanBtn.setText(getString(R.string.button_scan_start));
            Toast.makeText(this, getString(R.string.toast_scan_stop), Toast.LENGTH_LONG).show();
        }
        else {
            isScanning = BleScanner.getInstance().startScan();
            BleDataManager.getInstance().startTimer();
            scanBtn.setText(getString(R.string.button_scan_stop));
            Toast.makeText(this, getString(R.string.toast_scan_start), Toast.LENGTH_LONG).show();
        }
    }

    /* Scan Result Process --------------------------------------------------------------------------------------------------------------*/
    @Override
    public void scanCallback(LinkedHashMap<String, DeviceInfo> scannedDevices) {
        addScannedDevices(scannedDevices);
        Log.d(Tag.RECYCLER_TEST, "scanCallback!!");
    }

    private void addScannedDevices(LinkedHashMap<String, DeviceInfo> newData){
        String[] keySet = newData.keySet().toArray(new String[0]);
        DeviceInfo newDevice;

        for(final String key : keySet){

            newDevice = newData.get(key);

            if(removeMap.containsKey(key) || newDevice == null)                         // 1. remove 리스트에 있거나 null 이면 continue
                continue;

            if(scannedMap.containsKey(key))                                             // 2. scannedDevices 에 존재하면
                scannedList.set(getItemIndex(newDevice.getAddress()), newDevice);       // 2-1. scanList 에서 교체
            else                                                                        // 3. scannedDevices 에 존재 안하면
                scannedList.add(newDevice);                                             // 3-1. scanList 에 추가

            scannedMap.put(key, newDevice);                                             // 4. scannedDevices 에 추가

            Log.d(Tag.RECYCLER_TEST, "scannedList Size : " + scannedList.size());
        }
    }

    /* Permision -----------------------------------------------------------------------------------------------------------------------*/
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void createPermissionManager(){
        String[] locationPermissions = PermissionManager.locationPermissions;
        View snackBarView = findViewById(R.id.layout_main);
        String rationale = getString(R.string.permission_location_rationale);

        permissionManager = new PermissionManager.Builder()
                .setContext(this)
                .setRequestPermissions(locationPermissions)
                .setSnackBarView(snackBarView)
                .setRationale(rationale)
                .setPermissionCode(PermissionManager.PERMISSION_DEFAULT_CODE)
                .build();
        
        permissionManager.permissionCheckProcess();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // 코드 다름
        if(requestCode != PermissionManager.PERMISSION_DEFAULT_CODE)
            return;

        // 전부 승인
        if(permissionManager.isAllGranted(permissions, grantResults))
            return;

        // 승인 거부 퍼미션 요청 + 취소 다이얼로그 onClickListener
        permissionManager.reRequestPermissionProcess(permissions, (dialog, which) -> {
            Toast.makeText(MainActivity.this, getString(R.string.toast_permission_denied), Toast.LENGTH_LONG).show();
            finish();
        });
    }
}