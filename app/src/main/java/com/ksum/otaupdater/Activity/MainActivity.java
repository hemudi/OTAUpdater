package com.ksum.otaupdater.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ksum.otaupdater.Adapter.ScanAdapter;
import com.ksum.otaupdater.Adapter.UpdateAdapter;
import com.ksum.otaupdater.Bluetooth.BleDataManager;
import com.ksum.otaupdater.Bluetooth.BleManager;
import com.ksum.otaupdater.Bluetooth.BleScanner;
import com.ksum.otaupdater.Dialog.ResetDialog;
import com.ksum.otaupdater.Interface.DataReceiver;
import com.ksum.otaupdater.ItemTouchHelper.ItemTouchHelperCallback;
import com.ksum.otaupdater.Log.Tag;
import com.ksum.otaupdater.Manager.PermissionManager;
import com.ksum.otaupdater.R;
import com.ksum.otaupdater.Vo.DeviceInfo;
import com.ksum.otaupdater.Dialog.ResetDialog.ResetDialogClickListener;

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
    @BindView(R.id.button_list_toggle)
    Button listToggleBtn;
    @BindView(R.id.button_list_wide)
    Button listWideBtn;
    @BindView(R.id.container_button_update_list)
    LinearLayout containerListBtn;

    @BindView(R.id.recyclerview_device_list)
    RecyclerView rvScan;
    @BindView(R.id.recyclerview_update_list)
    RecyclerView rvUpdate;

    private PermissionManager permissionManager;
    private ScanAdapter scanAdapter;
    private UpdateAdapter updateAdapter;
    private ConcurrentHashMap<String, DeviceInfo> allScanList;          // scan 한 기기
    private ConcurrentHashMap<String, DeviceInfo> blackList;            // 제거 대상 기기
    private ArrayList<DeviceInfo> scannedList;                          // scan 한 기기 adapter 용
    private ArrayList<DeviceInfo> updateList;                           // update 할 기기 adapter 용

    private boolean isScanning;                                         // 스캔 중
    private boolean isUpdating;                                         // 업데이트 중

    private Timer viewUpdateTimer;                                      // recyclerView update Timer

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

//        startViewUpdate();                      // 리사이클러뷰 업데이트 시작
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
//        stopViewUpdate();
    }

    private void initialize() {
        isScanning = false;
        isUpdating = false;

        allScanList = new ConcurrentHashMap<>();
        blackList = new ConcurrentHashMap<>();
        scannedList = new ArrayList<>();
        updateList = new ArrayList<>();

        scanBtn.setOnClickListener(v -> scanControl());
        resetBtn.setOnClickListener(v -> resetList());
        listToggleBtn.setOnClickListener(v -> toggleUpdateList());
        listWideBtn.setOnClickListener(v -> wideUpdateList());
    }

    /* RecyclerView ---------------------------------------------------------------------------------------------------------------------*/
    private void setRecyclerView(RecyclerView recyclerView) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
