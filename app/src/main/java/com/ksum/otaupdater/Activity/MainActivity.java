package com.ksum.otaupdater.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.ksum.otaupdater.Bluetooth.BleManager;
import com.ksum.otaupdater.Bluetooth.BleReceiver;
import com.ksum.otaupdater.Log.Tag;
import com.ksum.otaupdater.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(Tag.MAIN_ACTIVITY, "MainActivity onCreate");

    }
}