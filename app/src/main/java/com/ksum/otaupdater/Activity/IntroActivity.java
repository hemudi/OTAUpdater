package com.ksum.otaupdater.Activity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ksum.otaupdater.Bluetooth.BleManager;
import com.ksum.otaupdater.Bluetooth.BleReceiver;
import com.ksum.otaupdater.Log.Tag;
import com.ksum.otaupdater.R;

import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        if(isBleSupported()){
            checkBluetoothEnable();

            registerBleReceiver();

            Intent toMainIntent = new Intent(IntroActivity.this, MainActivity.class);
            startActivity(toMainIntent);
        }
        else {
            new Handler().postDelayed(this::finish, 2000);
        }

        finish();

    }

    private boolean isBleSupported(){
        if(BleManager.getInstance().isNotSupportBle(this)) {
            Toast.makeText(this, getString(R.string.toast_is_not_support_ble), Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void checkBluetoothEnable(){
        if(!BleManager.getInstance().isNotEnabled())
            BleManager.getInstance().setBleEnable();
    }

    private void registerBleReceiver(){
        IntentFilter bleIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        BleReceiver bleReceiver = new BleReceiver();
        registerReceiver(bleReceiver, bleIntentFilter);
    }

}