//        recyclerView.setItemAnimator(null);  => 혹시 이거 땜??
    }

    private void initRecyclerView() {
        setRecyclerView(rvScan);
        setRecyclerView(rvUpdate);

        scanAdapter = new ScanAdapter(scannedList);
        updateAdapter = new UpdateAdapter(updateList);

        rvScan.setAdapter(scanAdapter);
        rvUpdate.setAdapter(updateAdapter);

        scanAdapter.setOnStateButtonClickListener(new ScanAdapter.OnStateButtonClickListener() {
            @Override
            public void onStateClick(DeviceInfo deviceInfo) {
                if (deviceInfo == null) {
//                    Log.d(Tag.RECYCLER_TEST, "scanAdapter onStateClick Called! => deviceInfo is null!");
                    return;
                }

//                Log.d(Tag.RECYCLER_TEST, "scanAdapter onStateClick Called!");

                addUpdateList(deviceInfo);
            }

            @Override
            public void removeDevice(DeviceInfo deviceInfo) {
                // scanList + scanMap 에서 제거하고
                // blackList 에 추가하기

                addBlackList(deviceInfo);

            }
        });

        updateAdapter.setOnRemoveButtonClickListener(deviceInfo -> {
            if (deviceInfo == null) {
//                Log.d(Tag.RECYCLER_TEST, "updateAdapter onRemoveClick Called! => deviceInfo is null!");
                return;
            }

//            Log.d(Tag.RECYCLER_TEST, "updateAdapter onRemoveClick Called!");
            removeUpdateList(deviceInfo);
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelperCallback(0, ItemTouchHelper.LEFT, scanAdapter));
        itemTouchHelper.attachToRecyclerView(rvScan);

    }

    /* RecyclerView Control ----------------------------------------------------------------------------------------------------------------*/
    private void changeRvUpdateVisibility() {

        /*
        * 맨 처음 펼쳐놓고 펼쳐져있으면 계속해서 펼쳐놓기
        * */
        if (updateList.size() > 0) {
//            rvUpdate.setVisibility(View.VISIBLE);
            containerListBtn.setVisibility(View.VISIBLE);
        }
        else {
            rvUpdate.setVisibility(View.GONE);
            rvScan.setVisibility(View.VISIBLE);
            containerListBtn.setVisibility(View.GONE);
            listToggleBtn.setVisibility(View.VISIBLE);
            listWideBtn.setVisibility(View.GONE);
        }
    }

    private void toggleUpdateList() {
        if (rvUpdate.getVisibility() == View.VISIBLE) {         // 접기
            rvUpdate.setVisibility(View.GONE);
            listWideBtn.setVisibility(View.GONE);
            listToggleBtn.setText(getString(R.string.button_update_list_wide));
        }
        else {                                                  // 펼치기
            rvUpdate.setVisibility(View.VISIBLE);
            listWideBtn.setVisibility(View.VISIBLE);
            listToggleBtn.setText(getString(R.string.button_update_list_gone));
        }
    }

    private void wideUpdateList(){
        if(rvScan.getVisibility() == View.VISIBLE){             // 넓히기
            rvScan.setVisibility(View.GONE);
            listToggleBtn.setVisibility(View.GONE);
            listWideBtn.setText(getString(R.string.button_update_list_gone));
        }
        else {                                                  // 반 접기
            rvScan.setVisibility(View.VISIBLE);
            listToggleBtn.setVisibility(View.VISIBLE);
            listWideBtn.setText(getString(R.string.button_update_list_wide));
        }
    }

    private void addUpdateList(DeviceInfo deviceInfo) {
//        new Handler().postDelayed(() -> {
//        Log.d(Tag.RECYCLER_TEST, "scanAdapter onStateClick Called! => addUpdateList!!");
            deviceInfo.setIsWaiting(true);                            // 대기 상태 true
            updateList.add(deviceInfo);                               // 업데이트 리스트 추가
            scannedList.remove(deviceInfo);                           // 스캔 리스트 제거
//        blackList.put(deviceInfo.getAddress(), deviceInfo);     // 제거 리스트 추가
            changeRvUpdateVisibility();                               // 업데이트 리스트 visibility 변경
            adapterNotify();
//        }, 100);

    }

    private void removeUpdateList(DeviceInfo deviceInfo) {

//        Log.d(Tag.RECYCLER_TEST, "updateAdapter onRemoveClick Called! => removeUpdateList!!");

        // 대기 상태라면
        if (deviceInfo.isWaiting()) {                             // 대기 상태라면
            deviceInfo.setIsWaiting(false);                       // 대기 상태 false
            scannedList.add(deviceInfo);                          // 스캔 리스트 추가
            updateList.remove(deviceInfo);                        // 업데이트 리스트 제거
//            blackList.remove(deviceInfo.getAddress());          // 제거 리스트에서 제거
        }
//        else
//            Log.d(Tag.RECYCLER_TEST, "updateAdapter onRemoveClick Called! => is not waiting!");

        adapterNotify();
        changeRvUpdateVisibility();                               // 업데이트 리스트 visibility 변경
    }

    private void addBlackList(DeviceInfo deviceInfo) {
        if (deviceInfo == null)
            return;

        if (deviceInfo.isWaiting() || deviceInfo.isUpdating())
            return;

//        scannedMap.remove(deviceInfo.getAddress());
        scannedList.remove(deviceInfo);                           // 스캔 리스트에서 삭제 -> scanMap 에서도 없애야되나
        blackList.put(deviceInfo.getAddress(), deviceInfo);       // 제거 리스트에 추가

        adapterNotify();

        Log.d(Tag.BLACK_TEST, "Add Black List : " + deviceInfo.getAddress() + " -> " + containsInUpdateList(deviceInfo));
    }

    private int getItemIndex(String address) {
        for (int index = 0; index < scannedList.size(); index++) {
            if (scannedList.get(index).getAddress().equals(address))
                return index;
        }

        return -1;
    }

    /* RecyclerView Reset ----------------------------------------------------------------------------------------------------------------*/
    // Update List 랑 remove List 는 없애면 안댐
    private void resetList() {
        if (isScanningWithToast())
            return;

        if (isAllEmpty()) {
            Toast.makeText(MainActivity.this, "초기화 시킬 수 있는 리스트가 없습니다.", Toast.LENGTH_LONG).show();
            return;
        }

        ResetDialogClickListener resetDialogClickListener = (cbScan, cbBlack, cbUpdate) -> {
            if (cbScan.isChecked() && cbScan.isEnabled()) {
                allScanList.clear();
                scannedList.clear();
                Log.d(Tag.RECYCLER_TEST, "scanChecked is clear!");
            }

            if (cbBlack.isChecked() && cbBlack.isEnabled()) {
                blackList.clear();
                Log.d(Tag.RECYCLER_TEST, "blackList is clear!");
                Log.d(Tag.BLACK_TEST, "BlackList size : " + blackList.size());
            }

            if (cbUpdate.isChecked() && cbUpdate.isEnabled()) {
//                scannedList.addAll(updateList); => 걍 안하는게 나은 듯
                updateList.clear();
                changeRvUpdateVisibility();
                Log.d(Tag.RECYCLER_TEST, "updateList is clear!");
            }

            adapterNotify();
        };

        boolean[] emptyCheckList = getEmptyCheckList();
        ResetDialog resetDialog = new ResetDialog(MainActivity.this, resetDialogClickListener, emptyCheckList);
        resetDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        resetDialog.setCancelable(false);
        resetDialog.show();
    }

    private boolean isAllEmpty() {
//        if (!scannedMap.isEmpty())
//            return false;

        if (!scannedList.isEmpty())
            return false;

        if (!updateList.isEmpty())
            return false;

        if (!blackList.isEmpty())
            return false;

        return true;
    }

    private boolean[] getEmptyCheckList() {
        boolean[] emptyList = new boolean[]{false, false, false};

        if(scannedList.isEmpty() /*&& scannedMap.isEmpty()*/)
            emptyList[0] = true;

        if(blackList.isEmpty())
            emptyList[1] = true;

        if(updateList.isEmpty())
            emptyList[2] = true;

        return emptyList;
    }

    /* RecyclerView Update Timer ---------------------------------------------------------------------------------------------------------*/

    private void adapterNotify(){
        scanAdapter.notifyDataSetChanged();
        updateAdapter.notifyDataSetChanged();
    }

    private void startViewUpdate() {
        viewUpdateTimer = new Timer();
        TimerTask viewUpdateTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    adapterNotify();
                });
            }
        };

        viewUpdateTimer.schedule(viewUpdateTask, 500, 300);
    }

    private void stopViewUpdate() {
        new Handler().postDelayed(() -> viewUpdateTimer.cancel(), 500);
    }

    private boolean isScanningWithToast() {
        if (isScanning) {
            Toast.makeText(this, getString(R.string.toast_scanning_retry), Toast.LENGTH_SHORT);
            return true;
        }

        return false;
    }

    /* Bluetooth ------------------------------------------------------------------------------------------------------------------------*/
    private void initBluetooth() {
        BleManager.getInstance().setBleEnable();
        BleDataManager.getInstance().bindReceiver(MainActivity.this);
        BleScanner.getInstance().setRxClient(this);
    }

    private void scanControl() {
        Log.d(Tag.SCAN_TEXT, "scanProcess");

        if (isScanning) {
            isScanning = !BleScanner.getInstance().stopScan();
            BleDataManager.getInstance().stopTimer();
            scanBtn.setText(getString(R.string.button_scan_start));
            Toast.makeText(this, getString(R.string.toast_scan_stop), Toast.LENGTH_LONG).show();
            stopViewUpdate();
        } else {
            isScanning = BleScanner.getInstance().startScan();
            BleDataManager.getInstance().startTimer();
            scanBtn.setText(getString(R.string.button_scan_stop));
            Toast.makeText(this, getString(R.string.toast_scan_start), Toast.LENGTH_LONG).show();
            startViewUpdate();
        }
    }

    /* Scan Result Process --------------------------------------------------------------------------------------------------------------*/
    @Override
    public void scanCallback(LinkedHashMap<String, DeviceInfo> scannedDevices) {
        addScannedDevices(scannedDevices);
//        Log.d(Tag.RECYCLER_TEST, "scanCallback!!");
    }

    private void addScannedDevices(LinkedHashMap<String, DeviceInfo> newData) {
        String[] keySet = newData.keySet().toArray(new String[0]);
        DeviceInfo newDevice;

        for (final String key : keySet) {

            newDevice = newData.get(key);

            if(newDevice == null)
                continue;

            if (containsInUpdateList(newDevice) || blackList.containsKey(key)) {         // 1. blackList 나 updateList 에 있는지 체크
                Log.d(Tag.BLACK_TEST, "Filtering " + key);
                continue;
            }

            if (allScanList.containsKey(key))                                            // 2. scannedDevices 에 존재하면
                scannedList.set(getItemIndex(newDevice.getAddress()), newDevice);        // 2-1. scanList 에서 교체
            else                                                                         // 3. scannedDevices 에 존재 안하면
                scannedList.add(newDevice);                                              // 3-1. scanList 에 추가

            allScanList.put(key, newDevice);                                             // 4. scannedDevices 에 추가

//            Log.d(Tag.RECYCLER_TEST, "scannedList Size : " + scannedList.size());
        }
    }

    private boolean containsInUpdateList(DeviceInfo device){
        String address = device.getAddress();

        for(DeviceInfo deviceInfo : updateList){
            if(deviceInfo.getAddress().equals(address))
                return true;
        }

        return false;
    }

    /* Permision -----------------------------------------------------------------------------------------------------------------------*/
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void createPermissionManager() {
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
        if (requestCode != PermissionManager.PERMISSION_DEFAULT_CODE)
            return;

        // 전부 승인
        if (permissionManager.isAllGranted(permissions, grantResults))
            return;

        // 승인 거부 퍼미션 요청 + 취소 다이얼로그 onClickListener
        permissionManager.reRequestPermissionProcess(permissions, (dialog, which) -> {
            Toast.makeText(MainActivity.this, getString(R.string.toast_permission_denied), Toast.LENGTH_LONG).show();
            finish();
        });
    }
}