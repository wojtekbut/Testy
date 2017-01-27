package com.example.wojtek.testy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


public class MyBluetoothReceiver extends BroadcastReceiver {
    private static final String TAG = "BluetoothReceiver";    // a tag for logging
    private static final int FAIL = -1;

    public MyBluetoothReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        String action = intent.getAction();            // Get intent's action string
        Bundle extras = intent.getExtras();
        Log.d(TAG, action);
        Log.d(TAG, extras.toString());
        switch (action) {
            case "android.bluetooth.device.action.ACL_DISCONNECTED": {


                //bluetoothStateChanged(extras.getInt("android.bluetooth.adapter.extra.STATE", FAIL));
                break;
            }
            case "android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED": {
                //a2dpStateChanged(
                //      extras.getInt("android.bluetooth.profile.extra.STATE", FAIL),
                //    (BluetoothDevice) extras.get("android.bluetooth.device.extra.DEVICE"));
                break;
            }
            case "android.bluetooth.device.action.BOND_STATE_CHANGED": {
                //bondStateChanged(
                //       extras.getInt("android.bluetooth.device.extra.BOND_STATE", FAIL),
                //     (BluetoothDevice) extras.get("android.bluetooth.device.extra.DEVICE"));
                break;
            }
        }

    }
}
