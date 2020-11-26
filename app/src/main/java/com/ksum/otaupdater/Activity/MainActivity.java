package com.ksum.otaupdater.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ksum.otaupdater.Bluetooth.BleManager;
import com.ksum.otaupdater.Bluetooth.BleReceiver;
import com.ksum.otaupdater.Log.Tag;
import com.ksum.otaupdater.Manager.PermissionManager;
import com.ksum.otaupdater.R;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    PermissionManager permissionManager;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(Tag.MAIN_ACTIVITY, "MainActivity onCreate");

        createPermissionManager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        permissionManager.permissionCheckProcess();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void createPermissionManager(){
        String[] locationPermissions = PermissionManager.locationPermissions;
        View snackBarView = findViewById(R.id.layout_main);
        String rationale = getString(R.string.permission_location_rationale);

        permissionManager = new PermissionManager(this,
                                                    PermissionManager.PERMISSION_DEFAULT_CODE,
                                                    locationPermissions,
                                                    snackBarView,
                                                    rationale);
        permissionManager.permissionCheckProcess();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode != PermissionManager.PERMISSION_DEFAULT_CODE)
            return;

        if(permissionManager.requestResult(permissions, grantResults))
            return;

        permissionManager.showPermissionDialog((dialog, which) -> {
            Toast.makeText(MainActivity.this, getString(R.string.toast_permission_denied), Toast.LENGTH_LONG).show();
            finish();
        });

    }
}